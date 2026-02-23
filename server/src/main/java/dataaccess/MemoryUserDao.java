package dataaccess;

import java.util.HashMap;

import model.UserData;


public class MemoryUserDao implements UserDao{
    final private HashMap<String, UserData> users = new HashMap<>();

    @Override
    public void clearUsers() throws QueryException {
        users.clear();
    }

    @Override
    public UserData createUser(UserData u) throws DuplicateException {
        if (existsUser(u.username())){
            throw new DuplicateException("Username already exists");
        }

        users.put(u.username(), u);
        return u;
    }

    @Override
    public UserData getUser(String username) throws NotFoundException {
        UserData user = users.get(username);
        if (user == null){
            throw new NotFoundException("UserData not found");
        }
        return user;
    }

    @Override
    public boolean existsUser(String username){
        return users.containsKey(username);
    }
}
