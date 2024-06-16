package de.rccookie.haskell.format;

import java.util.stream.Collectors;

public record ParenthesisPair(FormatSection content) implements FormatSection {
    @Override
    public String toString() {
        String content = this.content.toString();
        if(content.length() + 2 <= MAX_LINE_LENGTH && !content.contains("\n"))
            return "("+content+")";
        return "(\n"+content.lines().map(l -> INDENT+l+"\n").collect(Collectors.joining())+")";
    }
}
