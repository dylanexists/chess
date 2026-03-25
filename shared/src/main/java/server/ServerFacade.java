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

    public RegisterResult register(RegisterRequest request) {
        return null;
    }

    public LoginResult login(LoginRequest request) {
        return null;
    }

    public LogoutResult logout(LogoutRequest request) {
        return null;
    }

    public CreateGameResult createGame(CreateGameRequest request) {
        return null;
    }

    public ListGamesResult listGames(ListGamesRequest request) {
        return null;
    }

    public JoinGameResult joinGame(JoinGameRequest request) {
        return null;
    }

    public ClearResult clear(ClearRequest request) {
        return null;
    }

    private HttpRequest buildRequest(String method, String path, Object body) {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + path))
                .method(method, makeRequestBody(body));
        if (body != null) {
            request.setHeader("Content-Type", "application/json");
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
