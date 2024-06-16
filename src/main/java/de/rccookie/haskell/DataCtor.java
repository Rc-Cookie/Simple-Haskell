package de.rccookie.haskell;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public record DataCtor(String name, List<? extends Expression> args) implements Pattern {

    public static final DataCtor True = new DataCtor("True");
    public static final DataCtor False = new DataCtor("False");

    public DataCtor(String name, Pattern... args) {
        this(name, List.of(args));
    }

    @Override
    public DataCtor map(Mapper mapper) {
        return new DataCtor(name, args.stream().map(mapper::map).toList());
    }

    @Override
    public String toString() {
        if(args.isEmpty())
            return name;
        return "("+name+" "+args.stream().map(Objects::toString).collect(Collectors.joining(" "))+")";
    }

    @Override
    public List<String> names() {
        return List.of(name);
    }
}
