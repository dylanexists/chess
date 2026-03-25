package result;

public record CreateGameResult(Integer gameID, String message) {

    public static CreateGameResult badRequest(){
        return new CreateGameResult(null, "Error: bad request");
    }

    public static CreateGameResult unauthorized(){
        return new CreateGameResult(null, "Error: unauthorized");
    }

    public static CreateGameResult internalError(){
        return new CreateGameResult(null, "Internal server error");
    }
}
