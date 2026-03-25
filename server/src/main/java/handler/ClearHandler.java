package handler;

import com.google.gson.Gson;
import service.GameService;
import service.UserService;
import request.ClearRequest;
import result.ClearResult;

public class ClearHandler
        extends AbsBaseHandler<ClearRequest, ClearResult>{

    private final UserService userService;
    private final GameService gameService;

    public ClearHandler(Gson gson, UserService userService, GameService gameService){
        super(gson, ClearRequest.class);
        this.userService = userService;
        this.gameService = gameService;
    }

    @Override
    public ClearResult runRequestSpecificService(ClearRequest request) {
        ClearResult userResult = userService.clear();
        ClearResult gameResult = gameService.clear();
        return userResult.message() == null && gameResult.message() == null
                ? new ClearResult(null) : internalError(new Exception());
    }

    @Override
    protected ClearResult invalidJsonResponse() {
        return new ClearResult("Error: bad request");
    }

    @Override
    protected ClearResult internalError(Exception e) {
        String errorDesc = (e.getMessage() == null)
                ? "Internal error (no error message given)"
                : e.getMessage();
        return new ClearResult("Error: " + errorDesc);
    }
}
