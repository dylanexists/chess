package handler;

public interface Handler<Request, Result> {

    Request deserialize(String json);

    String serialize(Result result);

    String handle(String json);

    String handleToJson(Result result);

    Result handleRequest(Request request);
}
