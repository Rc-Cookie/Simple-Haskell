package de.rccookie.haskell;

public record IsA(String type, Expression expr) implements Expression {
    @Override
    public Expression map(Mapper mapper) {
        return new IsA(type, mapper.map(expr));
    }

    @Override
    public String toString() {
        return "(isa_"+type+" "+expr+")";
    }
}
