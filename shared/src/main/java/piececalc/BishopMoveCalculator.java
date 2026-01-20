package piececalc;

import chess.*;

import java.util.List;

public class BishopMoveCalculator implements PieceMoveCalculator {

    @Override
    public List<ChessMove> calculateMoves(ChessBoard board, ChessPosition pos){
        int [] [] directions = { //possible directions of piece, {row, col}
                {1, 1},
                {-1, 1},
                {1, -1},
                {-1, -1}
        };

        ContinualMovingPieceCalc bishopCalcMoves = new ContinualMovingPieceCalc(directions);
        List<ChessMove> moves = bishopCalcMoves.calcMovePaths(board, pos);

        return moves;
    }
}
