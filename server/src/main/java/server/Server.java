package server;

import com.google.gson.Gson;
import dataaccess.*;
import handler.*;
import io.javalin.*;
import result.*;
import server.websocket.WebSocketHandler;
import service.GameService;
import service.UserService;


public class Server {

    private final Javalin javalin;

    public Server() {
        try{
            DatabaseManager.createDatabase();
        } catch (DataAccessException e) {
            System.err.println("Database initialization failed");
        }
        Gson gson = new Gson();
        var userDao = new SQLUserDao();
        var authDao = new SQLAuthDao();
        var gameDao = new SQLGameDao(gson);
        var userService = new UserService(userDao, authDao);
        var gameService = new GameService(gameDao, authDao);
        var webSocketHandler = new WebSocketHandler(authDao);
        var registerHandler = new RegisterHandler(gson, userService);
        var loginHandler = new LoginHandler(gson, userService);
        var logoutHandler = new LogoutHandler(gson, userService);
        var createGameHandler = new CreateGameHandler(gson, gameService);
        var listGamesHandler = new ListGamesHandler(gson, gameService);
        var joinGameHandler = new JoinGameHandler(gson, gameService);
        var clearHandler = new ClearHandler(gson, userService, gameService);

        javalin = Javalin.create(config -> config.staticFiles.add("web"))
                .post("/user", ctx -> {
                    String input = ctx.body();
                    RegisterResult result = registerHandler.handle(input);
                    String resultJson = registerHandler.serialize(result);
                    ctx.status(identifyErrorNum(result.message()));
                    ctx.contentType("application/json");
                    ctx.result(resultJson);

                })
                .post("/session", ctx -> {
                    String input = ctx.body();
                    LoginResult result = loginHandler.handle(input);
                    String resultJson = loginHandler.serialize(result);
                    ctx.status(identifyErrorNum(result.message()));
                    ctx.contentType("application/json");
                    ctx.result(resultJson);
                })
                .delete("/session", ctx -> {
                    String header = ctx.header("Authorization");
                    String input = logoutHandler.headerStringToJson("authToken", header);
                    LogoutResult result = logoutHandler.handle(input);
                    String resultJson = logoutHandler.serialize(result);
                    ctx.status(identifyErrorNum(result.message()));
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
                    ctx.status(identifyErrorNum(result.message()));
                    ctx.contentType("application/json");
                    ctx.result(resultJson);
                })
                .get("/game", ctx -> {
                    String header = ctx.header("Authorization");
                    String headerInput = listGamesHandler.headerStringToJson("authToken", header);
                    ListGamesResult result = listGamesHandler.handle(headerInput);
                    String resultJson = listGamesHandler.serialize(result);
                    ctx.status(identifyErrorNum(result.message()));
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
                    ctx.status(identifyErrorNum(result.message()));
                    ctx.contentType("application/json");
                    ctx.result(resultJson);
                })
                .delete("/db", ctx -> {
                    ClearResult result = clearHandler.handle("");
                    String resultJson = clearHandler.serialize(result);
                    ctx.status(identifyErrorNum(result.message()));
                    ctx.contentType("application/json");
                    ctx.result(resultJson);
                })
                .ws("/ws", ws -> {
                    ws.onConnect(webSocketHandler);
                    ws.onMessage(webSocketHandler);
                    ws.onClose(webSocketHandler);
                })
                ;
    }


    private int identifyErrorNum(String message){
        if (message == null) {
            return 200;
        } else if (message.equals("Error: already taken")) {
           return 403;
        } else if (message.equals("Error: unauthorized")) {
            return 401;
        } else if (message.equals("Error: bad request")) {
            return 400;
        } else {
            return 500;
        }
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
