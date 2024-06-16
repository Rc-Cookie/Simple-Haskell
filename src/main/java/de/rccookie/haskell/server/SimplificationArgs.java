package de.rccookie.haskell.server;

import java.util.List;

import de.rccookie.json.Default;
import org.jetbrains.annotations.NotNull;

public record SimplificationArgs(@NotNull String ast,
                                 @Default("[]") List<String> rules) {
}
