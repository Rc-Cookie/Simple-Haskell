package de.rccookie.haskell;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public record Lambda(List<? extends Pattern> params, Expression expr) implements Expression {
    @Override
    public Lambda map(Mapper mapper) {
        return new Lambda(
                params.stream().map(mapper::map).toList(),
                mapper.map(expr)
        );
    }

    @Override
    public String toString() {
        return "(\\"+params.stream().map(Objects::toString).collect(Collectors.joining(" "))+" -> "+expr+")";
    }

    @Override
    public Expression substituteVar(String name, Expression value) {
        for(Pattern p : params) {
            if(p.stream().anyMatch(e -> e instanceof Variable v && v.name().equals(name)))
                return this;
        }
        return Expression.super.substituteVar(name, value);
    }
}
