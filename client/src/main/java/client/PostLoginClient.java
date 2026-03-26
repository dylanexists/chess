package client;

import model.GameData;
import request.CreateGameRequest;
import request.ListGamesRequest;
import result.CreateGameResult;
import result.ListGamesResult;
import server.ResponseException;
import server.ServerFacade;

import java.util.*;

public class PostLoginClient {
    private final ServerFacade serverFacade;
    private volatile String authToken;
    private final HashMap<Integer, GameData> gamesUserInteractable = new HashMap<>();


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
            case "list" -> list();
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

    public PostLoginResult list() throws ResponseException {
        ListGamesResult listGamesResult = serverFacade.listGames(new ListGamesRequest(authToken));
        List<GameData> games = listGamesResult.games();
        createGamesUserInteractableMap(games);
        StringBuilder sb = new StringBuilder();
        gamesUserInteractable.forEach((key, game) -> {
            sb.append(key)
                    .append(".  Game Name: ")
                    .append(game.gameName())
                    .append("    White: ")
                    .append(game.whiteUsername() != null ? game.whiteUsername() : "empty")
                    .append("    Black: ")
                    .append(game.blackUsername() != null ? game.blackUsername() : "empty")
                    .append("\n");
        });
        String cmdResult = sb.toString();
        return new PostLoginResult(cmdResult, ClientRepl.ClientState.POST_LOGIN, null);
    }

    private void createGamesUserInteractableMap(List<GameData> games) {
        gamesUserInteractable.clear();
        for (int i = 0; i < games.size(); i++) {
            gamesUserInteractable.put(i + 1, games.get(i));
        }
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
