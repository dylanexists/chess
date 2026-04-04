package server.websocket;

import chess.ChessMove;
import com.google.gson.Gson;
import dataaccess.AuthDao;
import dataaccess.DataAccessException;
import dataaccess.NotFoundException;
import dataaccess.UserDao;
import facade.ResponseException;
import io.javalin.websocket.*;
import model.AuthData;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.websocket.api.Session;
import org.jetbrains.annotations.NotNull;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;

public class WebSocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {
    private final ConnectionManager connections = new ConnectionManager();
    private final Gson gson= new Gson();
    private final AuthDao authDao;

    public WebSocketHandler(AuthDao userDao) {
        this.authDao = userDao;
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

    }

    private void makeMove(Session session, String username, MakeMoveCommand command) {

    }

    private void leave(Session session, String username, UserGameCommand command) {

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
}
