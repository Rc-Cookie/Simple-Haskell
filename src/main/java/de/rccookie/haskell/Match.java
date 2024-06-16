package de.rccookie.haskell;

public record Match(Pattern pattern, Expression input, Expression ifMatch, Expression ifNoMatch) implements Expression {

    @Override
    public Expression map(Mapper mapper) {
        return new Match(
                mapper.map(pattern),
                mapper.map(input),
                mapper.map(ifMatch),
                mapper.map(ifNoMatch)
        );
    }

    @Override
    public String toString() {
        return "(match "+pattern+" "+input+" "+ifMatch+" "+ifNoMatch+")";
    }
}
