package de.rccookie.haskell.format;

public record WithoutParenthesis(String content) implements FormatSection {
    @Override
    public String toString() {
        return content;
    }
}
