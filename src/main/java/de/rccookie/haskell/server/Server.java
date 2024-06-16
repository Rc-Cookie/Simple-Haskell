package de.rccookie.haskell.server;

import java.util.List;

import de.rccookie.haskell.Expression;
import de.rccookie.haskell.parse.Parser;
import de.rccookie.haskell.parse.SemanticException;
import de.rccookie.haskell.parse.SyntaxException;
import de.rccookie.haskell.simple.Rule;
import de.rccookie.haskell.simple.Simplification;
import de.rccookie.haskell.simple.Simplifier;
import de.rccookie.http.HttpRequest;
import de.rccookie.http.Method;
import de.rccookie.http.Route;
import de.rccookie.http.server.HttpProcessor;
import de.rccookie.http.server.HttpRequestFailure;
import de.rccookie.http.server.HttpRequestListener;
import de.rccookie.http.server.HttpServer;
import de.rccookie.http.server.StaticHttpHandler;
import de.rccookie.http.server.annotation.methods.POST;
import de.rccookie.json.JsonObject;
import de.rccookie.util.ArgsParser;
import de.rccookie.util.Options;

public class Server extends HttpServer implements HttpRequestListener, HttpProcessor {

    private static final List<Rule> RULES = List.of(
            Rule.ISA_0_TUPLE_ALWAYS_TRUE_i,
            Rule.EVAL_LAMBDA_ii,
            Rule.SIMPLIFY_NESTED_IFS_THEN_CASE_iii,
            Rule.SIMPLIFY_NESTED_IFS_ELSE_CASE_iii,
            Rule.MATCH_EMPTY_TUPLE_8,
            Rule.FUNC_TO_CASE_1,
            Rule.SPLIT_LAMBDA_PARAMS_2,
            Rule.LAMBDA_TO_CASE_3,
            Rule.CASE_TO_MATCH_4,
            Rule.MATCH_VAR_5,
            Rule.MATCH_JOKER_PATTERN_6,
            Rule.MATCH_CONSTRUCTORS_7,
            Rule.MATCH_NON_EMPTY_TUPLES_9,
            Rule.SPLIT_LETS_10
    );


    public Server() {
        addHandler(this);
        addHandler("/**", new StaticHttpHandler(Route.of("/html"), r -> r.route().isRoot() ? Route.of("/index") : r.route()), Method.GET);
    }

    @POST
    public Simplification simplify(SimplificationArgs args) {
        List<Rule> rules;
        if(args.rules().isEmpty())
            rules = RULES;
        else rules = args.rules()
                .stream()
                .map(r -> RULES.stream()
                        .filter(s -> s.name().startsWith(r) || s.name().startsWith("("+r+")"))
                        .findAny()
                        .orElseThrow(() -> HttpRequestFailure.notFound("Rule not found: "+r))
                )
                .toList();

        Expression expr = Parser.parse(args.ast());
        return Simplifier.simplify(expr, rules);
    }

    @Override
    public void processError(HttpRequest.Received request, Exception exception) throws Exception {
        if(exception instanceof SyntaxException)
            throw HttpRequestFailure.badRequest("Syntax error in AST", new JsonObject("type", "syntax", "message", exception.getMessage()), exception);
        if(exception instanceof SemanticException)
            throw HttpRequestFailure.badRequest("Semantic error in AST", new JsonObject("type", "semantic", "message", exception.getMessage()), exception);
        HttpProcessor.super.processError(request, exception);
    }


    public static void main(String[] args) {
        ArgsParser parser = new ArgsParser();
        parser.addOption('p', "port", true, "Port to listen to");
        Options options = parser.parse(args);

        new Server().listen(options.getIntOr("port", 80));
    }
}
