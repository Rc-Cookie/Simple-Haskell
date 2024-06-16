package de.rccookie.haskell;

import java.util.function.UnaryOperator;

public interface Mapper {
    <E extends Expression> E map(E expr);


    static Mapper unchecked(UnaryOperator<Expression> mapper) {
        return new Mapper() {
            @SuppressWarnings("unchecked")
            @Override
            public <E extends Expression> E map(E expr) {
                return (E) mapper.apply(expr);
            }
        };
    }
}
