package handler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.util.Map;


public abstract class AbsBaseHandler<Request, Result> implements Handler<Request, Result> {

    protected final Gson gson;
    private final Class<Request> requestClass;

    protected AbsBaseHandler (Gson gson, Class<Request> requestClass){
        this.gson = gson;
        this.requestClass = requestClass;
    }

    public Request deserialize(String json) {
        return gson.fromJson(json, requestClass);
    }

    public String serialize(Result result) {
        return gson.toJson(result);
    }

    public Result handle(String json){
        try {
            Request request = deserialize(json);
            return runRequestSpecificService(request);
        } catch (JsonSyntaxException e){
            return invalidJsonResponse();
        } catch (Exception e) {
            return internalError(e);
        }
    }

    public String headerStringToJson(String name, String header){
        Map<String, String> jsonMap = Map.of(name, header);
        return gson.toJson(jsonMap);
    }

    public String combineHeaderAndBodyJson(String header, String body){
        JsonObject headerJson = JsonParser.parseString(header).getAsJsonObject();
        JsonObject bodyJson = JsonParser.parseString(body).getAsJsonObject();

        JsonObject combinedJson = new JsonObject();
        for (var entry: headerJson.entrySet()){
            combinedJson.add(entry.getKey(), entry.getValue());
        }
        for (var entry: bodyJson.entrySet()){
            combinedJson.add(entry.getKey(), entry.getValue());
        }
        return gson.toJson(combinedJson);
    }

    protected abstract Result invalidJsonResponse();

    protected abstract Result internalError(Exception e);
}
