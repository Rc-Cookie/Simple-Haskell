package de.rccookie.haskell.simple;

import java.util.List;
import java.util.stream.Collectors;

import com.diogonunes.jcolor.Attribute;
import de.rccookie.haskell.Expression;
import de.rccookie.json.JsonObject;
import de.rccookie.json.JsonSerializable;
import de.rccookie.util.Console;
import de.rccookie.util.text.SimpleDiff;

public record Simplification(Expression in, List<RuleApplication> applications) {

    public Expression result() {
        return applications.getLast().result();
    }

    @Override
    public String toString() {
        return toString(new Attribute[0]);
    }

    public String toString(Attribute... attributes) {
        return in.toFormattedString() + "\n" + applications.stream().map(a -> a.toString(attributes)).collect(Collectors.joining("\n"));
    }

    public record RuleApplication(Rule rule, int count, Expression old, Expression result, Expression oldModified, Expression newModified) implements JsonSerializable {

        @Override
        public String toString() {
            return toString(new Attribute[0]);
        }

        public String toString(Attribute... attributes) {
            return "-- " + Console.colored("Rule " + rule.name() + (count != 1 ? " * "+count : ""), Attribute.BOLD()) + " --\n" + result.toFormattedString(attributes.length != 0 ? newModified : null, attributes);
        }

        @Override
        public Object toJson() {
            return new JsonObject(
                    "rule", rule,
                    "count", count,
                    "old", old,
                    "result", result,
                    "oldModified", oldModified,
                    "newModified", newModified,
                    "diff", SimpleDiff.compute(old.toFormattedString(), result.toFormattedString())
            );
        }
    }
}
