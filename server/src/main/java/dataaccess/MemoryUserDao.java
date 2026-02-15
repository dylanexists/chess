package dataaccess;

import model.UserData;

public class MemoryUserDao implements UserDao{
    @Override
    public void clearUsers() throws QueryException {

    }

    @Override
    public UserData createUser(UserData u) throws DuplicateException {
        return null;
    }

    @Override
    public UserData getUser(String username) throws NotFoundException {
        return null;
    }
}
