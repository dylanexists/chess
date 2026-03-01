package dataaccess;

import model.AuthData;
import model.UserData;

import java.util.HashMap;
import java.util.UUID;

public class MemoryAuthDao implements AuthDao{
    final private HashMap<String, AuthData> authTokens = new HashMap<>();

    @Override
    public void clearAuths() throws QueryException {
        authTokens.clear();
    }

    @Override
    public AuthData createAuth(AuthData a) throws DuplicateException {
        if (authTokens.containsKey(a.authToken())){
            throw new DuplicateException("Auth token already exists");
        }
        authTokens.put(a.authToken(), a);
        return a;
    }

    @Override
    public AuthData getAuth(String authToken) throws NotFoundException {
        AuthData data = authTokens.get(authToken);
        if (data == null){
            throw new NotFoundException("Auth doesn't exist");
        }
        return data;
    }

    @Override
    public void deleteAuth(AuthData a) throws NotFoundException {
        if (a == null){
            throw new NotFoundException("Auth doesn't exist");
        }
        authTokens.remove(a.authToken());
    }
}
