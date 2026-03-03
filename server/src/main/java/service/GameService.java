package service;

import chess.ChessGame;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import service.request.CreateGameRequest;
import service.request.JoinGameRequest;
import service.request.ListGamesRequest;
import service.result.ClearResult;
import service.result.CreateGameResult;
import service.result.JoinGameResult;
import service.result.ListGamesResult;

import java.util.*;

public class GameService {
    private final GameDao gameDao;
    private final AuthDao authDao;
    private final Set<Integer> usedGameIDs = new HashSet<>();

    public GameService(GameDao gameDao, AuthDao authDao){
        this.gameDao = gameDao;
        this.authDao = authDao;
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

    public ListGamesResult listGames(ListGamesRequest request) {
        if (request == null) {return ListGamesResult.badRequest();}
        String authToken = request.authToken();

        if (authToken == null || authToken.isEmpty()){return ListGamesResult.unauthorized();}
        try {
            AuthData currentSession = authDao.getAuth(authToken); //checks if authToken exists
            List<GameData> gamesList = new ArrayList<>();
            gamesList = gameDao.listGames();
            return new ListGamesResult(gamesList, null);
        } catch (NotFoundException e) {
            return ListGamesResult.unauthorized();
        } catch (QueryException e){
            return ListGamesResult.internalError();
        }
    }

    public JoinGameResult joinGame(JoinGameRequest request){
        if (request == null) {return JoinGameResult.badRequest();}
        String authToken = request.authToken();
        String playerColor = request.playerColor();
        Integer gameID = request.gameID();

        if (authToken == null || authToken.isEmpty()){return JoinGameResult.unauthorized();}
        if (playerColor == null || gameID == null ||
                (!playerColor.equals("WHITE") && !playerColor.equals("BLACK")) ){return JoinGameResult.badRequest();}
        try {
            AuthData currentSession = authDao.getAuth(authToken); //checks if authToken exists
            GameData retrievedGame = gameDao.getGame(gameID); //tries to retrieve game
            String gameName = retrievedGame.gameName();
            ChessGame game = retrievedGame.game();
            String username = currentSession.username();
            if (playerColor.equals("WHITE") && retrievedGame.whiteUsername() == null) {
                String blackUsername = retrievedGame.blackUsername();
                GameData updatedGame = new GameData(gameID, username, blackUsername, gameName, game);
                gameDao.updateGame(updatedGame);
            } else if (playerColor.equals("BLACK") && retrievedGame.blackUsername() == null){
                String whiteUsername = retrievedGame.whiteUsername();
                GameData updatedGame = new GameData(gameID, whiteUsername, username, gameName, game);
                gameDao.updateGame(updatedGame);
            } else {
                return JoinGameResult.alreadyTaken();
            }
            return new JoinGameResult(null); //success
        } catch (NotFoundException e) {
            return JoinGameResult.unauthorized();
        } catch (DataAccessException e){
            return JoinGameResult.internalError();
        }
    }
}
