package client.facade;

import com.google.gson.Gson;
import facade.ResponseException;
import request.*;
import result.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ServerFacade {

    private final HttpClient client = HttpClient.newHttpClient();
    private final String serverUrl;

    private final HttpCommunicator http;

    public ServerFacade(String url) {
        serverUrl = url;
        http = new HttpCommunicator(serverUrl);
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

}
