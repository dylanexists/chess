package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;
import service.request.RegisterRequest;
import service.result.ClearResult;
import service.result.RegisterResult;

import java.util.UUID;

public class UserService {
    private final UserDao userDao;
    private final AuthDao authDao;

    public UserService(UserDao userDao, AuthDao authDao){
        this.userDao = userDao;
        this.authDao = authDao;
    }

    public RegisterResult register(RegisterRequest request) {
        String username = request.username();
        String password = request.password();
        String email = request.email();

        if (username == null || password == null || email == null){ //Validate Inputs
            return new RegisterResult(null, null, "Missing Register Inputs");
        }
        if (userDao.existsUser(username)) { //Check if username is already taken
            return new RegisterResult(null, null, "This username is taken");
        }
        else { //User not found, safe to create new User with given username
            try { //success case
                UserData newUser = new UserData(username, password, email);
                userDao.createUser(newUser); //will try to create User
                String token = UUID.randomUUID().toString();
                AuthData newAuth = new AuthData(token, username);
                authDao.createAuth(newAuth); //will try to create Auth
                return new RegisterResult(username, token, null);
            }
            catch (DuplicateException dExcept) { //User or Auth already exists (shouldn't happen, but we handle it)
                return new RegisterResult(null, null, dExcept.toString());
            }
        }
    }
    public ClearResult clear() {
        try {
            userDao.clearUsers();
            authDao.clearAuths();
            return new ClearResult(null);
        }
        catch (QueryException qExcept){
            return new ClearResult(qExcept.toString());
        }
    }
}
