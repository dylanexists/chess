package server.websocket;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.AuthDao;
import dataaccess.DataAccessException;
import dataaccess.GameDao;
import facade.ResponseException;
import io.javalin.websocket.*;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.jetbrains.annotations.NotNull;
import server.Server;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

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
            saveSession(gameID, session);

            switch (command.getCommandType()) {
                case CONNECT -> connect(session, username, command);
                case MAKE_MOVE -> makeMove(session, username, serialize(wsMessageContext.message(), MakeMoveCommand.class));
                case LEAVE -> leave(session, username, command);
                case RESIGN -> resign(session, username, command);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void handleClose(WsCloseContext ctx) {
        System.out.println("WS closed");
    }

    private <T extends UserGameCommand> T serialize(String json, Class<T> givenClass) {
        return gson.fromJson(json, givenClass);
    }

    private void connect(Session session, String username, UserGameCommand command) {
        int gameID = command.getGameID();
        connections.add(gameID, session);
        var gameMessage = getLoadGameOrError(gameID);
        sendLoadGameOrErrorMessage(session, gameMessage);
        if (gameMessage.getServerMessageType() == ServerMessage.ServerMessageType.LOAD_GAME) {
            String message = username + " has joined the game.";
            var notification = new NotificationMessage(message);
            connections.broadcastNotification(gameID, session, notification);
        }
    }

    private void makeMove(Session session, String username, MakeMoveCommand command) {

    }

    private void leave(Session session, String username, UserGameCommand command) {
        int gameID = command.getGameID();
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

    private String getUsername(String authToken) {
        try {
            AuthData auth = authDao.getAuth(authToken);
            return auth.username();
        } catch (DataAccessException ex) {
            throw new ResponseException(ex.getMessage(), ex);
        }
    }

    private ServerMessage getLoadGameOrError(int gameID) {
        try {
            GameData game = gameDao.getGame(gameID);
            return new LoadGameMessage(game.game());
        } catch (DataAccessException ex) {
            return new ErrorMessage("Error: no game with that ID");
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
