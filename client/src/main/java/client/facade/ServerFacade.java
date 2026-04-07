package client.facade;

import chess.ChessMove;
import chess.ChessPosition;
import client.websocket.ServerMessageObserver;
import client.websocket.WebSocketCommunicator;
import facade.ResponseException;
import request.*;
import result.*;


public class ServerFacade {

    private final String serverUrl;
    private final HttpCommunicator http;
    private final WebSocketCommunicator ws;

    public ServerFacade(String url, ServerMessageObserver serverMessageObserver) {
        serverUrl = url;
        http = new HttpCommunicator(serverUrl);
        ws = new WebSocketCommunicator(serverUrl, serverMessageObserver);
    }

    public RegisterResult register(RegisterRequest request) throws ResponseException {
        return http.register(request);
    }

    public LoginResult login(LoginRequest request) {
        return http.login(request);
    }

    public LogoutResult logout(LogoutRequest request) {
        return http.logout(request);
    }

    public CreateGameResult createGame(CreateGameRequest request) {
        return http.createGame(request);
    }

    public ListGamesResult listGames(ListGamesRequest request) {
        return http.listGames(request);
    }

    public JoinGameResult joinGame(JoinGameRequest request) {
        return http.joinGame(request);
    }

    public ClearResult clear(ClearRequest request) {
        return http.clear(request);
    }

    public void wsLeaveGame(String authToken, Integer gameID) {
        ws.leaveGame(authToken, gameID);
    }

    public void wsConnectGame(String authToken, Integer gameID) {
        ws.connectGame(authToken, gameID);
    }

    public void wsMakeMove(String authToken, Integer gameID, ChessMove move) {ws.makeMove(authToken, gameID, move);}

    public void wsResign(String authToken, Integer gameID) {ws.resign(authToken, gameID);}

    public void wsRedraw(String authToken, Integer gameID) {ws.redraw(authToken, gameID);}

    public void wsHighlight(String authToken, Integer gameID, ChessPosition pos) {ws.highlight(authToken, gameID, pos);}

}
