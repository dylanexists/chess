package client;

import request.LoginRequest;
import request.RegisterRequest;
import result.LoginResult;
import result.RegisterResult;
import server.ResponseException;
import server.ServerFacade;

import java.util.Arrays;
import java.util.Scanner;

public class PreLoginClient {
    private boolean active;
    private ServerFacade serverFacade;

    public PreLoginClient(ServerFacade server) throws ResponseException {
        active = false;
        serverFacade = server;
    }

    public void run() {
        active = true;
        System.out.println("♕ Welcome to 240 chess. Type Help to get started. ♕");

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (active) {
            printPrompt();
            String line = scanner.nextLine();

            try {
                result = eval(line);
                System.out.println(result);
            } catch (ResponseException ex) {

            }
        }
    }

    private void printPrompt() {
        System.out.print("[LOGGED_OUT] >>> ");
    }

    public String eval(String input) {
        try {
            String[] tokens = input.split(" ");
            String cmd = (tokens.length > 0) ? tokens[0] : "help";
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "register" -> register(params);
                case "quit" -> "quit";
                default -> help();
            };
        } catch (ResponseException ex) {
            return ex.getMessage();
        }
    }

    public String register (String... params) throws ResponseException {
        if (params.length == 3) {
            String username = params[0];
            String password = params[1];
            String email = params[2];
            RegisterResult registerResult = serverFacade.register(new RegisterRequest(username, password, email));
            String authToken = registerResult.authToken();
            //LoginResult loginResult = serverFacade.login(new LoginRequest(username, password));
        }
        throw new ResponseException("Expected: register <USERNAME> <PASSWORD> <EMAIL>");
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
