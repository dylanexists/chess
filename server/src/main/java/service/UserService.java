package service;

import dataaccess.DuplicateException;
import dataaccess.MemoryUserDao;
import dataaccess.NotFoundException;
import model.UserData;
import service.request.RegisterRequest;
import service.result.RegisterResult;

public class UserService {
    private final MemoryUserDao userDao;

    public UserService(MemoryUserDao userDao){
        this.userDao = userDao;
    }

    public RegisterResult register(RegisterRequest request) {
        String username = request.username();
        String password = request.password();
        String email = request.email();

        if (username == null || password == null || email == null){ //Validate Inputs
            return new RegisterResult(null, null, "Missing Register Inputs");
        }
        try { //Check if username is already taken
            userDao.getUser(username);
            return new RegisterResult(null, null, "This username is taken");
        }
        catch (NotFoundException nfExcept) { //User not found, safe to create new User with given username
            UserData newUser = new UserData(username, password, email);
            try { //success case
                userDao.createUser(newUser);
                return new RegisterResult(username, "1234", null); //TODO, implement authToken logic
            }
            catch (DuplicateException dExcept) { //User already exists (shouldn't happen, but we handle it)
                return new RegisterResult(null, null, dExcept.toString());
            }
        }
    }
}
