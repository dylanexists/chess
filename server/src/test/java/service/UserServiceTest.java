package service;

import dataaccess.DataAccessException;
import dataaccess.MemoryAuthDao;
import dataaccess.MemoryUserDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import service.request.RegisterRequest;
import service.result.RegisterResult;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {
    private UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService(new MemoryUserDao(), new MemoryAuthDao());
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
}
