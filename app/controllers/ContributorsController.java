package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import javax.inject.Inject;
import play.mvc.*;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CompletableFuture;

public class ContributorsController extends Controller {
    private HttpExecutionContext ec;

    @Inject
    public ContributorsController(HttpExecutionContext ec) {
        this.ec = ec;
    }

    public CompletionStage<Result> index(Http.Request request, String organization) {
        return CompletableFuture.completedStage(organization).thenApplyAsync(org -> {
            return ok(Json.toJson(org));
        }, ec.current());
    }
}
