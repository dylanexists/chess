package client;

import chess.ChessGame;
import server.ResponseException;
import server.ServerFacade;
import ui.DrawnChessBoard;

import java.util.Arrays;
import java.util.Scanner;

public class InGameClient {
    private final ServerFacade serverFacade;
    private volatile String authToken;
    private volatile Integer gameID;
    private volatile ChessGame.TeamColor playerColor;
    private volatile DrawnChessBoard drawnChessBoard;

    public InGameClient(ServerFacade serverFac) throws ResponseException {
        serverFacade = serverFac;
    }

    public InGameResult run(String authToken, Integer gameID, ChessGame.TeamColor playerColor) {
        this.authToken = authToken;
        this.gameID = gameID;
        this.playerColor = playerColor;
        Scanner scanner = new Scanner(System.in);
        InGameResult result;
        drawnChessBoard = new DrawnChessBoard(new ChessGame());
        while (true) {
            drawnChessBoard.printBoard(playerColor); //playerColor = null is spectator, printBoard() auto assigns view to white
            printPrompt();
            String line = scanner.nextLine();
            try {
                result = eval(line);
                System.out.println(result.cmdResult());
                if (result.nextState() != ClientRepl.ClientState.IN_GAME) {return result;}
            } catch (ResponseException ex) {
                throw new ResponseException("- InGame command " + line + " failed - " + ex.getMessage(), ex);
            }
        }
    }

    private void printPrompt() {System.out.print("[CHESS GAME] >>> ");}

    public InGameResult eval(String input) {
        String[] tokens = input.split(" ");
        String cmd = (tokens.length > 0) ? tokens[0] : "help";
        String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
        return switch (cmd) {
            case "leave" -> new InGameResult("", ClientRepl.ClientState.POST_LOGIN);
            case "quit" -> new InGameResult("", ClientRepl.ClientState.EXIT);
            default -> new InGameResult(help(), ClientRepl.ClientState.IN_GAME);
        };
    }

    public String help() {
        return """
                **Placeholder Help Message**
                Gameplay has not yet been implemented
                Type 'leave' to leave Game REPL.
                Type 'quit' to quit the program.
                Leaving or quitting does not open up a player slot for the game,
                this will be fixed in Phase 6.
                """;
    }

}
