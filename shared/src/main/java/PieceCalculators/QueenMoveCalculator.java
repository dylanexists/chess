package PieceCalculators;

import chess.*;

import java.util.List;

public class QueenMoveCalculator implements PieceMoveCalculator {

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

        ContinualMovingPieceCalc queenCalcMoves = new ContinualMovingPieceCalc(directions);
        List<ChessMove> moves = queenCalcMoves.calcMovePaths(board, pos);

        return moves;
    }
}