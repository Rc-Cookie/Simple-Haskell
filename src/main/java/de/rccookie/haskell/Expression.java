package de.rccookie.haskell;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.diogonunes.jcolor.Attribute;
import de.rccookie.graph.HashTree;
import de.rccookie.graph.Tree;
import de.rccookie.haskell.format.Formatter;
import de.rccookie.json.JsonObject;
import de.rccookie.json.JsonSerializable;
import de.rccookie.util.Console;
import de.rccookie.util.ListStream;

public interface Expression extends JsonSerializable {

    Expression map(Mapper mapper);

    default List<String> names() {
        return List.of();
    }

    default String toString(Expression highlight, Attribute... attributes) {
        if(highlight == null)
            return toString();
        String highlightStr = highlight.toString();

        String str = toString();
        int index = str.lastIndexOf(highlightStr);
        return str.substring(0, index) + Console.colored(highlightStr, attributes) + str.substring(index + highlightStr.length());
    }

    default String toFormattedString() {
        return toFormattedString(null);
    }

    default String toFormattedString(Expression highlight, Attribute... attributes) {
        return Formatter.format(this.toString(highlight, attributes)).toString();
    }

    default void forEach(Consumer<? super Expression> action) {
        map(Mapper.unchecked(e -> {
            action.accept(e);
            return e;
        }));
    }

    default void forEachRecursive(Consumer<? super Expression> action) {
        forEach(e -> {
            action.accept(e);
            e.forEachRecursive(action);
        });
    }

    default Expression substituteVar(String name, Expression value) {
        return map(Mapper.unchecked(e -> {
            if(e instanceof Variable && ((Variable) e).name().equals(name))
                return value;
            return e.substituteVar(name, value);
        }));
    }

    default Tree<Expression,Integer> toTree() {
        Tree<Expression, Integer> tree = new HashTree<>();
        addToTree(this, tree, null);
        return tree;
    }

    default ListStream<Expression> stream() {
        List<Expression> list = new ArrayList<>();
        forEachRecursive(list::add);
        return ListStream.of(list);
    }

    @Override
    default Object toJson() {
        return new JsonObject(
                "compact", toString(),
                "formatted", toFormattedString()
        );
    }

    private static void addToTree(Expression expr, Tree<Expression, Integer> tree, Expression parent) {
        tree.add(expr, parent, 1);
        expr.forEach(e -> addToTree(e, tree, expr));
    }
}
