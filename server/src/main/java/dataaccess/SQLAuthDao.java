package dataaccess;

import model.AuthData;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class SQLAuthDao extends SQLBaseDao implements AuthDao{

    public SQLAuthDao(){
        try {
            configureDatabase(createStatement);
        } catch (DataAccessException e) {
            System.err.println("AuthDao table initialization failed");
        }
    }


    @Override
    public void clearAuths() throws QueryException{
        String clearStatement = "TRUNCATE TABLE auths;";
        try {
            execUpdateStatement(clearStatement);
        } catch (DataAccessException e){
            throw new QueryException("AuthDao's clear statement failed", e);
        }
    }

    @Override
    public AuthData createAuth(AuthData a) throws QueryException, DuplicateException{
        if (existsAuth(a.authToken())) {throw new DuplicateException("Duplicate");}
        String insertAuthStatement = """
                INSERT INTO auths (authToken, username)
                VALUES (?,?)
                """;
        String authToken = a.authToken();
        String username = a.username();

        try {
            execUpdateStatement(insertAuthStatement, prepState -> {
                prepState.setString(1, authToken);
                prepState.setString(2, username);
            });
            return a;
        } catch (QueryException e){
            throw new QueryException("AuthDao's create statement failed", e);
        }
    }

    @Override
    public AuthData getAuth(String authToken) throws NotFoundException, QueryException{
        String retrieveAuthStatement = """
                SELECT * FROM auths
                WHERE authToken = ?;
                """;
        try {
            List<AuthData> authList = execQueryStatement(
                    retrieveAuthStatement,
                    prepState -> prepState.setString(1, authToken),
                    resSet -> readAuth(resSet));
            if (authList.isEmpty()){
                throw new NotFoundException("Not found");
            }
            return authList.getFirst();
        } catch (QueryException e){
            throw new QueryException("AuthDao's get statement failed", e);
        }
    }

    @Override
    public void deleteAuth(AuthData a) throws DataAccessException{}

    @Override
    public boolean existsAuth(String authToken) throws QueryException{
        try {
            getAuth(authToken);
            return true;
        } catch (NotFoundException e) {
            return false;
        }
    }

    private final String createStatement =
            """
                    CREATE TABLE IF NOT EXISTS auths (
                    authToken varchar(256) NOT NULL,
                    username varchar(256) NOT NULL,
                    PRIMARY KEY (authToken)
                    )
                    """;

    private AuthData readAuth(ResultSet rs) throws SQLException {
        var authToken = rs.getString("authToken");
        var username = rs.getString("username");
        return new AuthData(authToken, username);
    }
}