package handler;

public interface Handler<R, S> {

    String headerStringToJson(String name, String header);

    S handle(String json);

    S runRequestSpecificService(R request);
}
