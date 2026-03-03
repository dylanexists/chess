package handler;

import com.google.gson.Gson;
import service.GameService;
import service.request.JoinGameRequest;
import service.result.JoinGameResult;

public class JoinGameHandler
        extends AbsBaseHandler<JoinGameRequest, JoinGameResult>{

    private final GameService gameService;

    public JoinGameHandler(Gson gson, GameService gameService){
        super(gson, JoinGameRequest.class);
        this.gameService = gameService;
    }

    @Override
    public JoinGameResult runRequestSpecificService(JoinGameRequest request) {return gameService.joinGame(request);}

    @Override
    protected JoinGameResult invalidJsonResponse() {return JoinGameResult.badRequest();}

    @Override
    protected JoinGameResult internalError(Exception e) {
        String errorDesc = (e.getMessage() == null)
                ? "Internal error (no error message given)"
                : e.getMessage();
        return new JoinGameResult("Error" + errorDesc);
    }
}
