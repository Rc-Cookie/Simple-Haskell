package de.rccookie.haskell.parse;

import java.util.ArrayList;
import java.util.List;

import de.rccookie.haskell.Expression;
import de.rccookie.haskell.Variable;

public final class Parser {

    private Parser() { }

    private static Literal lexer(String str) {
        StringBuilder buf = new StringBuilder(str.replaceAll("\\s", ""));
        Literal l = nextLiteral(buf);
        if(!buf.isEmpty())
            throw new SyntaxException("Trailing stuff: "+buf);
        return l;
    }

    private static Literal nextLiteral(StringBuilder str) {
        if(str.isEmpty())
            throw new SyntaxException("Reached end of file during parsing");
        return str.charAt(0) == '"' ? nextName(str) : nextCtor(str);
    }

    private static Name nextName(StringBuilder str) {
        str.deleteCharAt(0);
        StringBuilder name = new StringBuilder();
        while(!str.isEmpty()) {
            char c = str.charAt(0);
            str.deleteCharAt(0);
            if(c == '"') {
                if(name.isEmpty())
                    throw new SemanticException("Empty string");
                return new Name(name.toString());
            }
            if(c == '\\') {
                if(str.isEmpty()) break;
                c = str.charAt(0);
                str.deleteCharAt(0);
            }
            name.append(c);
        }
        throw new SyntaxException("Non-terminated string");
    }

    private static Constructor nextCtor(StringBuilder str) {
        StringBuilder name = new StringBuilder();
        while(!str.isEmpty()) {
            char c = str.charAt(0);
            if(c == '(' || c == ')' || c == ',')
                break;
            str.deleteCharAt(0);
            name.append(c);
        }
        String nameStr = name.isEmpty() ? "tuple" : name.toString();
        if(str.isEmpty() || str.charAt(0) != '(') {
            if(name.isEmpty())
                throw new SyntaxException("Statement missing");
            return new Constructor(nameStr, List.of());
        }

        str.deleteCharAt(0);
        if(!str.isEmpty() && str.charAt(0) == ')') {
            str.deleteCharAt(0);
            return new Constructor(nameStr, List.of());
        }
        List<Literal> arguments = new ArrayList<>();
        while(!str.isEmpty()) {
            if(str.charAt(0) == ')') {
                str.deleteCharAt(0);
                return new Constructor(nameStr, arguments);
            }
            arguments.add(nextLiteral(str));
            if(str.isEmpty()) break;
            if(str.charAt(0) == ',')
                str.deleteCharAt(0);
        }
        throw new SyntaxException("Mismatched parenthesis: missing closing parenthesis");
    }

    public static Expression parse(String str) {
        Literal l = lexer(str);
        if(l instanceof Constructor c)
            return c.instantiate();
        return new Variable(((Name) l).value());
    }
}
