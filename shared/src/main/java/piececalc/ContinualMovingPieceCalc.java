package piececalc;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;

import java.util.ArrayList;
import java.util.List;

public class ContinualMovingPieceCalc {

    private final int [] [] directions;

    public ContinualMovingPieceCalc(int [] [] directions){
        this.directions = directions;
    }

    public List<ChessMove> calcMovePaths(ChessBoard board, ChessPosition pos) {
        List<ChessMove> moves = new ArrayList<>();
        int row = pos.getRow();
        int col = pos.getColumn();
        ChessPiece piece = board.getPiece(pos);
        ChessPiece pathedSquareContent = null;
        ChessPosition testPos = null;

        for (int i = 0; i < directions.length; i++){ //for each possible piece direction
            boolean searchCond = true;
            int rowSearch = row + directions[i][0];
            int colSearch = col + directions[i][1];
            while (searchCond && rowSearch >= 1 && rowSearch <= 8 && colSearch >= 1 && colSearch <= 8){ //while searching and move is on board
                testPos = new ChessPosition(rowSearch, colSearch);
                pathedSquareContent = board.getPiece(testPos); //get the content of the pathed square
                if (pathedSquareContent == null){ //if square is empty, add move and continue searching
                    moves.add(new ChessMove(pos, testPos, null));
                    rowSearch += directions[i][0];
                    colSearch += directions[i][1];
                } else if (pathedSquareContent.getTeamColor() != piece.getTeamColor()){ //elif square is an enemy, add move and stop searching
                    moves.add(new ChessMove(pos, testPos, null));
                    searchCond = false;
                } else {searchCond = false;} //else, stop searching this direction
            }
        }

        return moves;
    }
}
