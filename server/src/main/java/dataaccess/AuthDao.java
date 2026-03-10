package dataaccess;

import model.AuthData;

import javax.xml.crypto.Data;

public interface AuthDao {

    void clearAuths() throws QueryException;

    AuthData createAuth(AuthData a) throws DuplicateException, QueryException;

    AuthData getAuth(String authToken) throws NotFoundException, QueryException;

    void deleteAuth(AuthData a) throws DataAccessException;

    boolean existsAuth(String authToken) throws QueryException;
}
