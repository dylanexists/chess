package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SQLGameDao implements GameDao{

    public SQLGameDao(){
        try {
            configureDatabase();
        } catch (DataAccessException e) {
            System.err.println("GameDao table initialization failed");
        }
    }

    public void clearGames() throws QueryException {}

    public GameData createGame(GameData g) throws DuplicateException{return new GameData(1, "", "","", new ChessGame());}

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

    private void configureDatabase() throws DataAccessException {
        var conn = DatabaseManager.getConnection(); //throws DataAccessException
        try (var preparedStatement = conn.prepareStatement(createStatement);){
            preparedStatement.executeUpdate();
        } catch (SQLException e){
            throw new DataAccessException("GameDao's create statement failed");
        }
    }
}
