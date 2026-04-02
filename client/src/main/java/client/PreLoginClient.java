package client;

import client.facade.ServerFacade;
import request.LoginRequest;
import request.RegisterRequest;
import result.LoginResult;
import result.RegisterResult;
import facade.ResponseException;

import java.util.Arrays;
import java.util.Scanner;

public class PreLoginClient {
    private final ServerFacade serverFacade;

    public PreLoginClient(ServerFacade serverFac) throws ResponseException {
        serverFacade = serverFac;
    }

    public PreLoginResult run() {
        System.out.println("♕ Welcome to 240 chess. Type Help to get started. ♕");
        System.out.println(help());

        Scanner scanner = new Scanner(System.in);
        PreLoginResult result;
        while (true) {
            printPrompt();
            String line = scanner.nextLine();

            try {
                result = eval(line);
                System.out.println(result.cmdResult());
                if (result.nextState() != ClientRepl.ClientState.PRE_LOGIN) {return result;}
            } catch (ResponseException ex) {
                throw new ResponseException("- PreLogin command " + line + " failed - " + ex.getMessage(), ex);
            }
        }
    }

    private void printPrompt() {
        System.out.print("[LOGGED_OUT] >>> ");
    }

    public PreLoginResult eval(String input) {
        String[] tokens = input.split(" ");
        String cmd = (tokens.length > 0) ? tokens[0] : "help";
        String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
        return switch (cmd) {
            case "register" -> register(params);
            case "login" -> login(params);
            case "quit" -> new PreLoginResult("", ClientRepl.ClientState.EXIT, null);
            default -> new PreLoginResult(help(), ClientRepl.ClientState.PRE_LOGIN, null);
        };
    }

    public PreLoginResult register (String... params) throws ResponseException {
        if (params.length == 3) {
            try {
                String username = params[0];
                String password = params[1];
                String email = params[2];
                RegisterResult registerResult = serverFacade.register(new RegisterRequest(username, password, email));
                String authToken = registerResult.authToken();
                return successfulLogin(username, authToken);
            } catch (ResponseException ex) {
                return new PreLoginResult("Register Error - Username might already be taken, try a new one.",
                        ClientRepl.ClientState.PRE_LOGIN, null);}
        }
        return registerError();
    }

    private PreLoginResult registerError() {
        String cmdResult = """
                Register Error - Expected: register <USERNAME> <PASSWORD> <EMAIL>
                Each input should be one word, no spaces.""";
        return new PreLoginResult(cmdResult, ClientRepl.ClientState.PRE_LOGIN, null);
    }

    public PreLoginResult login (String... params) throws ResponseException {
        if (params.length == 2) {
            try {
                String username = params[0];
                String password = params[1];
                LoginResult loginResult = serverFacade.login(new LoginRequest(username, password));
                String authToken = loginResult.authToken();
                return successfulLogin(username, authToken);
            } catch (ResponseException ex) {return login();}
        }
        return loginError();
    }

    private PreLoginResult loginError() {
        String cmdResult = """
                Login Error - Expected: login <USERNAME> <PASSWORD>
                Make sure username and password are spelled correctly.
                If you do not have an account, type 'help' to learn how to use 'register'.""";
        return new PreLoginResult(cmdResult, ClientRepl.ClientState.PRE_LOGIN, null);
    }

    private PreLoginResult successfulLogin(String username, String authToken) {
        return new PreLoginResult("Logged in as " + username, ClientRepl.ClientState.POST_LOGIN, authToken);
    }

    public String help() {
        return """
                register <USERNAME> <PASSWORD> <EMAIL> - to create an account
                login <USERNAME> <PASSWORD> - to play chess
                quit - playing chess
                help - with possible commands
                """;
    }
}
