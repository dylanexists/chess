package dataaccess;

import model.UserData;

public interface UserDao {

    void clearUsers() throws QueryException;

    UserData createUser(UserData u) throws DuplicateException, QueryException;

    UserData getUser(String username) throws NotFoundException, QueryException;

    boolean verifyUser(String username, String password) throws NotFoundException, QueryException;

    boolean existsUser(String username) throws QueryException;
}
