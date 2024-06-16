package de.rccookie.haskell.format;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Formatter {

    private Formatter() { }

    public static FormatSection format(String str) {
        FormatSection s = parseOpenPair(new StringBuilder(str));
        while(s instanceof ParenthesisPair p)
            s = p.content();
        return s;
    }

    public static FormatSection format(Object o) {
        return format(Objects.toString(o));
    }

    private static ParenthesisPair parsePair(StringBuilder str) {
        str.deleteCharAt(0);
        FormatSection section = parseOpenPair(str);
        str.deleteCharAt(0);
        return section instanceof ParenthesisPair pp ? pp : new ParenthesisPair(section);
    }

    private static FormatSection parseOpenPair(StringBuilder str) {
        List<FormatSection> sections = new ArrayList<>();
        while(!str.isEmpty()) {
            int open = Math.min(str.indexOf("(") & Integer.MAX_VALUE, str.indexOf("{") & Integer.MAX_VALUE);
            int close = Math.min(str.indexOf(")") & Integer.MAX_VALUE, str.indexOf("}") & Integer.MAX_VALUE);
            if(open == Integer.MAX_VALUE && close == Integer.MAX_VALUE) {
                sections.add(new WithoutParenthesis(str.toString()));
                break;
            }
            sections.add(new WithoutParenthesis(str.substring(0, Math.min(open, close)).replaceAll(";\\s*(.)", ";\n$1")));
            str.delete(0, Math.min(open, close));
            if(close < open)
                break;
            sections.add(parsePair(str));
        }
        sections.removeIf(s -> s instanceof WithoutParenthesis w && w.content().isEmpty());
        if(sections.size() == 1 && sections.get(0) instanceof ParenthesisPair p)
            return p;
        return new SectionList(sections);
    }
}
