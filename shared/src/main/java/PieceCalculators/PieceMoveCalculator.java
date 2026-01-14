package PieceCalculators;

import chess.ChessMove;
import chess.ChessBoard;
import chess.ChessPosition;
import java.util.List;

public interface PieceMoveCalculator{
    List<ChessMove> calculateMoves(
            ChessBoard board,
            ChessPosition pos
    );
}
