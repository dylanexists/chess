package dataaccess;

import model.GameData;

import java.util.HashMap;
import java.util.List;

public class MemoryGameDao implements GameDao{
    final private HashMap<Integer, GameData> games = new HashMap<>();

    @Override
    public void clearGames() throws QueryException {
        games.clear();
    }

    @Override
    public GameData createGame(GameData g) throws DuplicateException {
        if (existsGame(g.gameID())){
            throw new DuplicateException("Game already exists");
        }
        games.put(g.gameID(), g);
        return g;
    }

    @Override
    public GameData getGame(int gameID) throws NotFoundException {
        GameData game = games.get(gameID);
        if (game == null){
            throw new NotFoundException("GameData not found");
        }
        return game;
    }

    @Override
    public List<GameData> listGames() throws NotFoundException {
        if (games.isEmpty()){
            throw new NotFoundException("No games found");
        }
        return List.copyOf(games.values());
    }

    @Override
    public GameData updateGame(GameData g) throws DataAccessException {
        if (g == null){
            throw new DataAccessException("GameData cannot be null");
        }

        int id = g.gameID();

        if (!games.containsKey(id)){
            throw new NotFoundException("Game with ID: " + id + " not found");
        }
        games.put(id, g);
        return g;
    }

    @Override
    public boolean existsGame(int gameID){
        return games.containsKey(gameID);
    }

}
