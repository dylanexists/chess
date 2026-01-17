package PieceCalculators;

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
        ChessPiece pathed_square_content = null;
        ChessPosition test_pos = null;

        for (int i = 0; i < directions.length; i++){ //for each possible piece direction
            boolean search_cond = true;
            int row_search = row + directions[i][0];
            int col_search = col + directions[i][1];
            while (search_cond && row_search >= 1 && row_search <= 8 && col_search >= 1 && col_search <= 8){ //while searching=true and possible move is on board
                test_pos = new ChessPosition(row_search, col_search);
                pathed_square_content = board.getPiece(test_pos); //get the content of the pathed square
                if (pathed_square_content == null){ //if square is empty, add move and continue searching
                    moves.add(new ChessMove(pos, test_pos, null));
                    row_search += directions[i][0];
                    col_search += directions[i][1];
                } else if (pathed_square_content.getTeamColor() != piece.getTeamColor()){ //elif square is an enemy, add move and stop searching
                    moves.add(new ChessMove(pos, test_pos, null));
                    search_cond = false;
                } else {search_cond = false;} //else, stop searching this direction
            }
        }

        return moves;
    }
}
