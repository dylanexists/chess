package client;

import chess.ChessGame;
import client.facade.ServerFacade;
import client.websocket.ServerMessageObserver;
import facade.ResponseException;
import ui.DrawnChessBoard;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import static ui.EscapeSequences.SET_TEXT_COLOR_RED;

public class ClientRepl implements ServerMessageObserver {
    private final ServerFacade serverFac;
    private final PreLoginClient preLogin;
    private final PostLoginClient postLogin;
    private final InGameClient inGame;

    public ClientRepl(String serverUrl) throws ResponseException {
        serverFac = new ServerFacade(serverUrl, this);
        preLogin = new PreLoginClient(serverFac);
        postLogin = new PostLoginClient(serverFac);
        inGame = new InGameClient(serverFac);
    }

    @Override
    public void notify(ServerMessage message) {
        switch (message.getServerMessageType()) {
            case NOTIFICATION -> displayNotification(((NotificationMessage) message).getMessage());
            case ERROR -> displayError(((ErrorMessage) message).getErrorMessage());
            case LOAD_GAME -> loadGame(((LoadGameMessage) message).getGame());
        }
    }

    public void displayNotification(String notification) {
        System.out.println(SET_TEXT_COLOR_RED + "Notif:" + notification);
    }

    public void displayError(String error) {
        System.out.println(SET_TEXT_COLOR_RED + "Error:" + error);
    }

    public void loadGame(ChessGame game) {inGame.loadGame(game);}

    public void run() {
        ClientState state = ClientState.PRE_LOGIN;
        String authToken = null;
        Integer gameID = null;
        ChessGame.TeamColor playerColor = null;
        System.out.println("Welcome to Chess!\n");

        while (state != ClientState.EXIT) {
            switch (state) {
                case PRE_LOGIN:
                    var preResult = preLogin.run();
                    state = preResult.nextState();
                    authToken = preResult.authToken();
                    break;
                case POST_LOGIN:
                    var postResult = postLogin.run(authToken);
                    state = postResult.nextState();
                    gameID = postResult.gameID();
                    playerColor = postResult.playerColor();
                    break;
                case IN_GAME:
                    var inGameResult = inGame.run(authToken, gameID, playerColor);
                    state = inGameResult.nextState();
                    break;
            }
        }
    }

    public enum ClientState {
        PRE_LOGIN,
        POST_LOGIN,
        IN_GAME,
        EXIT
    }
}
