package de.rccookie.haskell;

public record Call(Expression function, Expression arg) implements Expression {
    @Override
    public Call map(Mapper mapper) {
        return new Call(
                mapper.map(function),
                mapper.map(arg)
        );
    }

    @Override
    public String toString() {
        return "("+function+" "+arg+")";
    }
}
