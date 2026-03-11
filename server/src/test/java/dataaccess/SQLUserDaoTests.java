package dataaccess;

import model.UserData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SQLUserDaoTests {

    private static SQLUserDao userDao;

    @BeforeAll
    public static void setUpDatabase() {
        try {
            DatabaseManager.createDatabase();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
        userDao = new SQLUserDao();
    }

    @BeforeEach
    void setUp() {
        try {
            userDao.clearUsers();
        } catch (QueryException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("UserDao clearUsers() Positive Test")
    public void clearUserSuccess() {
        assertDoesNotThrow(() -> {
            userDao.createUser(new UserData("user1", "password", "email@email.com"));
            userDao.createUser(new UserData("user2", "password", "lol@email.com"));
            assertTrue(userDao.existsUser("user1"));
            assertTrue(userDao.existsUser("user2"));
            userDao.clearUsers();
            assertFalse(userDao.existsUser("user1"));
            assertFalse(userDao.existsUser("user2"));
        });
    }

    @Test
    @DisplayName("UserDao createUser() Positive Test")
    public void createUserSuccess() {
        assertDoesNotThrow(() -> {
            userDao.createUser(new UserData("user1", "password", "email@email.com"));
            assertTrue(userDao.existsUser("user1"));
            userDao.createUser(new UserData("user2", "password", "lol@email.com"));
            assertTrue(userDao.existsUser("user2"));
        });
    }

    @Test
    @DisplayName("UserDao createUser() Negative Test")
    public void createAuthFailure() {
        assertThrows(DuplicateException.class, () -> {
            userDao.createUser(new UserData("user", "password", "email@email.com"));
            assertTrue(userDao.existsUser("user"));
            userDao.createUser(new UserData("user", "again", "again@email.com"));
        });
    }

    @Test
    @DisplayName("UserDao getUser() Positive Test")
    public void getUserSuccess() {
        assertDoesNotThrow(() -> {
            userDao.createUser(new UserData("user1", "12345", "x@gmail.com"));
            userDao.getUser("user1");
            userDao.createUser(new UserData("person", "super", "y@gmail.com"));
            userDao.getUser("person");
            userDao.getUser("user1");
        });
    }

    @Test
    @DisplayName("UserDao getUser() Negative Test")
    public void getUserFailure() {
        assertThrows(NotFoundException.class, () -> {
            userDao.createUser(new UserData("user1", "12345", "x@gmail.com"));
            userDao.getUser("noExist");
        });
    }



}
