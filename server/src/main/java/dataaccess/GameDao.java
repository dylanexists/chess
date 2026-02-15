package dataaccess;

import model.GameData;
import java.util.List;

public interface GameDao {

    void clearGames() throws QueryException;

    GameData createGame(GameData g) throws DuplicateException;

    GameData getGame(int gameID) throws NotFoundException;

    List<GameData> listGames() throws NotFoundException;

    GameData updateGame(GameData g) throws DataAccessException;
}
