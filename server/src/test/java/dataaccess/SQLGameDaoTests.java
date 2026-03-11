package dataaccess;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import com.google.gson.Gson;
import model.GameData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SQLGameDaoTests {

    private static SQLGameDao gameDao;

    @BeforeAll
    public static void setUpDatabase() {
        try {
            DatabaseManager.createDatabase();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
        Gson gson = new Gson();
        gameDao = new SQLGameDao(gson);
    }

    @BeforeEach
    void setUp() {
        try {
            gameDao.clearGames();
        } catch (QueryException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("GameDao clearGames() Positive Test")
    public void clearGameSuccess() {
        assertDoesNotThrow(() -> {
            ChessGame game = new ChessGame();
            ChessGame game2 = new ChessGame();
            gameDao.createGame(new GameData(1, "white", "black", "game1", game));
            gameDao.createGame(new GameData(2, "white", "black", "game2", game2));
            assertTrue(gameDao.existsGame(1));
            assertTrue(gameDao.existsGame(2));
            gameDao.clearGames();
            assertFalse(gameDao.existsGame(1));
            assertFalse(gameDao.existsGame(2));
        });
    }

    @Test
    @DisplayName("GameDao createGame() Positive Test")
    public void createGameSuccess() {
        assertDoesNotThrow(() -> {
            ChessGame game = new ChessGame();
            ChessGame game2 = new ChessGame();
            gameDao.createGame(new GameData(1, "white", "black", "game1", game));
            assertTrue(gameDao.existsGame(1));
            gameDao.createGame(new GameData(2, "white", "black", "game2", game2));
            assertTrue(gameDao.existsGame(2));
        });
    }

    @Test
    @DisplayName("GameDao createGame() Negative Test")
    public void createGameFailure() {
        assertThrows(DuplicateException.class, () -> {
            ChessGame game = new ChessGame();
            gameDao.createGame(new GameData(1, "white", "black", "testGame", game));
            assertTrue(gameDao.existsGame(1));
            gameDao.createGame(new GameData(1, "white", "black", "testGame", game));
        });
    }

    @Test
    @DisplayName("GameDao getGame() Positive Test")
    public void getGameSuccess() {
        assertDoesNotThrow(() -> {
            ChessGame game = new ChessGame();
            ChessGame game2 = new ChessGame();
            gameDao.createGame(new GameData(1, "white", "black", "game1", game));
            gameDao.getGame(1);
            gameDao.createGame(new GameData(2, "white", "black", "game2", game2));
            gameDao.getGame(2);
            gameDao.getGame(1);
        });
    }

    @Test
    @DisplayName("GameDao getGame() Negative Test")
    public void getGameFailure() {
        assertThrows(NotFoundException.class, () -> {
            gameDao.createGame(new GameData(1, "white", "black", "game1", new ChessGame()));
            gameDao.getGame(999);
        });
    }

    @Test
    @DisplayName("GameDao listGames() Positive Test")
    public void listGamesSuccess() {
        assertDoesNotThrow(() -> {
            ChessGame game = new ChessGame();
            ChessGame game2 = new ChessGame();
            GameData gameData1 = new GameData(1, "white", "black", "game1", game);
            GameData gameData2 = new GameData(2, "white", "black", "game2", game2);
            gameDao.createGame(gameData1);
            gameDao.createGame(gameData2);
            List<GameData> gameList = gameDao.listGames();
            assertEquals(2, gameList.size());
            assertEquals(gameData1, gameList.getFirst());
            assertEquals(gameData2, gameList.get(1));
            GameData gameData3 = new GameData(3, "white", "black", "game3", new ChessGame());
            gameDao.createGame(gameData3);
            gameList = gameDao.listGames();
            assertEquals(3, gameList.size());
            assertEquals(gameData3, gameList.get(2));
        });
    }

    @Test
    @DisplayName("GameDao listGames() Negative Test")
    public void listGamesFailure() {
        assertDoesNotThrow(() -> { //Failure in the sense that when there are no games, an empty list is returned
            List<GameData> gameList = gameDao.listGames();
            assertEquals(0, gameList.size());
        });
    }

    @Test
    @DisplayName("GameDao updateGame() Positive Test")
    public void updateGameSuccess() {
        assertDoesNotThrow(() -> {
            ChessGame game = new ChessGame();
            GameData gameData = new GameData(1, "white", "black", "game1", game);
            gameDao.createGame(gameData);
            GameData storedGame = gameDao.getGame(1);
            assertEquals(game, storedGame.game());
            game.makeMove(new ChessMove(new ChessPosition(2, 1), new ChessPosition(4, 1), null));
            assertNotEquals(game, storedGame.game()); //storedGame is still the old game data
            gameDao.updateGame(gameData);
            storedGame = gameDao.getGame(1);
            assertEquals(game, storedGame.game());
        });
    }

    @Test
    @DisplayName("GameDao updateGame() Negative Test")
    public void updateGameFailure() {
        assertThrows(NotFoundException.class, () -> {
            ChessGame game = new ChessGame();
            GameData gameData = new GameData(1, "white", "black", "game1", game);
            gameDao.createGame(gameData);
            GameData storedGame = gameDao.getGame(1);
            assertEquals(game, storedGame.game());
            game.makeMove(new ChessMove(new ChessPosition(2, 1), new ChessPosition(4, 1), null));
            assertNotEquals(game, storedGame.game()); //storedGame is still the old game data
            GameData nonExistentGameData = new GameData(42, "white", "black", "game1", game);
            gameDao.updateGame(nonExistentGameData);
        });
    }

}
