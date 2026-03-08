package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SQLGameDao implements GameDao{
    private final Gson gson;

    public SQLGameDao(Gson gson){
        this.gson = gson;
        try {
            configureDatabase();
        } catch (DataAccessException e) {
            System.err.println("GameDao table initialization failed");
        }
    }

    public void clearGames() throws QueryException {
        String clearStatement = "TRUNCATE TABLE games;";
        try {
            executeStatement(clearStatement);
        } catch (DataAccessException e){
            throw new QueryException("GameDao's clear statement failed");
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
            executeStatement(insertGameStatement, prepState -> {
                prepState.setInt(1, gameID);
                prepState.setString(2, whiteUser);
                prepState.setString(3, blackUser);
                prepState.setString(4, gameName);
                prepState.setString(5, game);
            });
            return g;
        } catch (DataAccessException e){
            throw new QueryException("GameDao's clear statement failed");
        }
    }


    public GameData getGame(int gameID) throws NotFoundException{return new GameData(1, "", "","", new ChessGame());}

    public List<GameData> listGames() throws QueryException{
        List<GameData> g = new ArrayList<>();
        return g;
    }

    public GameData updateGame(GameData g) throws DataAccessException{return new GameData(1, "", "","", new ChessGame());}

    public boolean existsGame(int gameID){return true;}


    @FunctionalInterface
    private interface SQLConsumer<T> {
        void accept(T t) throws SQLException;
    }

    private void executeStatement(String statement) throws DataAccessException{
        try (var conn = DatabaseManager.getConnection();
                var preparedStatement = conn.prepareStatement(statement)) {
            preparedStatement.executeUpdate();
        } catch (SQLException e){
            throw new DataAccessException("Failed SQL statement", e);
        }
    }

    private void executeStatement(String statement, SQLConsumer<PreparedStatement> paramSetter) throws DataAccessException{
        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(statement)) {
            paramSetter.accept(preparedStatement);
            preparedStatement.executeUpdate();
        } catch (SQLException e){
            throw new DataAccessException("Failed SQL statement", e);
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

    private void configureDatabase() throws DataAccessException {
        executeStatement(createStatement);
    }
}
