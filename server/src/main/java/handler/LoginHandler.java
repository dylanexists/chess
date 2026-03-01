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
    public LoginResult runRequestSpecificService(LoginRequest request) {
        return userService.login(request);
    }

    @Override
    protected LoginResult invalidJsonResponse() {
        return new LoginResult(null, null, "Error: bad request");
    }

    @Override
    protected LoginResult internalError(Exception e) {
        String errorDesc = (e.getMessage() == null)
                ? "Internal error (no error message given)"
                : e.getMessage();
        return new LoginResult(null, null, "Error: " + errorDesc);
    }

}
