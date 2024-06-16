package de.rccookie.haskell;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public record Case(Expression argument, List<Option> options) implements Expression {

    public Case(Expression argument, Option... options) {
        this(argument, List.of(options));
    }

    @Override
    public Case map(Mapper mapper) {
        return new Case(
                mapper.map(argument),
                options.stream().map(mapper::map).toList()
        );
    }

    @Override
    public String toString() {
        return "(case "+argument+" of { " + options.stream().map(Objects::toString).collect(Collectors.joining("; ")) + " })";
    }

    public record Option(Pattern pattern, Expression value) implements Expression {
        @Override
        public Expression map(Mapper mapper) {
            return new Option(
                    mapper.map(pattern),
                    mapper.map(value)
            );
        }

        @Override
        public String toString() {
            return pattern+" -> "+value;
        }
    }
}
