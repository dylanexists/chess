package piececalc;

import chess.*;

import java.util.ArrayList;
import java.util.List;

public class PawnMoveCalculator implements PieceMoveCalculator {

    @Override
    public List<ChessMove> calculateMoves(ChessBoard board, ChessPosition pos){
        int [] [] attackDirections = { //possible attack directions for pawn
                {1, 1},
                {1, -1}
        };

        List<ChessMove> moves = new ArrayList<>();
        int row = pos.getRow();
        int col = pos.getColumn();
        ChessPiece pawn = board.getPiece(pos);
        ChessPiece pathedSquareContent = null;
        ChessPosition testPos = null;
        ChessGame.TeamColor color = pawn.getTeamColor();

        int oneStepForwardSearch;
        if (color == ChessGame.TeamColor.WHITE) {
            oneStepForwardSearch = row + 1; //white pieces move up rows
        } else {oneStepForwardSearch = row - 1;} //black pieces move down rows
        if (oneStepForwardSearch >= 1 && oneStepForwardSearch <= 8){
            testPos = new ChessPosition(oneStepForwardSearch, col);
            pathedSquareContent = board.getPiece(testPos); //get the content of the pathed square
            if (pathedSquareContent == null){ //if square is empty, add possible move
                promotionLogic(moves, oneStepForwardSearch, color, pos, testPos);
                if(color == ChessGame.TeamColor.WHITE && row == 2 || color == ChessGame.TeamColor.BLACK && row == 7){ //if on starting square
                        ChessMove initialMoveTwoSquares = moveTwoSquares(board, pos, row, col, color);
                        if (initialMoveTwoSquares != null){moves.add(initialMoveTwoSquares);}
                    }
                }
            }

        for (int i = 0; i < attackDirections.length; i++){ //for each possible attack direction
            int rowSearch;
            if (pawn.getTeamColor() == ChessGame.TeamColor.WHITE) {
                rowSearch = row + attackDirections[i][0]; //white pieces move up rows
            } else {rowSearch = row + (-1 * attackDirections[i][0]);} //black pieces move down rows
            int col_search = col + attackDirections[i][1];
            if (rowSearch >= 1 && rowSearch <= 8 && col_search >= 1 && col_search <= 8){
                testPos = new ChessPosition(rowSearch, col_search);
                pathedSquareContent = board.getPiece(testPos); //get the content of the pathed square
                if (pathedSquareContent!= null && pathedSquareContent.getTeamColor() != pawn.getTeamColor()){
                    promotionLogic(moves, rowSearch, color, pos, testPos);
                }
            }
        }

        return moves;
    }

    public ChessMove moveTwoSquares(ChessBoard board, ChessPosition pos, int row, int col, ChessGame.TeamColor color){
        int twoMoveSearch;
        ChessPiece pathedSquareContent = null;
        ChessPosition test_pos = null;
         if (color == ChessGame.TeamColor.WHITE) {
             twoMoveSearch = row + 2;
         } else {twoMoveSearch = row - 2;}
        test_pos = new ChessPosition(twoMoveSearch, col);
        pathedSquareContent = board.getPiece(test_pos); //get the content of the pathed square
        if (pathedSquareContent == null){ //if square is empty, return possible move
            return new ChessMove(pos, test_pos, null);
        } else{return null;} //return null if square is occupied
    }

    public boolean isPromotionSquare(int newRow, ChessGame.TeamColor color){
        return color == ChessGame.TeamColor.WHITE && newRow == 8 || color == ChessGame.TeamColor.BLACK && newRow == 1;
    } //return true if pawn has moved to promotion square

    public void promotionLogic(List<ChessMove> moves, int row_search, ChessGame.TeamColor color, ChessPosition start_pos, ChessPosition end_pos){
        if (isPromotionSquare(row_search, color)) {
            moves.add(new ChessMove(start_pos, end_pos, ChessPiece.PieceType.ROOK));
            moves.add(new ChessMove(start_pos, end_pos, ChessPiece.PieceType.KNIGHT));
            moves.add(new ChessMove(start_pos, end_pos, ChessPiece.PieceType.BISHOP));
            moves.add(new ChessMove(start_pos, end_pos, ChessPiece.PieceType.QUEEN));
        } else {
            moves.add(new ChessMove(start_pos, end_pos, null));
        }
    }
}

