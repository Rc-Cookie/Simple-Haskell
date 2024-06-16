package de.rccookie.haskell;

import java.util.List;

public class Bot implements Expression {

    public static final Bot INSTANCE = new Bot();

    private Bot() { }

    @Override
    public Expression map(Mapper mapper) {
        return this;
    }

    @Override
    public String toString() {
        return "bot";
    }

    @Override
    public List<String> names() {
        return List.of("bot");
    }
}
