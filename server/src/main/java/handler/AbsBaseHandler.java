package handler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.util.Map;


public abstract class AbsBaseHandler<R, S> implements Handler<R, S> {

    protected final Gson gson;
    private final Class<R> requestClass;

    protected AbsBaseHandler (Gson gson, Class<R> requestClass){
        this.gson = gson;
        this.requestClass = requestClass;
    }

    public R deserialize(String json) {
        return gson.fromJson(json, requestClass);
    }

    public String serialize(S result) {
        return gson.toJson(result);
    }

    public S handle(String json){
        try {
            R request = deserialize(json);
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

    protected abstract S invalidJsonResponse();

    protected abstract S internalError(Exception e);
}
