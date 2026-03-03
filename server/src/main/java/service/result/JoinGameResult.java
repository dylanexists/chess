package service.result;

public record JoinGameResult(String message) {

    public static JoinGameResult badRequest() {return new JoinGameResult("Error: bad request");}

    public static JoinGameResult alreadyTaken(){
        return new JoinGameResult("Error: already taken");
    }

    public static JoinGameResult unauthorized(){
        return new JoinGameResult("Error: unauthorized");
    }

    public static JoinGameResult internalError(){
        return new JoinGameResult("Internal server error");
    }
}
