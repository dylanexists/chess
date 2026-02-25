package handler;

import com.google.gson.Gson;
import service.UserService;
import service.request.LogoutRequest;
import service.result.LogoutResult;

public class LogoutHandler
        extends AbsBaseHandler<LogoutRequest, LogoutResult> {

    private final UserService userService;

    public LogoutHandler(Gson gson, UserService userService){
        super(gson, LogoutRequest.class);
        this.userService = userService;
    }

    @Override
    public LogoutResult handleRequest(LogoutRequest request) {
        return userService.logout(request);
    }

    @Override
    protected LogoutResult invalidJsonResponse() {
        return new LogoutResult("Invalid JSON");
    }

    @Override
    protected LogoutResult internalError(Exception e) {
        return new LogoutResult("Internal Server Error");
    }
}
