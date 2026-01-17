package PieceCalculators;

import chess.*;

import java.util.List;

public class RookMoveCalculator implements PieceMoveCalculator {

    @Override
    public List<ChessMove> calculateMoves(ChessBoard board, ChessPosition pos){
        int [] [] directions = { //possible directions of piece, {row, col}
                {0, 1},
                {0, -1},
                {1, 0},
                {-1, 0}
        };

        ContinualMovingPieceCalc rookCalcMoves = new ContinualMovingPieceCalc(directions);
        List<ChessMove> moves = rookCalcMoves.calcMovePaths(board, pos);

        return moves;
    }
}