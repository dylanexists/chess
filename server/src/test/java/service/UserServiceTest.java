package service;

import dataaccess.MemoryAuthDao;
import dataaccess.MemoryUserDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import service.request.RegisterRequest;
import service.result.ClearResult;
import service.result.RegisterResult;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {
    private UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService(new MemoryUserDao(), new MemoryAuthDao());
    }

    @Test
    @DisplayName("Clear Test")
    public void clearSuccess(){
        service.register(new RegisterRequest("test123", "password1", "email1@email.com"));
        service.register(new RegisterRequest("test246", "password2", "email2@email.com"));
        service.register(new RegisterRequest("test369", "password3", "email3@email.com"));
        ClearResult result = service.clear();
        assertNull(result.message());
    }

    @Test
    @DisplayName("Register Positive Test")
    public void registerSuccess(){
        RegisterRequest testRequest = new RegisterRequest("test123", "password1", "email@email.com");
        RegisterResult result = service.register(testRequest);
        assertEquals(testRequest.username(), result.username());
        assertNotNull(result.authToken());
        assertNull(result.message());
    }

    @ParameterizedTest
    @DisplayName("Register Negative Test - Invalid User Credentials")
    @MethodSource("invalidRegisterRequests")
    public void registerFailMissingInputs(RegisterRequest request) {
        //Result for each invalid input of Stream below
        RegisterResult result = service.register(request);
        //Assert that username and authToken are empty, and error message is correct
        assertNull(result.username());
        assertNull(result.authToken());
        assertEquals("Missing Register Inputs", result.message());
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
                new RegisterRequest("test123", "alreadyexists", "preexist@email.com");
                service.register(preExistingRequest);
        RegisterRequest testRequest =
                new RegisterRequest("test123", "password1", "email@email.com");
        RegisterResult result = service.register(testRequest);
        assertNull(result.username());
        assertNull(result.authToken());
        assertEquals("This username is taken", result.message());
    }

}
