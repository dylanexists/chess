package piececalc;

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
        ChessPiece pathedSquareContent = null;
        ChessPosition testPos = null;

        for (int i = 0; i < directions.length; i++){ //for each possible piece direction
            int rowSearch = row + directions[i][0];
            int colSearch = col + directions[i][1];
            if (rowSearch >= 1 && rowSearch <= 8 && colSearch >= 1 && colSearch <= 8){ //if possible move is on board
                testPos = new ChessPosition(rowSearch, colSearch);
                pathedSquareContent = board.getPiece(testPos); //get the content of the pathed square
                if (pathedSquareContent == null || pathedSquareContent.getTeamColor() != king.getTeamColor()){
                    moves.add(new ChessMove(pos, testPos, null)); //if square is empty or enemy piece, add move and finish
                }
            }
        }

        return moves;
    }
}