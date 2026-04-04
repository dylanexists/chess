package server.websocket;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.*;
import facade.ResponseException;
import io.javalin.websocket.*;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.jetbrains.annotations.NotNull;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import javax.xml.crypto.Data;
import java.io.IOException;

public class WebSocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {
    private final ConnectionManager connections = new ConnectionManager();
    private final Gson gson= new Gson();
    private final GameDao gameDao;
    private final AuthDao authDao;

    public WebSocketHandler(GameDao gameDao, AuthDao authDao) {
        this.authDao = authDao;
        this.gameDao = gameDao;
    }

    @Override
    public void handleConnect(WsConnectContext ctx) {
        System.out.println("WS connected");
        ctx.enableAutomaticPings();
    }

    @Override
    public void handleMessage(@NotNull WsMessageContext wsMessageContext) throws Exception {
        int gameID = -1;
        Session session = wsMessageContext.session;

        try {
            UserGameCommand command = serialize(wsMessageContext.message(), UserGameCommand.class);
            gameID = command.getGameID();
            String username = getUsername(command.getAuthToken());
            ChessGame game = gameDao.getGame(gameID).game();
            saveSession(gameID, session);

            switch (command.getCommandType()) {
                case CONNECT -> connect(session, username, game, command);
                case MAKE_MOVE -> makeMove(session, username, serialize(wsMessageContext.message(), MakeMoveCommand.class));
                case LEAVE -> leave(session, username, command);
                case RESIGN -> resign(session, username, command);
            }
        } catch (DataAccessException ex) {
            sendLoadGameOrErrorMessage(session, new ErrorMessage("User or Game not found"));
        } catch (Exception ex) {
            sendLoadGameOrErrorMessage(session, new ErrorMessage("Error: undefined"));
        }
    }

    @Override
    public void handleClose(WsCloseContext ctx) {
        System.out.println("WS closed");
    }

    private <T extends UserGameCommand> T serialize(String json, Class<T> givenClass) {
        return gson.fromJson(json, givenClass);
    }

    private void connect(Session session, String username, ChessGame game, UserGameCommand command) {
        int gameID = command.getGameID();
        connections.add(gameID, session);
        LoadGameMessage gameMessage = new LoadGameMessage(game);
        sendLoadGameOrErrorMessage(session, gameMessage);
        String message = username + " has joined the game.";
        var notification = new NotificationMessage(message);
        connections.broadcastNotification(gameID, session, notification);
    }

    private void makeMove(Session session, String username, MakeMoveCommand command) {

    }

    private void leave(Session session, String username, UserGameCommand command) throws DataAccessException{
        int gameID = command.getGameID();
        removeUserFromGame(gameID, username);
        connections.remove(gameID, session);
        String message = username + " has left the game";
        var notification = new NotificationMessage(message);
        connections.broadcastNotification(gameID, session, notification);
    }

    private void resign(Session session, String username, UserGameCommand command) {

    }

    private void saveSession(Integer gameID, Session session) {
        connections.add(gameID, session);
    }

    private String getUsername(String authToken) throws NotFoundException, QueryException {
        AuthData auth = authDao.getAuth(authToken);
        return auth.username();
    }

    private ChessGame getGame(int gameID) throws NotFoundException, QueryException {
        GameData game = gameDao.getGame(gameID);
        return game.game();
    }

    private void removeUserFromGame(int gameID, String username) throws DataAccessException {
        GameData gameData = gameDao.getGame(gameID);
        if (username.equals(gameData.whiteUsername())){
            gameDao.updateGame(new GameData(gameData.gameID(),
                                null,
                                gameData.blackUsername(),
                                gameData.gameName(),
                                gameData.game()));
        } else if (username.equals(gameData.blackUsername())) {
            gameDao.updateGame(new GameData(gameData.gameID(),
                    gameData.whiteUsername(),
                    null,
                    gameData.gameName(),
                    gameData.game()));
        }
    }

    public void sendLoadGameOrErrorMessage(Session session, ServerMessage loadGameOrErrorMessage) {
        try {
            session.getRemote().sendString(gson.toJson(loadGameOrErrorMessage));
        } catch (IOException e) {
            throw new ResponseException("invalid session");
        }
    }
}
