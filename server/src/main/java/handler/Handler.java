package handler;

public interface Handler<Request, Result> {

    String handle(String json);

    Result handleRequest(Request request);
}
