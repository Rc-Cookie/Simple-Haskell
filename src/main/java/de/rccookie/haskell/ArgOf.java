package de.rccookie.haskell;

public record ArgOf(String type, Expression expr) implements Expression {
    @Override
    public Expression map(Mapper mapper) {
        return new ArgOf(type, mapper.map(expr));
    }

    @Override
    public String toString() {
        return "(argof_"+type+" "+expr+")";
    }
}
