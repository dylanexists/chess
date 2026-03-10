package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;

import java.util.ArrayList;
import java.util.List;

public class SQLGameDao extends SQLBaseDao implements GameDao{
    private final Gson gson;

    public SQLGameDao(Gson gson){
        this.gson = gson;
        try {
            configureDatabase(createStatement);
        } catch (DataAccessException e) {
            System.err.println("GameDao table initialization failed");
        }
    }

    public void clearGames() throws QueryException {
        String clearStatement = "TRUNCATE TABLE games;";
        try {
            execUpdateStatement(clearStatement);
        } catch (DataAccessException e){
            throw new QueryException("GameDao's clear statement failed", e);
        }
    }

    public GameData createGame(GameData g) throws DataAccessException{
        String insertGameStatement = """
                INSERT INTO games (gameID, whiteUser, blackUser, gameName, game)
                VALUES (?,?,?,?,?)
                """;
        int gameID = g.gameID();
        String whiteUser = g.whiteUsername();
        String blackUser = g.blackUsername();
        String gameName = g.gameName();
        String game = gson.toJson(g.game(), g.game().getClass());
        try {
            execUpdateStatement(insertGameStatement, prepState -> {
                prepState.setInt(1, gameID);
                prepState.setString(2, whiteUser);
                prepState.setString(3, blackUser);
                prepState.setString(4, gameName);
                prepState.setString(5, game);
            });
            return g;
        } catch (DataAccessException e){
            throw new QueryException("GameDao's clear statement failed", e);
        }
    }


    public GameData getGame(int gameID) throws NotFoundException{return new GameData(1, "", "","", new ChessGame());}

    public List<GameData> listGames() throws QueryException{
        List<GameData> g = new ArrayList<>();
        return g;
    }

    public GameData updateGame(GameData g) throws DataAccessException{return new GameData(1, "", "","", new ChessGame());}

    public boolean existsGame(int gameID){return true;}


    private final String createStatement =
            """
                    CREATE TABLE IF NOT EXISTS games (
                    gameID INT,
                    whiteUser varchar(256) NOT NULL,
                    blackUser varchar(256) NOT NULL,
                    gameName varchar(256) NOT NULL,
                    game TEXT,
                    PRIMARY KEY (gameID)
                    )
                    """;

}
