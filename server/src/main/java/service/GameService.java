package service;

import chess.ChessGame;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import service.request.CreateGameRequest;
import service.result.ClearResult;
import service.result.CreateGameResult;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class GameService {
    private final GameDao gameDao;
    private final AuthDao authDao;
    private final Set<Integer> usedGameIDs = new HashSet<>();

    public GameService(GameDao gameDao, AuthDao authDao){
        this.gameDao = gameDao;
        this.authDao = authDao;
    }

    public ClearResult clear() {
        try {
            gameDao.clearGames();
            return new ClearResult(null);
        } catch (QueryException qExcept) {
            return new ClearResult(qExcept.toString());
        }
    }

    public CreateGameResult createGame(CreateGameRequest request){
        if (request == null) {return CreateGameResult.badRequest();}
        String authToken = request.authToken();
        String gameName = request.gameName(); //from the API tests, it appears game name is not required

        if (gameName == null) {return CreateGameResult.badRequest();}
        if (authToken == null || authToken.isEmpty()){return CreateGameResult.unauthorized();}
        try { //success case
            AuthData currentSession = authDao.getAuth(authToken); //checks if authToken exists
            int uniqueGameID = generateUniqueID();
            GameData newGame = new GameData(uniqueGameID, null, null, gameName, new ChessGame());
            gameDao.createGame(newGame); //will try to create Game
            return new CreateGameResult(uniqueGameID, null);
        } catch (NotFoundException e) {
            return CreateGameResult.unauthorized();
        } catch (DuplicateException dExcept){
            return CreateGameResult.internalError();
        }
    }

    private int generateUniqueID(){
        int id;
        do {
            id = UUID.randomUUID().hashCode();

            if (id <= 0) { //negative integer check
                id = (id == Integer.MIN_VALUE) ? 1 : Math.abs(id); //handles rare edge case where Math.abs() doesn't work
            }
        } while (usedGameIDs.contains(id));
        usedGameIDs.add(id);
        return id;
    }
}
