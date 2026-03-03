package handler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import model.GameData;
import service.GameService;
import service.request.ListGamesRequest;
import service.result.ListGamesResult;

import java.util.List;

public class ListGamesHandler
        extends AbsBaseHandler<ListGamesRequest, ListGamesResult>{

    private final GameService gameService;

    public ListGamesHandler(Gson gson, GameService gameService){
        super(gson, ListGamesRequest.class);
        this.gameService = gameService;
    }

    @Override
    public String serialize(ListGamesResult result) {
        List<GameData> gamesList = result.gamesList();
        JsonObject resultJson = new JsonObject();
        resultJson.add("games", gson.toJsonTree(gamesList));
        return gson.toJson(resultJson);
    }

    @Override
    public ListGamesResult runRequestSpecificService(ListGamesRequest request) {return gameService.listGames(request);}

    @Override
    protected ListGamesResult invalidJsonResponse() {return ListGamesResult.badRequest();}

    @Override
    protected ListGamesResult internalError(Exception e) {
        String errorDesc = (e.getMessage() == null)
                ? "Internal error (no error message given)"
                : e.getMessage();
        return new ListGamesResult(null, "Error" + errorDesc);
    }
}
