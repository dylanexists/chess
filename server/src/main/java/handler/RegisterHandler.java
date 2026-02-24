package handler;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import service.UserService;
import service.request.RegisterRequest;
import service.result.RegisterResult;

public class RegisterHandler {

    private final UserService userService;
    private final Gson gson;

    public RegisterHandler(UserService userService){
        this.userService = userService;
        this.gson = new Gson();
    }

    public RegisterRequest deserialize(String json) {
        return gson.fromJson(json, RegisterRequest.class);
    }

    public String serialize(RegisterResult result) {
        return gson.toJson(result);
    }

    public RegisterResult handleRegister(String json){
        RegisterRequest request;
        try {
            request = deserialize(json);
        } catch (JsonSyntaxException e){
            return new RegisterResult(null, null, "Invalid JSON");
        }
        return userService.register(request);
    }

    public String handleRegisterToJson(String json){
        RegisterResult result = handleRegister(json);
        return serialize(result);
    }
}
