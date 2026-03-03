package handler;

import com.google.gson.Gson;
import service.GameService;
import service.request.CreateGameRequest;
import service.result.CreateGameResult;

public class CreateGameHandler
        extends AbsBaseHandler<CreateGameRequest, CreateGameResult>{

    private final GameService gameService;

    public CreateGameHandler(Gson gson, GameService gameService){
        super(gson, CreateGameRequest.class);
        this.gameService = gameService;
    }

    @Override
    public CreateGameResult runRequestSpecificService(CreateGameRequest request) {return gameService.createGame(request);}

    @Override
    protected CreateGameResult invalidJsonResponse() {return CreateGameResult.badRequest();}

    @Override
    protected CreateGameResult internalError(Exception e) {
        String errorDesc = (e.getMessage() == null)
                ? "Internal error (no error message given)"
                : e.getMessage();
        return new CreateGameResult(null, "Error" + errorDesc);
    }
}
