package server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dataaccess.MemoryAuthDao;
import dataaccess.MemoryGameDao;
import dataaccess.MemoryUserDao;
import handler.*;
import io.javalin.*;
import service.GameService;
import service.UserService;
import service.request.ClearRequest;
import service.result.*;

import java.util.List;
import java.util.Map;

public class Server {

    private final Javalin javalin;

    public Server() {
        Gson gson = new Gson();
        var userDao = new MemoryUserDao();
        var authDao = new MemoryAuthDao();
        var gameDao = new MemoryGameDao();
        var userService = new UserService(userDao, authDao);
        var gameService = new GameService(gameDao, authDao);
        var registerHandler = new RegisterHandler(gson, userService);
        var loginHandler = new LoginHandler(gson, userService);
        var logoutHandler = new LogoutHandler(gson, userService);
        var createGameHandler = new CreateGameHandler(gson, gameService);
        var listGamesHandler = new ListGamesHandler(gson, gameService);
        var joinGameHandler = new JoinGameHandler(gson, gameService);
        var clearHandler = new ClearHandler(gson, userService, gameService);

        // Register your endpoints and exception handlers here.
        javalin = Javalin.create(config -> config.staticFiles.add("web"))
                .post("/user", ctx -> {
                    String input = ctx.body();
                    RegisterResult result = registerHandler.handle(input);
                    String resultJson = registerHandler.serialize(result);
                    if (result.username() != null && result.authToken() != null && result.message() == null) {
                        ctx.status(200);
                    } else if (result.message().equals("Error: already taken")) {
                        ctx.status(403);
                    } else if (result.message().equals("Error: bad request")) {
                        ctx.status(400);
                    } else {
                        ctx.status(500);
                    }
                    ctx.contentType("application/json");
                    ctx.result(resultJson);

                })
                .post("/session", ctx -> {
                    String input = ctx.body();
                    LoginResult result = loginHandler.handle(input);
                    String resultJson = loginHandler.serialize(result);
                    if (result.username() != null && result.authToken() != null && result.message() == null) {
                        ctx.status(200);
                    } else if (result.message().equals("Error: unauthorized")) {
                        ctx.status(401);
                    } else if (result.message().equals("Error: bad request")) {
                        ctx.status(400);
                    } else {
                        ctx.status(500);
                    }
                    ctx.contentType("application/json");
                    ctx.result(resultJson);
                })
                .delete("/session", ctx -> {
                    String header = ctx.header("Authorization");
                    String input = logoutHandler.headerStringToJson("authToken", header);
                    LogoutResult result = logoutHandler.handle(input);
                    String resultJson = logoutHandler.serialize(result);
                    if (result.message() == null) {
                        ctx.status(200);
                    } else if (result.message().equals("Error: unauthorized")) {
                        ctx.status(401);
                    } else if (result.message().equals("Error: bad request")) {
                        ctx.status(400);
                    } else {
                        ctx.status(500);
                    }
                    ctx.contentType("application/json");
                    ctx.result(resultJson);
                })
                .post("/game", ctx -> { //pull inputs from body too
                    String header = ctx.header("Authorization");
                    String headerInput = createGameHandler.headerStringToJson("authToken", header);
                    String bodyInput = ctx.body();
                    String combinedInput = createGameHandler.combineHeaderAndBodyJson(headerInput, bodyInput);
                    CreateGameResult result = createGameHandler.handle(combinedInput);
                    String resultJson = createGameHandler.serialize(result);
                    if (result.message() == null) {
                        ctx.status(200);
                    } else if (result.message().equals("Error: unauthorized")) {
                        ctx.status(401);
                    } else if (result.message().equals("Error: bad request")) {
                        ctx.status(400);
                    } else {
                        ctx.status(500);
                    }
                    ctx.contentType("application/json");
                    ctx.result(resultJson);
                })
                .get("/game", ctx -> {
                    String header = ctx.header("Authorization");
                    String headerInput = listGamesHandler.headerStringToJson("authToken", header);
                    ListGamesResult result = listGamesHandler.handle(headerInput);
                    String resultJson = listGamesHandler.serialize(result);
                    if (result.message() == null) {
                        ctx.status(200);
                    } else if (result.message().equals("Error: unauthorized")) {
                        ctx.status(401);
                    } else if (result.message().equals("Error: bad request")) {
                        ctx.status(400);
                    } else {
                        ctx.status(500);
                    }
                    ctx.contentType("application/json");
                    ctx.result(resultJson);
                })
                .put("/game", ctx -> {
                    String header = ctx.header("Authorization");
                    String headerInput = joinGameHandler.headerStringToJson("authToken", header);
                    String bodyInput = ctx.body();
                    String combinedInput = joinGameHandler.combineHeaderAndBodyJson(headerInput, bodyInput);
                    JoinGameResult result = joinGameHandler.handle(combinedInput);
                    String resultJson = joinGameHandler.serialize(result);
                    if (result.message() == null) {
                        ctx.status(200);
                    } else if (result.message().equals("Error: already taken")) {
                        ctx.status(403);
                    } else if (result.message().equals("Error: unauthorized")) {
                        ctx.status(401);
                    } else if (result.message().equals("Error: bad request")) {
                        ctx.status(400);
                    } else {
                        ctx.status(500);
                    }
                    ctx.contentType("application/json");
                    ctx.result(resultJson);
                })
                .delete("/db", ctx -> {
                    //ClearResult result = userService.clear();
                    //gameService.clear();
                    //String resultJson = new Gson().toJson(result);
                    ClearResult result = clearHandler.handle("");
                    String resultJson = clearHandler.serialize(result);
                    ctx.contentType("application/json");
                    ctx.result(resultJson);
                })
                ;
    }



    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
