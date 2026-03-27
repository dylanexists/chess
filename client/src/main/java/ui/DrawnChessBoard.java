package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static ui.EscapeSequences.*;
import static ui.EscapeSequences.BLACK_KING;
import static ui.EscapeSequences.BLACK_ROOK;

public class DrawnChessBoard {

    private static ChessGame chessGame;
    private static String currentBGColor; //top left square is always light
    private volatile boolean REVERSED;

    public static final String myDarkGreen = SET_BG_COLOR_DARK_GREEN;
    public static final String myLightGreen = SET_BG_COLOR_GREEN;
    private static final int BOARD_SIZE_IN_SQUARES = 8;
    public static final String TINY = "\u2004"; // without this, things don't line up
    public static final String TEENY = "\u2006"; // this too



    public DrawnChessBoard(ChessGame game) {
        chessGame = game;
    }

    public void printBoard(ChessGame.TeamColor color) {
        var out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        out.print(ERASE_SCREEN);
        currentBGColor = myLightGreen; //top left square always light color
        REVERSED = color == ChessGame.TeamColor.BLACK; //reverse is true if it's black player's view

        drawHeader(out);
        drawChess(out);
        drawHeader(out);

        out.print(RESET_TEXT_COLOR + RESET_BG_COLOR);
    }

    private void drawChess(PrintStream out) {
        int topNumberRightLetter = REVERSED ? 1 : 8; //the top left number (ex. 8) is the bottom right letter (ex. h)
        int bottomNumberLeftLetter = REVERSED ? 8 : 1; //Ex. top left will be 1, h (8) or 8, a (1)
        int step = REVERSED ? 1 : -1;
        for (int r = topNumberRightLetter; r != (bottomNumberLeftLetter + step); r += step) { //iterates i to the correct number, regardless of board orientation
            out.print(SET_BG_COLOR_BLUE);
            drawBoarderSquare(out, " " + r + " ");
            for (int c = bottomNumberLeftLetter; c != topNumberRightLetter - step; c -= step){
                ChessBoard board = chessGame.getBoard();
                ChessPiece piece = board.getPiece(new ChessPosition(r, c));
                if (piece == null) {
                    drawSquare(out, EMPTY, null);
                } else {
                    ChessGame.TeamColor pieceColor = piece.getTeamColor();
                    ChessPiece.PieceType pieceType = piece.getPieceType();
                    String pieceString = pieceStringConvertor(pieceType);
                    drawSquare(out, pieceString, pieceColor);
                }
            }
            out.print(SET_BG_COLOR_BLUE);
            drawBoarderSquare(out, " " + r + " " + TEENY);
            drawNewline(out);
            alternateSquareColor();
        }
    }

    private String pieceStringConvertor(ChessPiece.PieceType type) {
            return switch (type) {
                case ChessPiece.PieceType.PAWN -> BLACK_PAWN;
                case ChessPiece.PieceType.ROOK -> BLACK_ROOK;
                case ChessPiece.PieceType.KNIGHT -> BLACK_KNIGHT;
                case ChessPiece.PieceType.BISHOP -> BLACK_BISHOP;
                case ChessPiece.PieceType.QUEEN -> BLACK_QUEEN;
                case ChessPiece.PieceType.KING -> BLACK_KING;
            };
    }

    private void drawBoarderSquare(PrintStream out, String number) {
        out.print(SET_TEXT_COLOR_BLACK);
        out.print(number);
    }

    private void drawSquare(PrintStream out, String piece, ChessGame.TeamColor color) {
        out.print(currentBGColor);
        out.print(color == ChessGame.TeamColor.WHITE ? SET_TEXT_COLOR_WHITE : SET_TEXT_COLOR_BLACK);
        out.print(piece);
        alternateSquareColor();
    }

    private void alternateSquareColor() {
        currentBGColor = currentBGColor.equals(myLightGreen) ? myDarkGreen : myLightGreen;
    }

    public void drawHeader(PrintStream out) {
        setBoarderColor(out);

        out.print(EMPTY); //corner

        String[] headers = {"a", "b", "c", "d", "e", "f", "g", "h"};
        if (!REVERSED) {
            for (int boardCol = 0; boardCol < BOARD_SIZE_IN_SQUARES; boardCol++) {
                drawBigHeader(out, headers[boardCol]);
            }
        } else {
            for (int boardCol = BOARD_SIZE_IN_SQUARES; boardCol > 0; boardCol--) {
                drawBigHeader(out, headers[boardCol - 1]);
            }
        }
        out.print(EMPTY); //corner
        drawNewline(out);


    }

    private void drawNewline(PrintStream out) {
        out.print(RESET_BG_COLOR);
        out.println();
    }

    private void setBoarderColor(PrintStream out) {
        out.print(SET_TEXT_COLOR_BLACK);
        out.print(SET_BG_COLOR_BLUE);
    }

    private void drawBigHeader(PrintStream out, String headerText) {
        out.print(TINY + TINY + headerText + TINY + TINY + TINY); //spacing to adjust for em-space weirdness
    }

}
