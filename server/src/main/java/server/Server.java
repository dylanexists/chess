package server;

import com.google.gson.Gson;
import dataaccess.MemoryAuthDao;
import dataaccess.MemoryUserDao;
import handler.LoginHandler;
import handler.LogoutHandler;
import handler.RegisterHandler;
import io.javalin.*;
import service.UserService;
import service.result.ClearResult;
import service.result.LoginResult;
import service.result.LogoutResult;
import service.result.RegisterResult;

import java.util.Map;

public class Server {

    private final Javalin javalin;

    public Server() {
        Gson gson = new Gson();
        var userDao = new MemoryUserDao();
        var authDao = new MemoryAuthDao();
        var userService = new UserService(userDao, authDao);
        var registerHandler = new RegisterHandler(gson, userService);
        var loginHandler = new LoginHandler(gson, userService);
        var logoutHandler = new LogoutHandler(gson, userService);

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
                .delete("/db", ctx -> {
                    ClearResult result = userService.clear();
                    String resultJson = new Gson().toJson(result);
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
