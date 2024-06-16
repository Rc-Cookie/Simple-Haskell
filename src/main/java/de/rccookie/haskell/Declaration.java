package de.rccookie.haskell;

import java.util.List;
import java.util.stream.Collectors;

public record Declaration(Variable variable, List<Pattern> params, Expression value) implements Expression {
    @Override
    public Declaration map(Mapper mapper) {
        return new Declaration(
                mapper.map(variable),
                params.stream().map(mapper::map).toList(),
                mapper.map(value)
        );
    }

    @Override
    public String toString() {
        return variable+" "+params.stream().map(p -> p+" ").collect(Collectors.joining())+"= "+value;
    }

    @Override
    public Expression substituteVar(String name, Expression value) {
        if(variable.name().equals(name))
            return this;
        return Expression.super.substituteVar(name, value);
    }
}
