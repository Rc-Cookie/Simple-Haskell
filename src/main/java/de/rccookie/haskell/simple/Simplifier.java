package de.rccookie.haskell.simple;

import java.util.ArrayList;
import java.util.List;

import de.rccookie.haskell.Expression;
import de.rccookie.haskell.Mapper;
import de.rccookie.util.T2;
import de.rccookie.util.Tuples;
import de.rccookie.util.Wrapper;

public final class Simplifier {

    private Simplifier() { }


    public static Simplification simplify(Expression expr, List<Rule> rules) {
        Expression in = expr;
        List<Simplification.RuleApplication> applications = new ArrayList<>();

        while(true) {
            Wrapper<T2<Expression, Expression>> simplified = new Wrapper<>(null);
            Expression before = expr;
            Rule appliedRule = null;
            for(Rule rule : rules) {
                expr = simplifyStep(expr, simplified, rule, expr);
                if(simplified.value != null) {
                    appliedRule = rule;
                    break;
                }
            }
            if(simplified.value == null)
                break;
            applications.add(new Simplification.RuleApplication(appliedRule, 1, before, expr, simplified.value.a, simplified.value.b));
        }

        return new Simplification(in, applications);
    }

    private static Expression simplifyStep(Expression expr, Wrapper<T2<Expression, Expression>> simplified, Rule rule, Expression root) {
        if(simplified.value != null)
            return expr;

        Expression simpler = rule.tryApply(expr, root);
        if(simpler != null) {
            simplified.value = Tuples.t(expr, simpler);
            return simpler;
        }

        return expr.map(Mapper.unchecked(e -> simplifyStep(e, simplified, rule, root)));
    }
}
