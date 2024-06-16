package de.rccookie.haskell.parse;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import de.rccookie.haskell.ArgOf;
import de.rccookie.haskell.Bot;
import de.rccookie.haskell.Call;
import de.rccookie.haskell.Case;
import de.rccookie.haskell.DataCtor;
import de.rccookie.haskell.Declaration;
import de.rccookie.haskell.Expression;
import de.rccookie.haskell.FunctionDecl;
import de.rccookie.haskell.IfThenElse;
import de.rccookie.haskell.IsA;
import de.rccookie.haskell.JokerPattern;
import de.rccookie.haskell.Lambda;
import de.rccookie.haskell.Let;
import de.rccookie.haskell.Match;
import de.rccookie.haskell.Pattern;
import de.rccookie.haskell.Sel;
import de.rccookie.haskell.Tuple;
import de.rccookie.haskell.Variable;

public record Constructor(String name, List<Literal> params) implements Literal {

    @Override
    public String toString() {
        if(params.isEmpty() && !name.equalsIgnoreCase("tuple"))
            return name;
        return (name.equalsIgnoreCase("tuple") ? "" : name) + "(" + params.stream().map(Objects::toString).collect(Collectors.joining(", ")) + ")";
    }

    public Expression instantiate() {
        checkParamCount();
        return switch(name.toLowerCase()) {
            case "argof" -> new ArgOf(
                    getName(0, "type name"),
                    getExpr(1, "expression")
            );
            case "bot" -> Bot.INSTANCE;
            case "call" -> {
                Call call = new Call(getExpr(0, "function"), getExpr(1, "1st parameter"));
                for(int i=2; i<params.size(); i++)
                    call = new Call(call, getExpr(i, indexToStr(i-1)+" parameter"));
                yield call;
            }
            case "case" -> new Case(
                    getExpr(0, "argument"),
                    getTupleAsList(1, "case options tuple", Case.Option.class)
            );
            case "option" -> new Case.Option(
                    getExpr(0, "pattern", Pattern.class),
                    getExpr(1, "expression")
            );
            case "data" -> new DataCtor(
                    getName(0, "data constructor name"),
                    getAll(1, "argument")
            );
            case "decl" -> new Declaration(
                    getExpr(0, "varName", Variable.class),
                    getTupleAsList(1, "params tuple", Pattern.class),
                    getExpr(2, "value")
            );
            case "func" -> {
                if(params.isEmpty())
                    throw new SemanticException("Function declaration requires at least 1 declaration statement");
                FunctionDecl f = new FunctionDecl(getAll(0, "function pattern", Declaration.class));
                if(f.declarations().stream().map(e -> e.variable().name()).distinct().count() > 1)
                    throw new SemanticException("Declarations inside function declaration must all use the same function name");
                if(f.declarations().stream().mapToInt(e -> e.params().size()).distinct().count() > 1)
                    throw new SemanticException("Patterns of function '"+f.declarations().get(0).variable().name()+"' must all have the same number of arguments");
                yield f;
            }
            case "if" -> new IfThenElse(
                    getExpr(0, "condition"),
                    getExpr(1, "then case"),
                    params.size() > 2 ? getExpr(2, "else case") : null
            );
            case "isa" -> new IsA(
                    getName(0, "type name"),
                    getExpr(1, "expression")
            );
            case "_" -> JokerPattern.INSTANCE;
            case "lambda", "\\" -> new Lambda(
                    getTupleAsList(0, "params tuple", Pattern.class),
                    getExpr(1, "expression")
            );
            case "let" -> new Let(
                    getTupleAsList(0, "declarations tuple", Expression.class),
                    getExpr(1, "in")
            );
            case "match" -> new Match(
                    getExpr(0, "pattern", Pattern.class),
                    getExpr(1, "input expression"),
                    getExpr(2, "expression if match"),
                    getExpr(3, "expression if no match")
            );
            case "sel" -> new Sel(
                    getInt(0, "tuple size"),
                    getInt(1, "index"),
                    getExpr(2, "tuple expression")
            );
            case "tuple" -> Tuple.of(getAll(0, "element"));
            case "var" -> new Variable(getName(0, "name"));
            default -> new DataCtor(name);
        };
    }

    private void checkParamCount() {
        int maxCount = switch(name.toLowerCase()) {
            case "bot", "_", "true", "false" -> 0;
            case "var" -> 1;
            case "argof", "let", "lambda", "\\", "isa", "case", "option" -> 2;
            case "decl", "sel", "if" -> 3;
            case "match" -> 4;
            case "call", "tuple", "func", "data" -> Integer.MAX_VALUE;
            default -> {
                if(name.matches("\\d+"))
                    yield 0;
                throw new SemanticException("Unknown type: '"+name+"()'");
            }
        };
        if(params.size() > maxCount)
            throw new SemanticException("Too many arguments for '"+name+"()', expected at most "+maxCount+", got "+params.size());
    }

    public String getName(int index, String fieldName) {
        Literal l = get(index, fieldName);
        if(!(l instanceof Name n))
            throw new SemanticException(indexToStr(index) + " parameter '" + fieldName + "' of '" + name + "()' should be a string");
        return n.value();
    }

    public int getInt(int index, String fieldName) {
        String str = getName(index, fieldName);
        try {
            return Integer.parseInt(str);
        } catch(NumberFormatException e) {
            throw new SemanticException(indexToStr(index) + " parameter '" + fieldName + "' of '" + name + "()' is not a number in a string");
        }
    }

    public Expression getExpr(int index, String fieldName) {
        return getExpr(index, fieldName, Expression.class);
    }

    public <E extends Expression> E getExpr(int index, String fieldName, Class<E> type) {
        Literal l = get(index, fieldName);
        Expression e;
        if(l instanceof Constructor c)
            e = c.instantiate();
        else e = new Variable(((Name) l).value());
        if(!type.isInstance(e))
            throw new SemanticException(indexToStr(index)+" parameter '"+fieldName+"' of '"+name+"()' should be a "+nameOf(type)+", got a "+nameOf(e));
        return type.cast(e);
    }

    public <E extends Expression> List<E> getTupleAsList(int index, String fieldName, Class<E> type) {
        Literal l = get(index, fieldName);
        if(!(l instanceof Constructor c) || !c.name.equals("tuple"))
            throw new SemanticException(indexToStr(index)+" parameter '"+fieldName+"' of '"+name+"()' should be a tuple of "+nameOf(type)+"s, got a "+(l instanceof Constructor c ? c.name : "variable"));

        return c.params.stream()
                .map(ll -> ll instanceof Constructor cc ? cc.instantiate() : new Variable(((Name) ll).value()))
                .map(e -> {
                    if(type.isInstance(e))
                        return type.cast(e);
                    throw new SemanticException(indexToStr(index)+" parameter '"+fieldName+"' of '"+name+"()' should be a tuple of "+nameOf(type)+"s, but contains a "+nameOf(e));
                })
                .toList();
    }

    public List<Expression> getAll(int fromIndex, String baseName) {
        return getAll(fromIndex, baseName, Expression.class);
    }

    public <E extends Expression> List<E> getAll(int fromIndex, String baseName, Class<E> type) {
        return IntStream.range(fromIndex, params.size())
                .mapToObj(i -> getExpr(i, indexToStr(i - fromIndex)+" "+baseName, type))
                .toList();
    }

    public Literal get(int index, String fieldName) {
        if(index >= params.size())
            throw new SemanticException("Too few arguments for '"+name+"()' (missing field '"+fieldName+"')");
        return params.get(index);
    }


    private static String indexToStr(int index) {
        return switch(index) {
            case 0 -> "1st";
            case 1 -> "2nd";
            case 2 -> "3rd";
            default -> (index+1)+"th";
        };
    }

    private static String nameOf(Object o) {
        if(o == null)
            return "null";
        if(!(o instanceof Class<?> c))
            return nameOf(o.getClass());
        if(c == DataCtor.class)
            return "data";
        if(c == Declaration.class)
            return "decl";
        if(c == FunctionDecl.class)
            return "func";
        if(c == IfThenElse.class)
            return "if";
        if(c == JokerPattern.class)
            return "_";
        if(c == Variable.class)
            return "var";
        return c.getSimpleName().toLowerCase();
    }
}
