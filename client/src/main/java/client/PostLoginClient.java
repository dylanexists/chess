package client;

import chess.ChessGame;
import client.facade.ServerFacade;
import model.GameData;
import request.CreateGameRequest;
import request.JoinGameRequest;
import request.ListGamesRequest;
import request.LogoutRequest;
import result.CreateGameResult;
import result.JoinGameResult;
import result.ListGamesResult;
import result.LogoutResult;
import facade.ResponseException;

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

    public void printPrompt() {
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
            case "observe" -> observe(params);
            case "logout" -> logout();
            case "quit" -> new PostLoginResult("", ClientRepl.ClientState.EXIT, null, null);
            default -> new PostLoginResult(help(), ClientRepl.ClientState.POST_LOGIN, null, null);
        };
    }

    public PostLoginResult create(String... params) {
        if (params.length == 1) {
            try {
                String gameName = params[0];
                CreateGameResult createGameResult = serverFacade.createGame(new CreateGameRequest(authToken, gameName));
                list(); //update gamesList
                return new PostLoginResult("Game '" + gameName + "' created! Use 'list' command to view its ID",
                        ClientRepl.ClientState.POST_LOGIN, null, null);
            } catch (ResponseException ex) {return new PostLoginResult("DN idk error: create", ClientRepl.ClientState.POST_LOGIN, null, null);}
        }
        return createError();
    }

    private PostLoginResult createError() {
        String cmdResult = """
                Create Game Error - Expected: create <NAME>
                <NAME> should be no more and no less than one word, zero spaces.""";
        return new PostLoginResult(cmdResult, ClientRepl.ClientState.POST_LOGIN, null, null);
    }

    public PostLoginResult list() throws ResponseException {
        try {
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
            return new PostLoginResult(cmdResult, ClientRepl.ClientState.POST_LOGIN, null, null);
        } catch (ResponseException ex) {return new PostLoginResult("DN idk error: list", ClientRepl.ClientState.POST_LOGIN, null, null);}
    }

    public PostLoginResult join(String... params) {
        if (params.length == 2) {
            try {
                String gameIDString = params[0];
                String playerColorString = params[1].toUpperCase();
                if (!playerColorString.equals("WHITE") && !playerColorString.equals("BLACK")) {
                    return new PostLoginResult("Join Game Error: 'WHITE' and 'BLACK' are the only valid team colors",
                            ClientRepl.ClientState.POST_LOGIN, null, null);
                }

                int gameID;
                try {
                    gameID = Integer.parseInt(gameIDString);
                } catch (NumberFormatException ex) {
                    return new PostLoginResult("Join Game Error: ID should be a number", ClientRepl.ClientState.POST_LOGIN, null, null);
                }

                if (!gamesUserInteractable.containsKey(gameID)) {
                    return joinError();
                }
                int trueGameID = gamesUserInteractable.get(gameID);
                JoinGameResult joinGameResult = serverFacade.joinGame(new JoinGameRequest(authToken, playerColorString, trueGameID));
                ChessGame.TeamColor playerColor = playerColorString.equals("WHITE") ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
                serverFacade.wsConnectGame(authToken, trueGameID);
                return new PostLoginResult("Game " + gameIDString + " successfully joined!",
                        ClientRepl.ClientState.IN_GAME, trueGameID, playerColor);
            } catch (ResponseException ex) {
                return joinError();
            }
        }
        return joinError();
    }

    private PostLoginResult joinError() {
        String cmdResult = """
                Join Game Error - Expected: join <ID> [WHITE|BLACK]
                Type 'list' to see all existing games and their IDs. Ensure the ID you type exists.
                Ensure player color is either 'WHITE' or 'BLACK' and that said color isn't already taken.""";
        return new PostLoginResult(cmdResult, ClientRepl.ClientState.POST_LOGIN, null, null);
    }

    public PostLoginResult observe(String... params) {
        if (params.length == 1){
            try {
                String gameIDString = params[0];
                int gameID;
                try {
                    gameID = Integer.parseInt(gameIDString);
                } catch (NumberFormatException ex) {
                    return new PostLoginResult("Observe Game Error: ID should be a number", ClientRepl.ClientState.POST_LOGIN, null, null);
                }

                if (!gamesUserInteractable.containsKey(gameID)) {
                    return observeError();
                }
                int trueGameID = gamesUserInteractable.get(gameID);
                serverFacade.wsConnectGame(authToken, trueGameID);
                return new PostLoginResult("Joining Game " + gameIDString + " to observe!",
                        ClientRepl.ClientState.IN_GAME, trueGameID, null); //null playerColor means spectator

            } catch (ResponseException ex) {
                return observeError();
            }
        }
        return observeError();
    }

    private PostLoginResult observeError() {
        String cmdResult = """
                Observe Game Error - Expected: observe <ID>
                Type 'list' to see all existing games and their IDs. Ensure the ID you type exists.
                <ID> should be no more and no less than one number, zero spaces.""";
        return new PostLoginResult(cmdResult, ClientRepl.ClientState.POST_LOGIN, null, null);
    }

    public PostLoginResult logout() {
        try {
            LogoutResult logoutResult = serverFacade.logout(new LogoutRequest(authToken));
            return new PostLoginResult("Successfully Logged Out",
                            ClientRepl.ClientState.PRE_LOGIN, null, null);
        } catch (ResponseException ex) {return new PostLoginResult("DN idk error: logout", ClientRepl.ClientState.POST_LOGIN, null, null);}
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
