package client;

import org.junit.jupiter.api.*;
import request.*;
import result.*;
import server.ResponseException;
import server.Server;
import server.ServerFacade;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade serverFacade;
    private static String existingUsername = "UnitTestDemoUser";

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        var url = "http://localhost:" + port;
        serverFacade = new ServerFacade(url);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    public void setUp() {
        serverFacade.clear(new ClearRequest());
        serverFacade.register(new RegisterRequest(existingUsername, "password", "demo@gmail.com"));
    }

    @Test
    @DisplayName("Clear ServerFacade Positive Test")
    public void clearSuccess() {
        RegisterRequest registerRequest = new RegisterRequest("username", "password", "email@email.com");
        serverFacade.register(registerRequest);
        RegisterRequest registerRequest2 = new RegisterRequest("username2", "password2", "email2@email.com");
        serverFacade.register(registerRequest2);
        ClearResult result = serverFacade.clear(new ClearRequest());
        assertNull(result.message());
    }

    @Test
    @DisplayName("Register ServerFacade Positive Test")
    public void registerSuccess() {
        String username = "testUser";
        String password = "testPass";
        String email = "testUser@gmail.com";
        RegisterRequest registerRequest = new RegisterRequest(username, password, email);
        RegisterResult result = serverFacade.register(registerRequest);
        assertEquals(username, result.username());
        assertNotNull(result.authToken());
    }

    @Test
    @DisplayName("Register ServerFacade Negative Test")
    public void registerFailure() {
        RegisterRequest registerRequest = new RegisterRequest("username", "password", "email@email.com");
        RegisterResult result = serverFacade.register(registerRequest);
        assertEquals("username", result.username());
        assertNotNull(result.authToken());
        RegisterRequest registerRequest2 = new RegisterRequest("username", "repeat", "repeat@email.com");
        assertThrows(ResponseException.class, () -> {
            RegisterResult result2 = serverFacade.register(registerRequest2);
        });
    }

    @Test
    @DisplayName("Login ServerFacade Positive Test")
    public void loginSuccess() {
        LoginRequest loginRequest = new LoginRequest(existingUsername, "password");
        LoginResult result = serverFacade.login(loginRequest);
        assertEquals(existingUsername, result.username());
        assertNull(result.message());
        assertNotNull(result.authToken());
    }

    @Test
    @DisplayName("Login ServerFacade Negative Test")
    public void loginFailure() {
        LoginRequest loginRequest = new LoginRequest("nonExistentUser", "password");
        assertThrows(ResponseException.class, () -> {
            LoginResult result = serverFacade.login(loginRequest);
        });
    }

    @Test
    @DisplayName("Logout ServerFacade Positive Test")
    public void logoutSuccess() {
        LoginResult lResult = serverFacade.login(new LoginRequest(existingUsername, "password"));
        String authToken = lResult.authToken();
        LogoutRequest request = new LogoutRequest(authToken);
        LogoutResult result = serverFacade.logout(request);
        assertNull(result.message());
    }

    @Test
    @DisplayName("Logout ServerFacade Negative Test")
    public void logoutFailure() {
        LogoutRequest logoutRequest = new LogoutRequest("fakeAuth");
        assertThrows(ResponseException.class, () -> {
            LogoutResult result = serverFacade.logout(logoutRequest);
        });
    }

    @Test
    @DisplayName("CreateGame ServerFacade Positive Test")
    public void createGameSuccess() {
        LoginResult lResult = serverFacade.login(new LoginRequest(existingUsername, "password"));
        String authToken = lResult.authToken();
        String gameName = "testGame";
        CreateGameRequest createGameRequest = new CreateGameRequest(authToken, gameName);
        CreateGameResult result = serverFacade.createGame(createGameRequest);
        assertNotNull(result.gameID());
        assertNull(result.message());
    }

    @Test
    @DisplayName("CreateGame ServerFacade Negative Test")
    public void createGameFailure() {
        String nonExistentAuthToken = "abc123";
        String gameName = "testGame";
        CreateGameRequest createGameRequest = new CreateGameRequest(nonExistentAuthToken, gameName);
        assertThrows(ResponseException.class, () -> {
            CreateGameResult result = serverFacade.createGame(createGameRequest);
        });
    }

    @Test
    @DisplayName("ListGames ServerFacade Positive Test")
    public void listGamesSuccess() {
        LoginResult lResult = serverFacade.login(new LoginRequest(existingUsername, "password"));
        String authToken = lResult.authToken();
        ListGamesRequest request = new ListGamesRequest(authToken);
        ListGamesResult result = serverFacade.listGames(request);
        assertNotNull(result.games());
        assertEquals(0, result.games().size());
        serverFacade.createGame(new CreateGameRequest(authToken, "placeHolderGame"));
        ListGamesResult result2 = serverFacade.listGames(request);
        assertNotNull(result2.games());
        assertEquals(1, result2.games().size());
        assertNull(result2.message());
    }

    @Test
    @DisplayName("ListGames ServerFacade Negative Test")
    public void listGamesFailure() {
        String nonExistentAuthToken = "abc123";
        ListGamesRequest request = new ListGamesRequest(nonExistentAuthToken);
        assertThrows(ResponseException.class, () -> {
            ListGamesResult result = serverFacade.listGames(request);
        });
    }

    @Test
    @DisplayName("JoinGame ServerFacade Positive Test")
    public void joinGameSuccess() {
        LoginResult lResult = serverFacade.login(new LoginRequest(existingUsername, "password"));
        String authToken = lResult.authToken();
        CreateGameResult gResult = serverFacade.createGame(new CreateGameRequest(authToken, "placeHolderGame"));
        Integer gameID = gResult.gameID();
        JoinGameRequest request = new JoinGameRequest(authToken, "WHITE", gameID);
        JoinGameResult result = serverFacade.joinGame(request);
        assertNull(result.message());
    }

    @Test
    @DisplayName("JoinGame ServerFacade Negative Test")
    public void joinGameFailure() {
        LoginResult lResult = serverFacade.login(new LoginRequest(existingUsername, "password"));
        String authToken = lResult.authToken();
        CreateGameResult gResult = serverFacade.createGame(new CreateGameRequest(authToken, "placeHolderGame"));
        Integer gameID = gResult.gameID();
        JoinGameRequest request = new JoinGameRequest(authToken, "WHITE", gameID);
        JoinGameResult result = serverFacade.joinGame(request);
        assertNull(result.message());
        RegisterResult rResult2 = serverFacade.register(new RegisterRequest("user2", "pass2", "email2@gmail.com"));
        LoginResult lResult2 = serverFacade.login(new LoginRequest(rResult2.username(), "pass2"));
        String authTokenUser2 = lResult2.authToken();
        JoinGameRequest request2 = new JoinGameRequest(authTokenUser2, "WHITE", gameID);
        assertThrows(ResponseException.class, () -> {
            JoinGameResult result2 = serverFacade.joinGame(request2);
        });
    }
}
