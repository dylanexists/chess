package client;

import request.CreateGameRequest;
import result.CreateGameResult;
import server.ResponseException;
import server.ServerFacade;

import java.util.Arrays;
import java.util.Scanner;

public class PostLoginClient {
    private final ServerFacade serverFacade;
    private volatile String authToken;

    public PostLoginClient(ServerFacade serverFac) {
        serverFacade = serverFac;
    }

    public PostLoginResult run(String authToken) {
        this.authToken = authToken;
        Scanner scanner = new Scanner(System.in);
        PostLoginResult result;
        while (true) {
            printPrompt();
            String line = scanner.nextLine();

            try {
                result = eval(line);
                System.out.println(result.cmdResult());
                if (result.nextState() != ClientRepl.ClientState.POST_LOGIN) {return result;}
            } catch (ResponseException ex) {
                throw new ResponseException("- PostLogin command " + line + " failed - " + ex.getMessage(), ex);
            }
        }
    }

    private void printPrompt() {
        System.out.print("[LOGGED_IN] >>> ");
    }

    public PostLoginResult eval(String input) {
        String[] tokens = input.split(" ");
        String cmd = (tokens.length > 0) ? tokens[0] : "help";
        String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
        return switch (cmd) {
            case "create" -> create(params);
            case "quit" -> new PostLoginResult("", ClientRepl.ClientState.EXIT, null);
            default -> new PostLoginResult(help(), ClientRepl.ClientState.POST_LOGIN, null);
        };
    }

    public PostLoginResult create(String... params) throws ResponseException {
        if (params.length == 1) {
            String gameName = params[0];
            CreateGameResult createGameResult = serverFacade.createGame(new CreateGameRequest(authToken, gameName));
            return new PostLoginResult("Game '" + gameName + "' created! Use 'list' command to view its ID",
                    ClientRepl.ClientState.POST_LOGIN, null);
        }
        throw new ResponseException("Expected: create <NAME>");
    }

    public String help() {
        return """
                create <NAME> - a game
                list - games
                join <ID> [WHITE|BLACK] - a game
                observe <ID> - a game
                logout - when you are done
                quit - playing chess
                help - with possible commands
                """;
    }
}
