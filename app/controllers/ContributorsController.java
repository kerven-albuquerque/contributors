package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import javax.inject.Inject;
import play.mvc.*;
import play.libs.ws.*;
import play.libs.ws.WSBodyReadables;
import play.libs.ws.ahc.*;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CompletableFuture;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.time.temporal.ChronoUnit;
import java.time.Duration;

public class ContributorsController extends Controller {
    private final HttpExecutionContext ec;
    private final WSClient ws;

    @Inject
    public ContributorsController(HttpExecutionContext ec, WSClient ws) {
        this.ec = ec;
        this.ws = ws;
    }

    public CompletionStage<Result> index(Http.Request request, String organization) {
        return ws.url("https://api.github.com/orgs/github/repos").get().thenApplyAsync(response -> {
            JsonNode jsonRepos = response.getBody(WSBodyReadables.instance.json());
            Map<String, Integer> contributions = new HashMap<String, Integer>();

            CompletableFuture[] repos = new CompletableFuture[jsonRepos.size()];
            for (int i = 0; i < jsonRepos.size(); i++)
                repos[i] = ws.url(jsonRepos.get(i).get("contributors_url").asText())
                        .setRequestTimeout(Duration.of(1000, ChronoUnit.MILLIS)).get().thenApplyAsync(repoContrib -> {
                            JsonNode jsonContrib = repoContrib.getBody(WSBodyReadables.instance.json());
                            jsonContrib.forEach(userContrib -> {
                                String name = userContrib.get("login").asText();
                                Integer contrib = userContrib.get("contributions").asInt()
                                        + contributions.getOrDefault(name, 0);
                                contributions.put(name, contrib);
                            });
                            return null;
                        }, ec.current()).exceptionally(result -> null).toCompletableFuture();

            CompletableFuture.allOf(repos).join();

            List<ObjectNode> contributors = new ArrayList<ObjectNode>();
            contributions.forEach((key, value) -> {
                ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
                objectNode.put("name", key);
                objectNode.put("contributions", value);
                contributors.add(objectNode);
            });
            Collections.sort(contributors, new Comparator<ObjectNode>() {
                @Override
                public int compare(ObjectNode o1, ObjectNode o2) {
                    return o2.get("contributions").asInt() - o1.get("contributions").asInt();
                }
            });
            return ok(Json.toJson(contributors));

        }, ec.current()).exceptionally(result -> {
            ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
            objectNode.put("message", "API limit exceeded");
            return forbidden(Json.toJson(objectNode));
        });
    }
}
