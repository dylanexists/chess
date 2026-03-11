package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class SQLGameDao extends SQLBaseDao implements GameDao{
    private final Gson gson;

    public SQLGameDao(Gson gson){
        this.gson = gson;
        try {
            configureDatabase(createStatement);
        } catch (QueryException e) {
            System.err.println("GameDao table initialization failed");
        }
    }

    public void clearGames() throws QueryException {
        String clearStatement = "TRUNCATE TABLE games;";
        try {
            execUpdateStatement(clearStatement);
        } catch (QueryException e){
            throw new QueryException("GameDao's clear statement failed", e);
        }
    }

    public GameData createGame(GameData g) throws DuplicateException, QueryException{
        if (existsGame(g.gameID())) {throw new DuplicateException("Already exists");}
        String insertGameStatement = """
                INSERT INTO games (gameID, whiteUser, blackUser, gameName, game)
                VALUES (?,?,?,?,?)
                """;
        String game = gson.toJson(g.game(), g.game().getClass());
        try {
            execUpdateStatement(insertGameStatement, prepState -> {
                prepState.setInt(1, g.gameID());
                prepState.setString(2, g.whiteUsername());
                prepState.setString(3, g.blackUsername());
                prepState.setString(4, g.gameName());
                prepState.setString(5, game);
            });
            return g;
        } catch (QueryException e){
            throw new QueryException("GameDao's clear statement failed", e);
        }
    }


    public GameData getGame(int gameID) throws NotFoundException, QueryException{
        String retrieveGameStatement = """
                SELECT * FROM games
                WHERE gameID = ?;
                """;
        try {
            List<GameData> gameList = execQueryStatement(
                    retrieveGameStatement,
                    prepState -> prepState.setInt(1, gameID),
                    resSet -> readGame(resSet));
            if (gameList.isEmpty()){
                throw new NotFoundException("Not found");
            }
            return gameList.getFirst();
        } catch (QueryException e){
            throw new QueryException("GameDao's get statement failed", e);
        }
    }

    public List<GameData> listGames() throws QueryException{
        String retrieveGameStatement = """
                SELECT gameID, whiteUser, blackUser, gameName, game FROM games;
                """;
        try {
            List<GameData> gameList = execQueryStatement(
                    retrieveGameStatement,
                    prepState -> {},
                    resSet -> readGame(resSet));
            return gameList;
        } catch (QueryException e){
            throw new QueryException("GameDao's list statement failed", e);
        }
    }

    public GameData updateGame(GameData g) throws DataAccessException{
        if (!existsGame(g.gameID())) {throw new NotFoundException("Not found");}
        String updateGameStatement = """
                UPDATE games
                SET whiteUser = ?, blackUser = ?, gameName = ?, game = ?
                WHERE gameID = ?
                """;
        String game = gson.toJson(g.game(), g.game().getClass());
        try {
            execUpdateStatement(updateGameStatement, prepState -> {
                prepState.setString(1, g.whiteUsername());
                prepState.setString(2, g.blackUsername());
                prepState.setString(3, g.gameName());
                prepState.setString(4, game);
                prepState.setInt(5, g.gameID());
            });
            return g;
        } catch (QueryException e){
            throw new QueryException("GameDao's update statement failed", e);
        }
    }

    public boolean existsGame(int gameID) throws QueryException{
        try {
            getGame(gameID);
            return true;
        } catch (NotFoundException e) {
            return false;
        }
    }


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

    private GameData readGame (ResultSet rs) throws SQLException {
        var gameID = rs.getInt("gameID");
        var whiteUser = rs.getString("whiteUser");
        var blackUser = rs.getString("blackUser");
        var gameName = rs.getString("gameName");
        var gameJSON = rs.getString("game");
        var game = gson.fromJson(gameJSON, ChessGame.class);
        return new GameData(gameID, whiteUser, blackUser, gameName, game);
    }

}