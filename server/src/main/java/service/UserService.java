package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;
import service.request.LoginRequest;
import service.request.LogoutRequest;
import service.request.RegisterRequest;
import service.result.ClearResult;
import service.result.LoginResult;
import service.result.LogoutResult;
import service.result.RegisterResult;

import java.util.UUID;

public class UserService {
    private final UserDao userDao;
    private final AuthDao authDao;

    public UserService(UserDao userDao, AuthDao authDao){
        this.userDao = userDao;
        this.authDao = authDao;
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

    public RegisterResult register(RegisterRequest request) {
        if (request == null) {return new RegisterResult(null, null, "Invalid request");}
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
                return new RegisterResult(null, null, "Internal server error");
            }
        }
    }

    public LoginResult login(LoginRequest request) {
        if (request == null) {return new LoginResult(null, null, "Invalid request");}
        String username = request.username();
        String password = request.password();

        if (username == null || password == null){ //Validate Inputs
            return new LoginResult(null, null, "Missing Login Inputs");
        }
        try { //Check if username exists
            UserData user = userDao.getUser(username); //will try to get User
            if (password.equals(user.password())){ //verify password
                String token = UUID.randomUUID().toString();
                AuthData newAuth = new AuthData(token, username);
                authDao.createAuth(newAuth); //will try to create Auth
                return new LoginResult(username, token, null);
            } else {
                return new LoginResult(null, null, "Incorrect username or password");
            }
        } catch (NotFoundException nfExcept){
            return new LoginResult(null, null, "Incorrect username or password");
        } catch (DuplicateException dExcept) { //User or Auth already exists (shouldn't happen, but we handle it)
            return new LoginResult(null, null, "Internal server error");
        }
    }

    public LogoutResult logout(LogoutRequest request) {
        if (request == null) {return new LogoutResult("Invalid request");}
        String authToken = request.authToken();

        if (authToken == null || authToken.isEmpty()){ //Validate Inputs
            return new LogoutResult("Missing Authentication Token");
        }
        try {
            AuthData currentSession = authDao.getAuth(authToken);
            authDao.deleteAuth(currentSession);
            return new LogoutResult(null); //success
        } catch (NotFoundException e) {
            return new LogoutResult("Invalid authentication inputs");
        } catch (DataAccessException e) {
            return new LogoutResult("Internal server error");
        }
    }

}
