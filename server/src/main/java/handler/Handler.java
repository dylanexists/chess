package handler;

public interface Handler<Request, Result> {

    String headerStringToJson(String name, String header);

    Result handle(String json);

    Result runRequestSpecificService(Request request);
}
