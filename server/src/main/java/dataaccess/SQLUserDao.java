package dataaccess;

import model.UserData;

import java.sql.SQLException;

public class SQLUserDao implements UserDao{

    public SQLUserDao(){
        try {
            configureDatabase();
        } catch (DataAccessException e) {
            System.err.println("AuthDao table initialization failed");
        }
    }

    @Override
    public void clearUsers() throws QueryException{}

    @Override
    public UserData createUser(UserData u) throws DuplicateException{return new UserData("","","");}


    @Override
    public UserData getUser(String username) throws NotFoundException{return new UserData("","","");}

    @Override
    public boolean existsUser(String username){return true;};

    private final String createStatement =
            """
                    CREATE TABLE IF NOT EXISTS users (
                    username varchar(256) NOT NULL,
                    password varchar(256) NOT NULL,
                    email varchar(256) NOT NULL,
                    PRIMARY KEY (username)
                    )
                    """;

    private void configureDatabase() throws DataAccessException {
        var conn = DatabaseManager.getConnection(); //throws DataAccessException
        try (var preparedStatement = conn.prepareStatement(createStatement);){
            preparedStatement.executeUpdate();
        } catch (SQLException e){
            throw new DataAccessException("UserDao's create statement failed");
        }
    }
}