package client;

import chess.ChessGame;
import facade.ResponseException;
import facade.ServerFacade;

public class ClientRepl {
    private final ServerFacade serverFac;
    private final PreLoginClient preLogin;
    private final PostLoginClient postLogin;
    private final InGameClient inGame;

    public ClientRepl(String serverUrl) throws ResponseException {
        serverFac = new ServerFacade(serverUrl);
        preLogin = new PreLoginClient(serverFac);
        postLogin = new PostLoginClient(serverFac);
        inGame = new InGameClient(serverFac);
    }

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
