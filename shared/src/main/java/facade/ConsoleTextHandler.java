package facade;

import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;

public class ConsoleTextHandler {

    public ChessMove textToChessMove(String startPosText, String endPosText, String piecePromText) throws ResponseException {
        ChessPiece.PieceType pieceProm = null;
        if (piecePromText != null) {
            pieceProm = textToPiece(piecePromText);
            if (pieceProm == ChessPiece.PieceType.KING || pieceProm == ChessPiece.PieceType.PAWN){
                throw new ResponseException(piecePromText + " is not a valid promotion piece.", chessPositionError());
            }
        }
        ChessPosition startPos = textToChessPosition(startPosText);
        ChessPosition endPos = textToChessPosition(endPosText);
        return new ChessMove(startPos, endPos, pieceProm);
    }

    public ChessPosition textToChessPosition(String positionText) throws ResponseException {
        if (positionText.length() != 2) {
            throw chessPositionError();
        }
        char startLetter = positionText.toLowerCase().charAt(0);
        int startNumber = Character.getNumericValue(positionText.charAt(1));
        if (startLetter < 'a' || startLetter > 'h' || startNumber < 1 || startNumber > 8) {
            throw new ResponseException(positionText + " is not a valid chess square.", chessPositionError());
        }
        return new ChessPosition(startNumber, letterToNumber(startLetter));
    }

    public ChessPiece.PieceType textToPiece(String pieceText) {
        return switch (pieceText.toLowerCase()) {
            case "pawn" -> ChessPiece.PieceType.PAWN;
            case "rook" -> ChessPiece.PieceType.ROOK;
            case "knight" -> ChessPiece.PieceType.KNIGHT;
            case "bishop" -> ChessPiece.PieceType.BISHOP;
            case "queen" -> ChessPiece.PieceType.QUEEN;
            case "king" -> ChessPiece.PieceType.KING;
            default -> null;
        };
    }

    public String pieceToText(ChessPiece.PieceType pieceType) {
        return switch (pieceType) {
            case ChessPiece.PieceType.PAWN -> "pawn";
            case ChessPiece.PieceType.ROOK -> "rook";
            case ChessPiece.PieceType.KNIGHT -> "knight";
            case ChessPiece.PieceType.BISHOP -> "bishop";
            case ChessPiece.PieceType.QUEEN -> "queen";
            case ChessPiece.PieceType.KING -> "king";
        };
    }

    public String prettyPrintPosition(ChessPosition position) {
        int letterPos = position.getColumn();
        String letter = COLUMN_LETTERS[letterPos - 1];
        String number = String.valueOf(position.getRow());
        return letter + number;
    }

    private ResponseException chessPositionError() {
        return new ResponseException("Console text does not match chess position syntax.");
    }

    private int letterToNumber(char ch) {//use ASCII addition and subtraction to convert char to int
        return ch - 'a' + 1;
    }

    private static final String [] COLUMN_LETTERS = { //letter array of chess-board columns
            "a",
            "b",
            "c",
            "d",
            "e",
            "f",
            "g",
            "h"
    };
}
