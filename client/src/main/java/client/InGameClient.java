package client;

import chess.ChessGame;
import client.websocket.ServerMessageObserver;
import facade.ResponseException;
import facade.ServerFacade;
import ui.DrawnChessBoard;
import ui.EscapeSequences;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.util.Arrays;
import java.util.Scanner;

import static ui.EscapeSequences.*;

public class InGameClient implements ServerMessageObserver {
    private final ServerFacade serverFacade;
    private volatile String authToken;
    private volatile Integer gameID;
    private volatile ChessGame.TeamColor playerColor;
    private volatile DrawnChessBoard drawnChessBoard;

    public InGameClient(ServerFacade serverFac) throws ResponseException {
        serverFacade = serverFac;
    }

    @Override
    public void notify(ServerMessage message) {
        switch (message.getServerMessageType()) {
            case NOTIFICATION -> displayNotification(((NotificationMessage) message).getMessage());
            case ERROR -> displayError(((ErrorMessage) message).getErrorMessage());
            case LOAD_GAME -> loadGame(((LoadGameMessage) message).getGame());
        }
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

    public void displayNotification(String notification) {
        System.out.println(SET_TEXT_COLOR_RED + "Notif:" + notification);
    }

    public void displayError(String error) {
        System.out.println(SET_TEXT_COLOR_RED + "Error:" + error);
    }

    public void loadGame(ChessGame game) {
        new DrawnChessBoard(game).printBoard(playerColor);
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
