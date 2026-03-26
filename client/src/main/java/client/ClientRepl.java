package client;

import server.ResponseException;
import server.ServerFacade;

public class ClientRepl {
    private final ServerFacade serverFac;
    private final PreLoginClient preLogin;
    private final PostLoginClient postLogin;
    private final InGameClient inGame;

    public ClientRepl(String serverUrl) throws ResponseException {
        serverFac = new ServerFacade(serverUrl);
        preLogin = new PreLoginClient(serverFac);
        postLogin = new PostLoginClient(serverFac);
        inGame = new InGameClient();
    }

    public void run() {
        ClientState state = ClientState.PRE_LOGIN;
        String authToken = null;
        Integer gameID = null;
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
                    break;
                case IN_GAME:
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
