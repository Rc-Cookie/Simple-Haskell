package de.rccookie.haskell.parse;

public record Name(String value) implements Literal {
    @Override
    public String toString() {
        return '"' + value.replace("\\", "\\\\").replace("\"", "\\\"") + '"';
    }
}
