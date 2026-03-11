package dataaccess;

import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class SQLUserDao extends SQLBaseDao implements UserDao{

    public SQLUserDao(){
        try {
            configureDatabase(createStatement);
        } catch (DataAccessException e) {
            System.err.println("UserDao table initialization failed");
        }
    }

    @Override
    public void clearUsers() throws QueryException{
        String clearStatement = "TRUNCATE TABLE users;";
        try {
            execUpdateStatement(clearStatement);
        } catch (QueryException e) {
            throw new QueryException("UserDao's clear statement failed", e);
        }
    }

    @Override
    public UserData createUser(UserData u) throws DuplicateException, QueryException{
        if (existsUser(u.username())) {throw new DuplicateException("Duplicate");}
        String insertUserStatement = """
                INSERT INTO users (username, password, email)
                VALUES (?, ?, ?)
                """;
        String hashedPassword = BCrypt.hashpw(u.password(), BCrypt.gensalt());
        try {
            execUpdateStatement(insertUserStatement, prepState -> {
                prepState.setString(1, u.username());
                prepState.setString(2, hashedPassword);
                prepState.setString(3, u.email());
            });
            return u;
        } catch (QueryException e) {
            throw new QueryException("UserDao's create statement failed", e);
        }
    }


    @Override
    public UserData getUser(String username) throws NotFoundException, QueryException{
        String retrieveUserStatement = """
                SELECT * FROM users
                WHERE username = ?;
                """;
        try {
            List<UserData> userList = execQueryStatement(
                    retrieveUserStatement,
                    prepState -> prepState.setString(1, username),
                    resSet -> readUser(resSet));
            if (userList.isEmpty()){
                throw new NotFoundException("Not found");
            }
            return userList.getFirst();
        } catch (QueryException e) {
            throw new QueryException("UserDao's get statement failed", e);
        }
    }

    @Override
    public boolean verifyUser(String username, String password) throws NotFoundException, QueryException{
        UserData user = getUser(username);
        var hashedPassword = user.password();
        return BCrypt.checkpw(password, hashedPassword);
    }

    @Override
    public boolean existsUser(String username) throws QueryException{
        try {
            getUser(username);
            return true;
        } catch (NotFoundException e) {
            return false;
        }
    };

    private final String createStatement =
            """
                    CREATE TABLE IF NOT EXISTS users (
                    username varchar(256) NOT NULL,
                    password varchar(256) NOT NULL,
                    email varchar(256) NOT NULL,
                    PRIMARY KEY (username)
                    )
                    """;

    private UserData readUser(ResultSet rs) throws SQLException {
        var username = rs.getString("username");
        var password = rs.getString("password");
        var email = rs.getString("email");
        return new UserData(username, password, email);
    }
}