package de.rccookie.haskell;

public interface Pattern extends Expression {

    @Override
    Pattern map(Mapper mapper);
}
