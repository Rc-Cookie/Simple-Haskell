package de.rccookie.haskell;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public record FunctionDecl(List<Declaration> declarations) implements Expression {
    public FunctionDecl(Declaration... declarations) {
        this(List.of(declarations));
    }

    @Override
    public Expression map(Mapper mapper) {
        return new FunctionDecl(
                declarations.stream().map(mapper::map).toList()
        );
    }

    @Override
    public String toString() {
        return declarations.stream().map(Objects::toString).collect(Collectors.joining("; "));
    }
}
