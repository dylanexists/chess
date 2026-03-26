package client;

import org.junit.jupiter.api.*;
import request.*;
import result.*;
import server.ResponseException;
import server.Server;
import server.ServerFacade;

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
}
