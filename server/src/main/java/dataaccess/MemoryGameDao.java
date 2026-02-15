package dataaccess;

import model.GameData;

import java.util.List;

public class MemoryGameDao implements GameDao{
    @Override
    public void clearGames() throws QueryException {

    }

    @Override
    public GameData createGame(GameData g) throws DuplicateException {
        return null;
    }

    @Override
    public GameData getGame(int gameID) throws NotFoundException {
        return null;
    }

    @Override
    public List<GameData> listGames() throws NotFoundException {
        return List.of();
    }

    @Override
    public GameData updateGame(GameData g) throws DataAccessException {
        return null;
    }
}
