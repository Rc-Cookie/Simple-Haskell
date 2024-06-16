package de.rccookie.haskell.simple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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
import de.rccookie.haskell.Mapper;
import de.rccookie.haskell.Match;
import de.rccookie.haskell.Pattern;
import de.rccookie.haskell.Tuple;
import de.rccookie.haskell.Variable;
import de.rccookie.json.JsonSerializable;
import de.rccookie.util.BoolWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Rule extends JsonSerializable {

    Rule FUNC_TO_CASE_1 = new Rule() {
        @Override
        public @Nullable Expression tryApply(@NotNull Expression e, Expression root) {
            if(e instanceof FunctionDecl decl) {
                List<Variable> variables = new ArrayList<>();
                String varBaseName = Rule.newVariableGroup(e, root);
                for(int i=0, count=decl.declarations().get(0).params().size(); i<count; i++)
                    variables.add(new Variable(varBaseName + (count == 1 ? "" : "_"+(i+1))));
                return new Declaration(
                    decl.declarations().get(0).variable(),
                    List.of(),
                    new Lambda(variables, new Case(
                            Tuple.of(variables),
                            ((FunctionDecl) e).declarations()
                                    .stream()
                                    .map(d -> new Case.Option(Tuple.ofPatterns(d.params()), d.value()))
                                    .toList()
                    ))
                );
            }
            return null;
        }

        @Override
        public String name() {
            return "(1) Transform function to 'case'";
        }
    };

    Rule SPLIT_LAMBDA_PARAMS_2 = new Rule() {
        @Override
        public @Nullable Expression tryApply(@NotNull Expression expr, Expression root) {
            if(expr instanceof Lambda l && l.params().size() > 1) {
                Lambda res = new Lambda(List.of(l.params().getLast()), l.expr());
                for(int i=l.params().size()-2; i>=0; i--)
                    res = new Lambda(List.of(l.params().get(i)), res);
                return res;
            }
            return null;
        }

        @Override
        public String name() {
            return "(2) Split lambda parameters";
        }
    };

    Rule LAMBDA_TO_CASE_3 = new Rule() {
        @Override
        public @Nullable Expression tryApply(@NotNull Expression expr, Expression root) {
            if(expr instanceof Lambda l && l.params().size() == 1 && !(l.params().get(0) instanceof Variable)) {
                Variable var = new Variable(Rule.newVariableGroup(expr, root));
                return new Lambda(
                        List.of(var),
                        new Case(
                                var,
                                new Case.Option(
                                        Tuple.ofPatterns(l.params()),
                                        expr
                                )
                        )
                );
            }
            return null;
        }

        @Override
        public String name() {
            return "(3) Transform lambda to 'case'";
        }
    };

    Rule CASE_TO_MATCH_4 = new Rule() {
        @Override
        public @Nullable Expression tryApply(@NotNull Expression expr, Expression root) {
            if(expr instanceof Case c) {
                Match res = new Match(
                        c.options().getLast().pattern(),
                        c.argument(),
                        c.options().getLast().value(),
                        Bot.INSTANCE
                );
                for(int i=c.options().size()-2; i>=0; i--) {
                    res = new Match(
                            c.options().get(i).pattern(),
                            c.argument(),
                            c.options().get(i).value(),
                            res
                    );
                }
                return res;
            }
            return null;
        }

        @Override
        public String name() {
            return "(4) Transform 'case' to 'match'";
        }
    };

    Rule MATCH_VAR_5 = new Rule() {
        @Override
        public @Nullable Expression tryApply(@NotNull Expression expr, Expression root) {
            if(expr instanceof Match m && m.pattern() instanceof Variable v) {
                return new Call(new Lambda(List.of(v), m.ifMatch()), m.input());
            }
            return null;
        }

        @Override
        public String name() {
            return "(5) Variable pattern always matches";
        }
    };

    Rule MATCH_JOKER_PATTERN_6 = new Rule() {
        @Override
        public @Nullable Expression tryApply(@NotNull Expression expr, Expression root) {
            if(expr instanceof Match m && m.pattern() == JokerPattern.INSTANCE) {
                return m.ifMatch();
            }
            return null;
        }

        @Override
        public String name() {
            return "(6) Joker pattern always matches";
        }
    };

    Rule MATCH_CONSTRUCTORS_7 = new Rule() {
        @Override
        public @Nullable Expression tryApply(@NotNull Expression expr, Expression root) {
            if(expr instanceof Match m && m.pattern() instanceof DataCtor c && c.args().stream().allMatch(Pattern.class::isInstance)) {
                return new IfThenElse(
                    new IsA(c.name(), m.input()),
                    new Match(
                            Tuple.ofPatterns(c.args().stream().map(Pattern.class::cast).toList()),
                            new ArgOf(c.name(), m.input()),
                            m.ifMatch(),
                            m.ifNoMatch()
                    ),
                    m.ifNoMatch()
                );
            }
            return null;
        }

        @Override
        public String name() {
            return "(7) Match type constructor";
        }
    };

    Rule MATCH_EMPTY_TUPLE_8 = new Rule() {
        @Override
        public @Nullable Expression tryApply(@NotNull Expression expr, Expression root) {
            if(expr instanceof Match m && m.pattern() instanceof Tuple t && t.fields().isEmpty()) {
                return new IfThenElse(
                        new IsA("0_tuple", m.input()),
                        m.ifMatch(),
                        m.ifNoMatch()
                );
            }
            return null;
        }

        @Override
        public String name() {
            return "(8) Match empty tuple";
        }
    };

    Rule MATCH_NON_EMPTY_TUPLES_9 = new Rule() {
        @Override
        public @Nullable Expression tryApply(@NotNull Expression expr, Expression root) {
            if(expr instanceof Match m && m.pattern() instanceof Tuple t && !t.fields().isEmpty()) {
                Match match = new Match(
                        (Pattern) t.fields().getLast(),
                        t.get(t.fields().size() - 1),
                        m.ifMatch(),
                        m.ifNoMatch()
                );
                for(int i=t.fields().size()-2; i>=0; i--) {
                    match = new Match(
                            (Pattern) t.fields().get(i),
                            t.get(i),
                            match,
                            m.ifNoMatch()
                    );
                }
                return new IfThenElse(
                    new IsA(t.fields().size()+"_tuple", t),
                    match,
                    m.ifNoMatch()
                );
            }
            return null;
        }

        @Override
        public String name() {
            return "(9) Match non-empty tuple";
        }
    };

    Rule SPLIT_LETS_10 = new Rule() {
        @Override
        public @Nullable Expression tryApply(@NotNull Expression expr, Expression root) {
            if(expr instanceof Let l && l.defs().size() > 1) {
                Let let = new Let(List.of(l.defs().getLast()), l.in());
                for(int i=l.defs().size()-2; i>=0; i--)
                    let = new Let(List.of(l.defs().get(i)), let);
                return let;
            }
            return null;
        }

        @Override
        public String name() {
            return "(10) Split multiple declarations in 'let'";
        }
    };

    Rule ISA_0_TUPLE_ALWAYS_TRUE_i = new Rule() {
        @Override
        public @Nullable Expression tryApply(@NotNull Expression expr, Expression root) {
            if(expr instanceof IfThenElse i && i.condition() instanceof IsA isA && isA.type().equals("0_tuple")) {
                return i.ifTrue();
            }
            return null;
        }

        @Override
        public String name() {
            return "(i) Simplify isa_0_tuple _ to True";
        }
    };

    Rule EVAL_LAMBDA_ii = new Rule() {
        @Override
        public @Nullable Expression tryApply(@NotNull Expression expr, Expression root) {
            if(expr instanceof Call c && c.function() instanceof Lambda l && l.params().size() == 1 && l.params().get(0) instanceof Variable v) {
                return l.expr().substituteVar(v.name(), c.arg());
            }
            return null;
        }

        @Override
        public String name() {
            return "(ii) Evaluate lambda";
        }
    };

    Rule SIMPLIFY_NESTED_IFS_THEN_CASE_iii = new Rule() {
        @Override
        public @Nullable Expression tryApply(@NotNull Expression expr, Expression root) {
            if(expr instanceof IfThenElse i) {
                BoolWrapper any = new BoolWrapper(false);
                Expression e = ifSimplifyStep(i.ifTrue(), any, i.condition(), IfThenElse::ifTrue);
                if(any.value)
                    return new IfThenElse(i.condition(), e, i.ifFalse());
            }
            return null;
        }

        @Override
        public String name() {
            return "(iii) Simplify nested if statement's 'then' case";
        }
    };

    Rule SIMPLIFY_NESTED_IFS_ELSE_CASE_iii = new Rule() {
        @Override
        public @Nullable Expression tryApply(@NotNull Expression expr, Expression root) {
            if(expr instanceof IfThenElse i) {
                BoolWrapper any = new BoolWrapper(false);
                Expression e = ifSimplifyStep(i.ifFalse(), any, i.condition(), IfThenElse::ifFalse);
                if(any.value)
                    return new IfThenElse(i.condition(), i.ifTrue(), e);
            }
            return null;
        }

        @Override
        public String name() {
            return "(iii) Simplify nested if statement's 'else' case";
        }
    };

    private static Expression ifSimplifyStep(Expression expr, BoolWrapper any, Expression condition, Function<IfThenElse, Expression> selected) {
        if(any.value)
            return expr;

        if(expr instanceof IfThenElse i && condition.equals(i.condition())) {
            any.value = true;
            return selected.apply(i);
        }

        return expr.map(Mapper.unchecked(e -> ifSimplifyStep(e, any, condition, selected)));
    }



    @Nullable
    Expression tryApply(@NotNull Expression expr, Expression root);

    String name();

    @Override
    default Object toJson() {
        return name();
    }

    private static String newVariableGroup(Expression expr, Expression root) {
        List<String> allOccupied = new ArrayList<>();
        addNamesInScope(root, expr, false, allOccupied);
        Set<String> occupiedGroups = allOccupied.stream().map(n -> n.replaceFirst("_.*", "")).collect(Collectors.toSet());

        for(int i=0; i<26; i++) {
            String var = "" + (char) ('a' + (i+23)%26);
            if(!occupiedGroups.contains(var))
                return var;
        }
        for(int i=1;; i++)
            if(!occupiedGroups.contains("x"+i))
                return "x"+i;
    }

    private static boolean addNamesInScope(Expression expr, Expression scope, boolean inner, Collection<String> out) {
        BoolWrapper anyInner = new BoolWrapper(false);
        expr.forEach(e -> anyInner.value |= addNamesInScope(e, scope, inner || expr == scope, out));
        if(!inner && expr != scope && !anyInner.value)
            return false;
        out.addAll(expr.names());
        return true;
    }
}
