package de.rccookie.haskell;

import java.util.List;

public record Variable(String name) implements Pattern {
    @Override
    public Variable map(Mapper mapper) {
        return this;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public List<String> names() {
        return List.of(name);
    }
}
