package de.rccookie.haskell;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public record Let(List<Expression> defs, Expression in) implements Expression {
    @Override
    public Let map(Mapper mapper) {
        return new Let(defs.stream().map(mapper::map).toList(), mapper.map(in));
    }

    @Override
    public String toString() {
        return "let "+defs.stream().map(Objects::toString).collect(Collectors.joining(", "))+" in "+in;
    }
}
