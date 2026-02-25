package handler;

import com.google.gson.Gson;
import service.UserService;
import service.request.RegisterRequest;
import service.result.RegisterResult;

public class RegisterHandler
        extends AbsBaseHandler<RegisterRequest, RegisterResult> {

    private final UserService userService;

    public RegisterHandler(Gson gson, UserService userService){
        super(gson, RegisterRequest.class);
        this.userService = userService;
    }

    @Override
    public RegisterResult handleRequest(RegisterRequest request) {
        return userService.register(request);
    }

    @Override
    protected RegisterResult invalidJsonResponse() {
        return new RegisterResult(null, null, "Invalid JSON");
    }

    @Override
    protected RegisterResult internalError(Exception e) {
        return new RegisterResult(null, null, "Internal Server Error");
    }

}
