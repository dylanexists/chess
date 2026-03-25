package result;

import model.GameData;

import java.util.List;

public record ListGamesResult(List<GameData> gamesList, String message) {

    public static ListGamesResult badRequest(){
        return new ListGamesResult(null, "Error: bad request");
    }

    public static ListGamesResult unauthorized(){
        return new ListGamesResult(null, "Error: unauthorized");
    }

    public static ListGamesResult internalError(){
        return new ListGamesResult(null, "Internal server error");
    }
}
