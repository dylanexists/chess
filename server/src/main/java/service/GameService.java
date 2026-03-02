package service;

import dataaccess.GameDao;
import dataaccess.QueryException;
import service.result.ClearResult;

public class GameService {
    private final GameDao gameDao;

    public GameService(GameDao gameDao){
        this.gameDao = gameDao;
    }

    public ClearResult clear() {
        try {
            gameDao.clearGames();
            return new ClearResult(null);
        } catch (QueryException qExcept) {
            return new ClearResult(qExcept.toString());
        }
    }
}
