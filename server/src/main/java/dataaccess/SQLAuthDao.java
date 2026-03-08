package dataaccess;

import model.AuthData;

import java.sql.SQLException;

public class SQLAuthDao implements AuthDao{

    public SQLAuthDao(){
        try {
            configureDatabase();
        } catch (DataAccessException e) {
            System.err.println("AuthDao table initialization failed");
        }
    }


    @Override
    public void clearAuths() throws QueryException{}

    @Override
    public AuthData createAuth(AuthData a) throws DuplicateException{return new AuthData("","");}

    @Override
    public AuthData getAuth(String authToken) throws NotFoundException{return new AuthData("","");}

    @Override
    public void deleteAuth(AuthData a) throws DataAccessException{}

    @Override
    public boolean existsAuth(String authToken){return true;}

    private final String createStatement =
            """
                    CREATE TABLE IF NOT EXISTS authtokens (
                    authToken varchar(256) NOT NULL,
                    username varchar(256) NOT NULL,
                    PRIMARY KEY (authToken)
                    )
                    """;

    private void configureDatabase() throws DataAccessException {
            var conn = DatabaseManager.getConnection(); //throws DataAccessException
            try (var preparedStatement = conn.prepareStatement(createStatement);){
                preparedStatement.executeUpdate();
            } catch (SQLException e){
                throw new DataAccessException("AuthDao's create statement failed");
            }
    }
}
