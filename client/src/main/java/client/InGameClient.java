package client;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import client.facade.ServerFacade;
import facade.ConsoleTextHandler;
import facade.ResponseException;
import ui.DrawnChessBoard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Scanner;

import static ui.EscapeSequences.*;

public class InGameClient {
    private final ConsoleTextHandler consoleTextHandler = new ConsoleTextHandler();
    private final ServerFacade serverFacade;
    private volatile String authToken;
    private volatile Integer gameID;
    private volatile ChessGame.TeamColor playerColor;

    public InGameClient(ServerFacade serverFac) throws ResponseException {
        serverFacade = serverFac;
    }

    public InGameResult run(String authToken, Integer gameID, ChessGame.TeamColor playerColor) {
        this.authToken = authToken;
        this.gameID = gameID;
        this.playerColor = playerColor;
        Scanner scanner = new Scanner(System.in);
        InGameResult result;
        while (true) {
            printPrompt();
            String line = scanner.nextLine();
            try {
                result = eval(line, scanner);
                System.out.println(result.cmdResult());
                if (result.nextState() != ClientRepl.ClientState.IN_GAME) {return result;}
            } catch (ResponseException ex) {
                throw new ResponseException("- InGame command " + line + " failed - " + ex.getMessage(), ex);
            }
        }
    }

    public void printPrompt() {System.out.print("[CHESS GAME] >>> ");}

    public InGameResult eval(String input, Scanner scanner) {
        String[] tokens = input.split(" ");
        String cmd = (tokens.length > 0) ? tokens[0] : "help";
        String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
        return switch (cmd) {
            case "redraw" -> redraw();
            case "highlight" -> highlight(params);
            case "move" -> move(params);
            case "resign" -> resign(scanner);
            case "leave" -> leave();
            case "quit" -> new InGameResult("", ClientRepl.ClientState.EXIT);
            default -> new InGameResult(help(), ClientRepl.ClientState.IN_GAME);
        };
    }

    public InGameResult redraw() {
        serverFacade.wsRedraw(authToken, gameID);
        return new InGameResult("", ClientRepl.ClientState.IN_GAME);
    }

    public InGameResult highlight(String ... params) {
        if (!(params.length == 1)) {
            return highlightError();
        }
        try {
            ChessPosition highlightPos = consoleTextHandler.textToChessPosition(params[0]);
            serverFacade.wsHighlight(authToken, gameID, highlightPos);
            return new InGameResult("", ClientRepl.ClientState.IN_GAME);
        } catch (ResponseException ex) {
            return highlightError();
        }
    }

    private InGameResult highlightError(){
        String cmdResult = """
                Highlight Error - Expected: highlight <PIECE SQUARE>
                <PIECE SQUARE> must be a letter followed by a number. Ex: e2 e4, h8 h3, etc.
                <PIECE SQUARE> must have a piece on top of it.""";
        return new InGameResult(cmdResult, ClientRepl.ClientState.IN_GAME);
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
                Make Move Error - Expected: move <PIECE SQUARE> <MOVE-TO SQUARE> <OPTIONAL: PAWN PROMOTES-TO PIECE>
                <PIECE SQUARE> and <MOVE-TO SQUARE> must be a letter followed by a number. Ex: e2 e4, h8 h3, etc.
                <PIECE SQUARE> must be the square of one of your pieces.
                <MOVE-TO SQUARE> must be a valid square that the piece can move to. Type 'help' to learn to use 'highlight'.
                <Pawn Promotes-To Piece> is optional; It only applies to pawns that reach the other team's back rank.""";
        return new InGameResult(cmdResult, ClientRepl.ClientState.IN_GAME);
    }

    public InGameResult resign(Scanner scanner) {
        if (confirmationLoop("resign", scanner)) {
            serverFacade.wsResign(authToken, gameID);
        }
        return new InGameResult("", ClientRepl.ClientState.IN_GAME);
    }

    private boolean confirmationLoop(String action, Scanner scanner) {
        while (true) {
            System.out.print("Are you sure you wish to " + action + "? [Y/N] >>> ");
            String line = scanner.nextLine().trim().toLowerCase();
            switch (line) {
                case "y":
                case "yes": return true;
                case "n":
                case "no": return false;
                default: System.out.println("Please enter 'Y' or 'N'.");
            }
        }
    }

    public InGameResult leave() {
        serverFacade.wsLeaveGame(authToken, gameID);
        return new InGameResult("", ClientRepl.ClientState.POST_LOGIN);
    }

    public InGameResult connect() {
        serverFacade.wsConnectGame(authToken, gameID);
        return new InGameResult("", ClientRepl.ClientState.IN_GAME);
    }

    public void loadGame(ChessGame game, ChessPosition highlightPosition) {
        Collection<ChessPosition> highlights = null;
        if (highlightPosition != null){highlights = highlightMoves(game, highlightPosition);}
        new DrawnChessBoard(game).printBoard(playerColor, highlightPosition, highlights);
        printPrompt();
    }

    private Collection<ChessPosition> highlightMoves(ChessGame game, ChessPosition highlightPos) {
        Collection<ChessMove> validMoves = game.validMoves(highlightPos);
        Collection<ChessPosition> endPositions = new ArrayList<>();
        for (ChessMove move : validMoves) {
            endPositions.add(move.getEndPosition());
        }
        return endPositions;
    }

    public String help() {
        return """
                redraw - redraws the chess board
                highlight <PIECE SQUARE> - highlights the legal moves of the chosen piece
                move <PIECE SQUARE> <MOVE-TO SQUARE> <OPTIONAL: PAWN PROMOTES-TO PIECE> - moves a piece
                resign - forfeits the game and gives the opponent the win
                leave - goes back to pre-game lobby
                quit - playing chess
                help - with possible commands
                """;
    }

}
