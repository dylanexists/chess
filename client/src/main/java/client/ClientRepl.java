package client;

import chess.ChessGame;
import client.facade.ServerFacade;
import client.websocket.ServerMessageObserver;
import com.google.gson.Gson;
import facade.ResponseException;
import ui.DrawnChessBoard;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import static ui.EscapeSequences.*;

public class ClientRepl implements ServerMessageObserver {
    private final Gson gson = new Gson();
    private final ServerFacade serverFac;
    private final PreLoginClient preLogin;
    private final PostLoginClient postLogin;
    private final InGameClient inGame;
    ClientState state;

    public ClientRepl(String serverUrl) throws ResponseException {
        serverFac = new ServerFacade(serverUrl, this);
        preLogin = new PreLoginClient(serverFac);
        postLogin = new PostLoginClient(serverFac);
        inGame = new InGameClient(serverFac);
    }

    @Override
    public void notify(String message) {
        ServerMessage sMessage = gson.fromJson(message, ServerMessage.class);
        switch (sMessage.getServerMessageType()) {
            case NOTIFICATION -> displayNotification((gson.fromJson(message, NotificationMessage.class)).getMessage());
            case ERROR -> displayError((gson.fromJson(message, ErrorMessage.class)).getErrorMessage());
            case LOAD_GAME -> loadGame((gson.fromJson(message, LoadGameMessage.class)).getGame());
        }
    }

    public void displayNotification(String notification) {
        System.out.println(SET_TEXT_COLOR_WHITE + "** " + notification);
        System.out.println(RESET_TEXT_COLOR);
        printPromptOfRepl();
    }

    public void displayError(String error) {
        System.out.println(SET_TEXT_COLOR_RED + "Error: " + error);
        System.out.println(RESET_TEXT_COLOR);
        printPromptOfRepl();
    }

    public void loadGame(ChessGame game) {inGame.loadGame(game);}

    private void printPromptOfRepl() {
        if (state == ClientState.IN_GAME) {inGame.printPrompt();}
        else if (state == ClientState.POST_LOGIN) {postLogin.printPrompt();}
        else if (state == ClientState.PRE_LOGIN) {preLogin.printPrompt();}
    }

    public void run() {
        state = ClientState.PRE_LOGIN;
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
