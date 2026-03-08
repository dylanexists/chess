package dataaccess;

import model.UserData;

import java.sql.SQLException;

public class SQLUserDao extends SQLBaseDao implements UserDao{

    public SQLUserDao(){
        try {
            configureDatabase(createStatement);
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
}