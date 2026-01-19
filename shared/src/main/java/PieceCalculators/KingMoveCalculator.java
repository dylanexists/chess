package PieceCalculators;

import chess.*;

import java.util.ArrayList;
import java.util.List;

public class KingMoveCalculator implements PieceMoveCalculator {

    @Override
    public List<ChessMove> calculateMoves(ChessBoard board, ChessPosition pos){
        int [] [] directions = { //possible directions of piece, {row, col}
                {1, 1},
                {0, 1},
                {-1, 1},
                {-1, 0},
                {-1, -1},
                {0, -1},
                {1, -1},
                {1, 0},
        };

        List<ChessMove> moves = new ArrayList<>();
        int row = pos.getRow();
        int col = pos.getColumn();
        ChessPiece king = board.getPiece(pos);
        ChessPiece pathed_square_content = null;
        ChessPosition test_pos = null;

        for (int i = 0; i < directions.length; i++){ //for each possible piece direction
            int row_search = row + directions[i][0];
            int col_search = col + directions[i][1];
            if (row_search >= 1 && row_search <= 8 && col_search >= 1 && col_search <= 8){ //if possible move is on board
                test_pos = new ChessPosition(row_search, col_search);
                pathed_square_content = board.getPiece(test_pos); //get the content of the pathed square
                if (pathed_square_content == null || pathed_square_content.getTeamColor() != king.getTeamColor()){ //if square is empty or enemy piece, add move and finish
                    moves.add(new ChessMove(pos, test_pos, null));
                }
            }
        }

        return moves;
    }
}