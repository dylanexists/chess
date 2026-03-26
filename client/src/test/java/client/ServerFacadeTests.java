package client;

import org.junit.jupiter.api.*;
import request.ClearRequest;
import request.RegisterRequest;
import result.ClearResult;
import result.RegisterResult;
import server.ResponseException;
import server.Server;
import server.ServerFacade;

import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade serverFacade;

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
    public void setUp(){
        serverFacade.clear(new ClearRequest());
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

}
