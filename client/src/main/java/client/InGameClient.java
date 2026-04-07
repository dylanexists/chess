package client;

import chess.ChessGame;
import chess.ChessMove;
import client.facade.ServerFacade;
import client.websocket.ServerMessageObserver;
import facade.ConsoleTextHandler;
import facade.ResponseException;
import ui.DrawnChessBoard;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.util.Arrays;
import java.util.Scanner;

import static ui.EscapeSequences.*;

public class InGameClient {
    private final ConsoleTextHandler consoleTextHandler = new ConsoleTextHandler();
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
            case "move" -> move(params);
            case "leave" -> leave();
            case "quit" -> new InGameResult("", ClientRepl.ClientState.EXIT);
            default -> new InGameResult(help(), ClientRepl.ClientState.IN_GAME);
        };
    }

    public InGameResult move(String ... params) {
        String promotionPiece;
        if (params.length == 2) {
            promotionPiece = null;
        } else if (params.length == 3){
            promotionPiece = params[2];
        } else {
            return moveError();
        }
        try {
            //textToChessMove() verifies that params[0 and 1] are valid squares and that promotionPiece is a piece
            ChessMove move = consoleTextHandler.textToChessMove(params[0], params[1], promotionPiece);
            serverFacade.wsMakeMove(authToken, gameID, move);
            return new InGameResult("", ClientRepl.ClientState.IN_GAME);
        } catch (ResponseException ex) {
            return moveError();
        }
    }

    private InGameResult moveError(){
        String cmdResult = """
                Make Move Error - Expected: move <Piece Square> <Move-To Square> <OPTIONAL: Pawn Promotes-To Piece>
                <Piece Square> and <Move-To Square> must be a letter followed by a number. Ex: e2 e4, h8 h3, etc.
                <Piece Square> must be the square of one of your pieces.
                <Move-To Square> must be a valid square that the piece can move to. Type 'help' to learn to use 'highlight'.
                <Pawn Promotes-To Piece> is optional; It only applies to pawns that reach the other team's back rank.""";
        return new InGameResult(cmdResult, ClientRepl.ClientState.IN_GAME);
    }

    public InGameResult leave() {
        serverFacade.wsLeaveGame(authToken, gameID);
        return new InGameResult("", ClientRepl.ClientState.POST_LOGIN);
    }

    public InGameResult connect() {
        serverFacade.wsConnectGame(authToken, gameID);
        return new InGameResult("", ClientRepl.ClientState.POST_LOGIN);
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
