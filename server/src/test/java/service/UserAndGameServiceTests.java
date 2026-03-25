package service;

import dataaccess.MemoryAuthDao;
import dataaccess.MemoryGameDao;
import dataaccess.MemoryUserDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import request.*;
import result.*;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class UserAndGameServiceTests {
    private UserService userService;
    private GameService gameService;

    @BeforeEach
    void setUp() {
        MemoryAuthDao authDao = new MemoryAuthDao();
        userService = new UserService(new MemoryUserDao(), authDao);
        gameService = new GameService(new MemoryGameDao(), authDao);
    }

    @Test
    @DisplayName("Clear Test")
    public void clearSuccess(){
        userService.register(new RegisterRequest("test123", "password1", "email1@email.com"));
        userService.register(new RegisterRequest("test246", "password2", "email2@email.com"));
        userService.register(new RegisterRequest("test369", "password3", "email3@email.com"));
        ClearResult result = userService.clear();
        assertNull(result.message());
    }

    @Test
    @DisplayName("Register Positive Test")
    public void registerSuccess(){
        RegisterRequest testRequest = new RegisterRequest("test123", "password1", "email@email.com");
        RegisterResult result = userService.register(testRequest);
        assertEquals(testRequest.username(), result.username());
        assertNotNull(result.authToken());
        assertNull(result.message());
    }

    @ParameterizedTest
    @DisplayName("Register Negative Test - Invalid User Credentials")
    @MethodSource("invalidRegisterRequests")
    public void registerFailMissingInputs(RegisterRequest request) {
        //Result for each invalid input of Stream below
        RegisterResult result = userService.register(request);
        //Assert that username and authToken are empty, and error message is correct
        assertNull(result.username());
        assertNull(result.authToken());
        assertEquals("Error: bad request", result.message());
    }
    private static Stream<RegisterRequest> invalidRegisterRequests() {
        return Stream.of(
            new RegisterRequest(null, "password1", "email@email.com"),
            new RegisterRequest("test123", null, "email@email.com"),
            new RegisterRequest("test123", "password1", null),
            new RegisterRequest(null, null, null)
        );
    }

    @Test
    @DisplayName("Register Negative Test - User Already Exists")
    public void registerFailUserAlreadyExists(){
        RegisterRequest preExistingRequest =
                new RegisterRequest("test123", "alreadyExists", "preexist@email.com");
        userService.register(preExistingRequest);
        RegisterRequest testRequest =
                new RegisterRequest("test123", "password1", "email@email.com");
        RegisterResult result = userService.register(testRequest);
        assertNull(result.username());
        assertNull(result.authToken());
        assertEquals("Error: already taken", result.message());
    }

    @Test
    @DisplayName("Login Positive Test")
    public void loginSuccess(){
        RegisterRequest registeredUser = new RegisterRequest("validUser", "realPassword", "email@email.com");
        userService.register(registeredUser);
        LoginRequest testRequest = new LoginRequest("validUser", "realPassword");
        LoginResult result = userService.login(testRequest);
        assertEquals(registeredUser.username(), result.username());
        assertNotNull(result.authToken());
        assertNull(result.message());
    }

    @ParameterizedTest
    @DisplayName("Register Negative Test - Invalid User Credentials")
    @MethodSource("invalidLoginRequests")
    public void loginFailMissingInputs(LoginRequest request) {
        //Result for each invalid input of Stream below
        LoginResult result = userService.login(request);
        //Assert that username and authToken are empty, and error message is correct
        assertNull(result.username());
        assertNull(result.authToken());
        assertEquals("Error: bad request", result.message());
    }
    private static Stream<LoginRequest> invalidLoginRequests() {
        return Stream.of(
                new LoginRequest(null, "password1"),
                new LoginRequest("test123", null),
                new LoginRequest(null, null)
        );
    }

    @Test
    @DisplayName("Login Negative Test - User Doesn't Exists")
    public void loginFailUserDoesNotExists(){
        LoginRequest nonExistentLogin =
                new LoginRequest("IAmNotReal", "doesNotExist");
        LoginResult result = userService.login(nonExistentLogin);
        assertNull(result.username());
        assertNull(result.authToken());
        assertEquals("Error: unauthorized", result.message());
    }

    @Test
    @DisplayName("Logout Positive Test")
    public void logoutSuccess(){
        RegisterRequest registeredUser = new RegisterRequest("validUser", "realPassword", "email@email.com");
        userService.register(registeredUser);
        LoginRequest testRequest = new LoginRequest("validUser", "realPassword");
        LoginResult loginResult = userService.login(testRequest);
        String authToken = loginResult.authToken();
        LogoutResult result = userService.logout(new LogoutRequest(authToken));
        assertNull(result.message());
    }

    @ParameterizedTest
    @DisplayName("Logout Negative Test - Invalid AuthTokens")
    @MethodSource("invalidLogoutRequests")
    public void logoutInvalidAuthTokens(LogoutRequest request) {
        //Result for each invalid input of Stream below
        LogoutResult result = userService.logout(request);
        //Assert error message is correct
        assertEquals("Error: unauthorized", result.message());
    }
    private static Stream<LogoutRequest> invalidLogoutRequests() {
        return Stream.of(
                new LogoutRequest("thisAuthTokenDoesNotExist"),
                new LogoutRequest(""),
                new LogoutRequest(null)
        );
    }

    @Test
    @DisplayName("Create Game Positive Test")
    public void createGameSuccess(){
        RegisterRequest registeredUser = new RegisterRequest("validUser", "realPassword", "email@email.com");
        userService.register(registeredUser);
        LoginRequest testRequest = new LoginRequest("validUser", "realPassword");
        LoginResult loginResult = userService.login(testRequest);
        String authToken = loginResult.authToken();
        CreateGameResult result = gameService.createGame(new CreateGameRequest(authToken, "testGame"));
        assertNotNull(result.gameID());
        assertNull(result.message());
    }

    @ParameterizedTest
    @DisplayName("Create Game Negative Test - Invalid Inputs")
    @MethodSource("invalidCreateGameRequests")
    public void logoutInvalidAuthTokens(CreateGameRequest request) {
        //Result for each invalid input of Stream below
        CreateGameResult result = gameService.createGame(request);
        //Assert error message is correct
        assertNull(result.gameID());
        assertEquals("Error: unauthorized", result.message());
    }
    private static Stream<CreateGameRequest> invalidCreateGameRequests() {
        return Stream.of(
                new CreateGameRequest("thisAuthTokenDoesNotExist", "game1"),
                new CreateGameRequest("", "game2"),
                new CreateGameRequest(null, "game3")
        );
    }

    @Test
    @DisplayName("List Games Positive Test")
    public void listGamesSuccess(){
        RegisterRequest registeredUser = new RegisterRequest("validUser", "realPassword", "email@email.com");
        userService.register(registeredUser);
        LoginRequest testRequest = new LoginRequest("validUser", "realPassword");
        LoginResult loginResult = userService.login(testRequest);
        String authToken = loginResult.authToken();
        gameService.createGame(new CreateGameRequest(authToken, "testGame"));
        ListGamesResult result = gameService.listGames(new ListGamesRequest(authToken));
        assertNotNull(result.gamesList());
        assertNull(result.message());
    }

    @ParameterizedTest
    @DisplayName("List Games Negative Test - Invalid Inputs")
    @MethodSource("invalidListGamesRequests")
    public void listGamesInvalidAuthTokens(ListGamesRequest request) {
        //Result for each invalid input of Stream below
        ListGamesResult result = gameService.listGames(request);
        //Assert error message is correct
        assertNull(result.gamesList());
        assertEquals("Error: unauthorized", result.message());
    }
    private static Stream<ListGamesRequest> invalidListGamesRequests() {
        return Stream.of(
                new ListGamesRequest("thisAuthTokenDoesNotExist"),
                new ListGamesRequest(""),
                new ListGamesRequest(null)
        );
    }

    @Test
    @DisplayName("Join Game Positive Test")
    public void joinGameSuccess(){
        RegisterRequest registeredUser = new RegisterRequest("validUser", "realPassword", "email@email.com");
        userService.register(registeredUser);
        LoginRequest testRequest = new LoginRequest("validUser", "realPassword");
        LoginResult loginResult = userService.login(testRequest);
        String authToken = loginResult.authToken();
        CreateGameResult newGame = gameService.createGame(new CreateGameRequest(authToken, "testGame"));
        Integer gameID = newGame.gameID();
        JoinGameResult result = gameService.joinGame(new JoinGameRequest(authToken, "WHITE", gameID));
        assertNull(result.message());

        RegisterRequest registeredUser2 = new RegisterRequest("validUser2", "realPassword2", "email2@email.com");
        userService.register(registeredUser2);
        LoginRequest testRequest2 = new LoginRequest("validUser2", "realPassword2");
        LoginResult loginResult2 = userService.login(testRequest2);
        String authToken2 = loginResult2.authToken();
        JoinGameResult result2 = gameService.joinGame(new JoinGameRequest(authToken2, "BLACK", gameID));
        assertNull(result2.message());

    }

    @Test
    @DisplayName("Join Game Negative Test")
    public void joinGameFailure(){
        RegisterRequest registeredUser = new RegisterRequest("validUser", "realPassword", "email@email.com");
        userService.register(registeredUser);
        LoginRequest testRequest = new LoginRequest("validUser", "realPassword");
        LoginResult loginResult = userService.login(testRequest);
        String authToken = loginResult.authToken();
        CreateGameResult newGame = gameService.createGame(new CreateGameRequest(authToken, "testGame"));
        Integer gameID = newGame.gameID();
        JoinGameResult result = gameService.joinGame(new JoinGameRequest(authToken, "WHITE", gameID));
        assertNull(result.message());

        RegisterRequest registeredUser2 = new RegisterRequest("validUser2", "realPassword2", "email2@email.com");
        userService.register(registeredUser2);
        LoginRequest testRequest2 = new LoginRequest("validUser2", "realPassword2");
        LoginResult loginResult2 = userService.login(testRequest2);
        String authToken2 = loginResult2.authToken();
        JoinGameResult result2 = gameService.joinGame(new JoinGameRequest(authToken2, "WHITE", gameID));
        assertNotNull(result2.message());
        assertEquals("Error: already taken", result2.message());

        JoinGameResult result3 = gameService.joinGame(new JoinGameRequest(authToken2, "WHITE", 2)); //random gameID
        assertNotNull(result3.message());
        assertEquals("Error: unauthorized", result3.message());
    }
}
