package server;

import com.google.gson.Gson;
import dataaccess.MemoryAuthDao;
import dataaccess.MemoryUserDao;
import handler.RegisterHandler;
import io.javalin.*;
import service.UserService;
import service.result.ClearResult;
import service.result.RegisterResult;

public class Server {

    private final Javalin javalin;

    public Server() {
        Gson gson = new Gson();
        var userDao = new MemoryUserDao();
        var authDao = new MemoryAuthDao();
        var userService = new UserService(userDao, authDao);
        var registerHandler = new RegisterHandler(gson, userService);

        // Register your endpoints and exception handlers here.
        javalin = Javalin.create(config -> config.staticFiles.add("web"))
                .post("/user", ctx -> {
                    String body = ctx.body();
                    String resultJson = registerHandler.handle(body);
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
