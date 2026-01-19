package PieceCalculators;

import chess.*;

import java.util.ArrayList;
import java.util.List;

public class PawnMoveCalculator implements PieceMoveCalculator {

    @Override
    public List<ChessMove> calculateMoves(ChessBoard board, ChessPosition pos){
        int [] [] attack_directions = { //possible attack directions for pawn
                {1, 1},
                {1, -1}
        };

        List<ChessMove> moves = new ArrayList<>();
        int row = pos.getRow();
        int col = pos.getColumn();
        ChessPiece pawn = board.getPiece(pos);
        ChessPiece pathed_square_content = null;
        ChessPosition test_pos = null;
        ChessGame.TeamColor color = pawn.getTeamColor();

        int move_up_one_search;
        if (color == ChessGame.TeamColor.WHITE) {
            move_up_one_search = row + 1; //white pieces move up rows
        } else {move_up_one_search = row - 1;} //black pieces move down rows
        if (move_up_one_search >= 1 && move_up_one_search <= 8){
            test_pos = new ChessPosition(move_up_one_search, col);
            pathed_square_content = board.getPiece(test_pos); //get the content of the pathed square
            if (pathed_square_content == null){ //if square is empty, add possible move
                moves.add(new ChessMove(pos, test_pos, null));
                if(color == ChessGame.TeamColor.WHITE && row == 2 || color == ChessGame.TeamColor.BLACK && row == 7){ //if on starting square
                        ChessMove initialMoveTwoSquares = moveTwoSquares(board, pos, row, col, color);
                        if (initialMoveTwoSquares != null){moves.add(initialMoveTwoSquares);}
                    }
                }
            }

        for (int i = 0; i < attack_directions.length; i++){ //for each possible attack direction
            int row_search;
            if (pawn.getTeamColor() == ChessGame.TeamColor.WHITE) {
                 row_search = row + attack_directions[i][0]; //white pieces move up rows
            } else {row_search = row + (-1 * attack_directions[i][0]);} //black pieces move down rows
            int col_search = col + attack_directions[i][1];
            if (row_search >= 1 && row_search <= 8 && col_search >= 1 && col_search <= 8){
                test_pos = new ChessPosition(row_search, col_search);
                pathed_square_content = board.getPiece(test_pos); //get the content of the pathed square
                if (pathed_square_content!= null && pathed_square_content.getTeamColor() != pawn.getTeamColor()){ //if square is enemy, add possible move
                    moves.add(new ChessMove(pos, test_pos, null));
                }
            }
        }

        return moves;
    }

    public ChessMove moveTwoSquares(ChessBoard board, ChessPosition pos, int row, int col, ChessGame.TeamColor color){
        int two_move_search;
        ChessPiece pathed_square_content = null;
        ChessPosition test_pos = null;
         if (color == ChessGame.TeamColor.WHITE) {
             two_move_search = row + 2;
         } else {two_move_search = row - 2;}
        test_pos = new ChessPosition(two_move_search, col);
        pathed_square_content = board.getPiece(test_pos); //get the content of the pathed square
        if (pathed_square_content == null){ //if square is empty, return possible move
            return new ChessMove(pos, test_pos, null);
        } else{return null;} //return null if square is occupied
    }
}