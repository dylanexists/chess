package server;


//Response Exception from shared server facade
public class ResponseException extends RuntimeException {
    public ResponseException(String message) {
        super(message);
    }
    public ResponseException(String message, Throwable ex) {
            super(message, ex);
        }
}
