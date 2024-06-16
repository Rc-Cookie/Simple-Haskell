package de.rccookie.haskell;

public final class JokerPattern implements Pattern {

    public static final JokerPattern INSTANCE = new JokerPattern();

    private JokerPattern() { }

    @Override
    public Pattern map(Mapper mapper) {
        return this;
    }

    @Override
    public String toString() {
        return "_";
    }
}
