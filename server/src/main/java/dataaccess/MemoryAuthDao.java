package dataaccess;

import model.AuthData;

public class MemoryAuthDao implements AuthDao{
    @Override
    public void clearAuths() throws QueryException {

    }

    @Override
    public AuthData createAuth(AuthData a) throws DuplicateException {
        return null;
    }

    @Override
    public AuthData getAuth(String authToken) throws NotFoundException {
        return null;
    }

    @Override
    public void deleteAuth(AuthData a) throws DataAccessException {

    }
}
