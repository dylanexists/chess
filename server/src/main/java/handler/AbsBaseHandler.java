package handler;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;


public abstract class AbsBaseHandler<Request, Result> implements Handler<Request, Result> {

    private final Gson gson;
    private final Class<Request> requestClass;

    private AbsBaseHandler (Class<Request> requestClass){
        this.gson = new Gson();
        this.requestClass = requestClass;
    }

    public Request deserialize(String json) {
        return gson.fromJson(json, requestClass);
    }

    public String serialize(Result result) {
        return gson.toJson(result);
    }

    public String handle(String json){
        try {
            Request request = deserialize(json);
            Result result = handleRequest(request);
            return serialize(result);
        } catch (JsonSyntaxException e){
            return serialize(invalidJsonResponse());
        }
    }

    protected abstract Result invalidJsonResponse();
}
