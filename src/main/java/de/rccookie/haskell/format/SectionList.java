package de.rccookie.haskell.format;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public record SectionList(List<FormatSection> content) implements FormatSection {
    @Override
    public String toString() {
        return content.stream().map(Objects::toString).collect(Collectors.joining());
    }
}
