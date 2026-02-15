package dataaccess;

import model.UserData;

public interface UserDao {

    void clearUsers() throws QueryException;

    UserData createUser(UserData u) throws DuplicateException;

    UserData getUser(String username) throws NotFoundException;
}
