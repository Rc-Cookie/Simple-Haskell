package de.rccookie.haskell;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public record Tuple(List<? extends Expression> fields) implements Pattern {

    @Override
    public Tuple map(Mapper mapper) {
        return new Tuple(fields.stream().map(mapper::map).toList());
    }

    @Override
    public String toString() {
        if(fields.size() == 1)
            return fields.get(0).toString();
        return "("+fields.stream().map(Objects::toString).collect(Collectors.joining(", "))+")";
    }

    public Sel get(int index) {
        return new Sel(fields.size(), index+1, this);
    }

    public static Expression of(List<? extends Expression> fields) {
        return fields.size() != 1 ? new Tuple(fields) : fields.get(0);
    }

    public static Pattern ofPatterns(List<? extends Pattern> fields) {
        return (Pattern) of(fields);
    }

    public static Expression of(Expression... fields) {
        return of(List.of(fields));
    }

    public static Pattern ofPatterns(Pattern... fields) {
        return (Pattern) of(fields);
    }
}
