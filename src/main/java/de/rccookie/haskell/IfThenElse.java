package de.rccookie.haskell;

import org.jetbrains.annotations.Nullable;

public record IfThenElse(Expression condition, Expression ifTrue, @Nullable Expression ifFalse) implements Expression {
    @Override
    public Expression map(Mapper mapper) {
        return new IfThenElse(
                mapper.map(condition),
                mapper.map(ifTrue),
                ifFalse != null ? mapper.map(ifFalse) : null
        );
    }

    @Override
    public String toString() {
        return "(if "+condition+" then "+ifTrue+(ifFalse != null ? " else "+ifFalse : "")+")";
    }
}
