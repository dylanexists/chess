package PieceCalculators;

import chess.*;

import java.util.ArrayList;
import java.util.List;

public class KnightMoveCalculator implements PieceMoveCalculator {

    @Override
    public List<ChessMove> calculateMoves(ChessBoard board, ChessPosition pos){
        int [] [] directions = { //possible directions of piece, {row, col}
                {2, 1},
                {1, 2},
                {-1, 2},
                {-2, 1},
                {-2, -1},
                {-1, -2},
                {1, -2},
                {2, -1},
        };

        List<ChessMove> moves = new ArrayList<>();
        int row = pos.getRow();
        int col = pos.getColumn();
        ChessPiece knight = board.getPiece(pos);
        ChessPiece pathed_square_content = null;
        ChessPosition test_pos = null;

        for (int i = 0; i < directions.length; i++){ //for each possible piece direction
            int row_search = row + directions[i][0];
            int col_search = col + directions[i][1];
            if (row_search >= 1 && row_search <= 8 && col_search >= 1 && col_search <= 8){ //while searching=true and possible move is on board
                test_pos = new ChessPosition(row_search, col_search);
                pathed_square_content = board.getPiece(test_pos); //get the content of the pathed square
                if (pathed_square_content == null || pathed_square_content.getTeamColor() != knight.getTeamColor()){ //if square is empty, add move and continue searching
                    moves.add(new ChessMove(pos, test_pos, null));
                }
            }
        }

        return moves;
    }
}