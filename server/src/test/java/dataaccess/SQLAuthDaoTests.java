package dataaccess;

import model.AuthData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SQLAuthDaoTests {

    private static SQLAuthDao authDao;

    @BeforeAll
    public static void setUpDatabase() {
        try {
            DatabaseManager.createDatabase();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
        authDao = new SQLAuthDao();
    }

    @BeforeEach
    void setUp() {
        try {
            authDao.clearAuths();
        } catch (QueryException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("AuthDao clearAuths() Positive Test")
    public void clearAuthSuccess() {
        assertDoesNotThrow(() -> {
            authDao.createAuth(new AuthData("token", "user"));
            authDao.createAuth(new AuthData("token2", "user2"));
            assertTrue(authDao.existsAuth("token"));
            assertTrue(authDao.existsAuth("token2"));
            authDao.clearAuths();
            assertFalse(authDao.existsAuth("token"));
            assertFalse(authDao.existsAuth("token2"));
        });
    }

    @Test
    @DisplayName("AuthDao createAuth() Positive Test")
    public void createAuthSuccess() {
        assertDoesNotThrow(() -> {
            authDao.createAuth(new AuthData("newToken", "newUser"));
            assertTrue(authDao.existsAuth("newToken"));
            authDao.createAuth(new AuthData("newToken2", "newUser2"));
            assertTrue(authDao.existsAuth("newToken2"));
        });
    }

    @Test
    @DisplayName("AuthDao createAuth() Negative Test")
    public void createAuthFailure() {
        assertThrows(DuplicateException.class, () -> {
            authDao.createAuth(new AuthData("authToken", "username"));
            assertTrue(authDao.existsAuth("authToken"));
            authDao.createAuth(new AuthData("authToken", "username"));
        });
    }

    @Test
    @DisplayName("AuthDao getAuth() Positive Test")
    public void getAuthSuccess() {
        assertDoesNotThrow(() -> {
            authDao.createAuth(new AuthData("123", "existingUser"));
            authDao.getAuth("123");
            authDao.createAuth(new AuthData("456", "existingUser2"));
            authDao.getAuth("456");
            authDao.getAuth("123");
        });
    }

    @Test
    @DisplayName("AuthDao getAuth() Negative Test")
    public void getAuthFailure() {
        assertThrows(NotFoundException.class, () -> {
            authDao.createAuth(new AuthData("123", "existingUser"));
            authDao.getAuth("999");
        });
    }

    @Test
    @DisplayName("AuthDao deleteAuth() Positive Test")
    public void deleteAuthSuccess() {
        assertDoesNotThrow(() -> {
            AuthData auth = new AuthData("123", "willDeleteUser");
            AuthData auth2 = new AuthData("456", "existingUser");
            authDao.createAuth(auth);
            authDao.createAuth(auth2);
            authDao.deleteAuth(auth);
            authDao.getAuth("456");
        });
        assertThrows(NotFoundException.class, () -> {
            authDao.getAuth("123");
        });
    }

    @Test
    @DisplayName("AuthDao deleteAuth() Negative Test")
    public void deleteAuthFailure() {
        assertThrows(NotFoundException.class, () -> {
            AuthData auth = new AuthData("1234", "user");
            AuthData auth2 = new AuthData("5678", "neverMade");
            authDao.createAuth(auth);
            authDao.deleteAuth(auth2);
        });
    }

}