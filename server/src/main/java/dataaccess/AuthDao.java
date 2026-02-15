package dataaccess;

import model.AuthData;

import javax.xml.crypto.Data;

public interface AuthDao {

    void clearAuths() throws QueryException;

    AuthData createAuth(AuthData a) throws DuplicateException;

    AuthData getAuth(String authToken) throws NotFoundException;

    void deleteAuth(AuthData a) throws DataAccessException;
}
