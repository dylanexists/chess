package server;

import com.google.gson.Gson;
import request.*;
import result.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ServerFacade {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String serverUrl;

    public ServerFacade(String url) {serverUrl = url;}

    public RegisterResult register(RegisterRequest request) throws ResponseException {
        try {
            var httpRequest = buildRequest("POST", "/user", request, null);
            var response = sendRequest(httpRequest);
            return handleResponse(response, RegisterResult.class);
        } catch (ResponseException ex) {
            throw new ResponseException("Failed to register: " + ex.getMessage(), ex);
        }
    }

    public LoginResult login(LoginRequest request) {
        try {
            var httpRequest = buildRequest("POST", "/session", request, null);
            var response = sendRequest(httpRequest);
            return handleResponse(response, LoginResult.class);
        } catch (ResponseException ex) {
            throw new ResponseException("Failed to login: " + ex.getMessage(), ex);
        }
    }

    public LogoutResult logout(LogoutRequest request) {
        try {
            var httpRequest = buildRequest("DELETE", "/session", request, request.authToken());
            var response = sendRequest(httpRequest);
            return handleResponse(response, LogoutResult.class);
        } catch (ResponseException ex) {
            throw new ResponseException("Failed to logout: " + ex.getMessage(), ex);
        }
    }

    public CreateGameResult createGame(CreateGameRequest request) {
        try {
            var httpRequest = buildRequest("POST", "/game", request, request.authToken());
            var response = sendRequest(httpRequest);
            return handleResponse(response, CreateGameResult.class);
        } catch (ResponseException ex) {
            throw new ResponseException("Failed to create game: " + ex.getMessage(), ex);
        }
    }

    public ListGamesResult listGames(ListGamesRequest request) {
        try {
            var httpRequest = buildRequest("GET", "/game", request, request.authToken());
            var response = sendRequest(httpRequest);
            return handleResponse(response, ListGamesResult.class);
        } catch (ResponseException ex) {
            throw new ResponseException("Failed to list games: " + ex.getMessage(), ex);
        }
    }

    public JoinGameResult joinGame(JoinGameRequest request) {
        try {
            var httpRequest = buildRequest("PUT", "/game", request, request.authToken());
            var response = sendRequest(httpRequest);
            return handleResponse(response, JoinGameResult.class);
        } catch (ResponseException ex) {
            throw new ResponseException("Failed to join game: " + ex.getMessage(), ex);
        }
    }

    public ClearResult clear(ClearRequest request) {
        try {
            var httpRequest = buildRequest("DELETE", "/db", request, null);
            var response = sendRequest(httpRequest);
            return handleResponse(response, ClearResult.class);
        } catch (ResponseException ex) {
            throw new ResponseException("Failed to clear: " + ex.getMessage(), ex);
        }
    }

    private HttpRequest buildRequest(String method, String path, Object body, String authToken) {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + path))
                .method(method, makeRequestBody(body));
        if (body != null) {
            request.setHeader("Content-Type", "application/json");
        }
        if (authToken != null) {
            request.setHeader("Authorization", authToken);
        }
        return request.build();
    }

    private HttpRequest.BodyPublisher makeRequestBody(Object request) {
        if (request != null) {
            return HttpRequest.BodyPublishers.ofString(new Gson().toJson(request));
        } else {
            return HttpRequest.BodyPublishers.noBody();
        }
    }

    private HttpResponse<String> sendRequest(HttpRequest request) throws ResponseException {
        try {
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception ex) {
            throw new ResponseException(ex.getMessage());
        }
    }

    private <T> T handleResponse(HttpResponse<String> response, Class<T> responseClass) throws ResponseException {
        var status = response.statusCode();
        if (!isSuccessful(status)) {
            throw new ResponseException("ServerFacade handler unsuccessful");
        }
        if (responseClass != null) {
            return new Gson().fromJson(response.body(), responseClass);
        }
        return null;
    }

    private boolean isSuccessful(int status) {return status / 100 == 2;}
}
