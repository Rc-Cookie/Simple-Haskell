package de.rccookie.haskell;

public record Sel(int tupleSize, int index, Expression expr) implements Expression {
    @Override
    public Expression map(Mapper mapper) {
        return new Sel(
                tupleSize,
                index,
                mapper.map(expr)
        );
    }

    @Override
    public String toString() {
        return "(sel"+tupleSize+","+index+" "+expr+")";
    }
}
