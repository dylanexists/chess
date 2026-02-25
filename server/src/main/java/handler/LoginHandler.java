package handler;

import com.google.gson.Gson;
import service.UserService;
import service.request.LoginRequest;
import service.result.LoginResult;

public class LoginHandler
        extends AbsBaseHandler<LoginRequest, LoginResult> {

    private final UserService userService;

    public LoginHandler(Gson gson, UserService userService){
        super(gson, LoginRequest.class);
        this.userService = userService;
    }

    @Override
    public LoginResult handleRequest(LoginRequest request) {
        //return userService.login(request);
        return new LoginResult("filler", "filler", null);
    }

    @Override
    protected LoginResult invalidJsonResponse() {
        return new LoginResult(null, null, "Invalid JSON");
    }

    @Override
    protected LoginResult internalError(Exception e) {
        return new LoginResult(null, null, "Internal Server Error");
    }

}
