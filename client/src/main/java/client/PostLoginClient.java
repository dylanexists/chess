package client;

import model.GameData;
import request.CreateGameRequest;
import request.JoinGameRequest;
import request.ListGamesRequest;
import result.CreateGameResult;
import result.JoinGameResult;
import result.ListGamesResult;
import server.ResponseException;
import server.ServerFacade;

import java.util.*;

public class PostLoginClient {
    private final ServerFacade serverFacade;
    private volatile String authToken;
    private final HashMap<Integer, Integer> gamesUserInteractable = new HashMap<>();


    public PostLoginClient(ServerFacade serverFac) {
        serverFacade = serverFac;
    }

    public PostLoginResult run(String authToken) {
        this.authToken = authToken;
        list(); // creates first iteration of gamesUserInteractable with the pre-existing games in the db
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
            case "join" -> join(params);
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
        HashMap<Integer, GameData> displayIDToGameMap = createGamesUserInteractableMap(games);
        StringBuilder sb = new StringBuilder();
        displayIDToGameMap.forEach((id, game) -> {
            sb.append(id)
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

    public PostLoginResult join(String... params) throws ResponseException {
        if (params.length == 2) {
            String gameIDString = params[0];
            String playerColor = params[1];
            if (!playerColor.equals("WHITE") && !playerColor.equals("BLACK")) {
                throw new ResponseException("Error: 'WHITE' and 'BLACK' are the only valid team colors");
            }

            int gameID;
            try {
                gameID = Integer.parseInt(gameIDString);
            } catch (NumberFormatException ex) {
                throw new ResponseException("Error: ID should be a number", ex);
            }

            int trueGameID = gamesUserInteractable.get(gameID);
            JoinGameResult joinGameResult = serverFacade.joinGame(new JoinGameRequest(authToken, playerColor, trueGameID));
            return new PostLoginResult("Game " + gameIDString + " successfully joined!",
                        ClientRepl.ClientState.IN_GAME, trueGameID);
        }
        throw new ResponseException("Expected: join <ID> [WHITE|BLACK]");
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

    private HashMap<Integer, GameData> createGamesUserInteractableMap(List<GameData> games) {
        gamesUserInteractable.clear();
        HashMap<Integer, GameData> returnMap = new HashMap<>();
        for (int i = 0; i < games.size(); i++) {
            gamesUserInteractable.put(i + 1, games.get(i).gameID());
            returnMap.put(i + 1, games.get(i));
        }
        return returnMap;
    }

}
