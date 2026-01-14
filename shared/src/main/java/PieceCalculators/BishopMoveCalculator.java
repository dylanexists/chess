package PieceCalculators;

import chess.*;

import java.util.ArrayList;
import java.util.List;

public class BishopMoveCalculator implements PieceMoveCalculator {

    @Override
    public List<ChessMove> calculateMoves(ChessBoard board, ChessPosition pos){
        List<ChessMove> moves = new ArrayList<>();
        int row = pos.getRow();
        int col = pos.getColumn();
        ChessPiece bishop = board.getPiece(pos);
        ChessPiece pathed_square_content = null;
        int row_search_up = row + 1;
        boolean search_up_cond = true;
        int row_search_down = row - 1;
        boolean search_down_cond = true;
        ChessPosition test_pos = null;

        for (int c = col + 1; c <= 8; c++){
            if (row_search_up > 8){
                search_up_cond = false;
            } else {row_search_up = row_search_up + 1;}
            if (row_search_down < 1){
                search_down_cond = false;
            } else {row_search_down = row_search_down - 1;}

            test_pos = new ChessPosition(row_search_up - 1, c);

            if (search_up_cond){
                pathed_square_content = board.getPiece(test_pos);
            if (search_up_cond && (pathed_square_content == null || pathed_square_content.getTeamColor() != bishop.getTeamColor())){
                moves.add(new ChessMove(pos, test_pos, null));
            } else {search_up_cond = false;}}

            test_pos = new ChessPosition(row_search_down + 1, c);

            if (search_down_cond){
                pathed_square_content = board.getPiece(test_pos);
            if (search_down_cond && (pathed_square_content == null || pathed_square_content.getTeamColor() != bishop.getTeamColor())){
                moves.add(new ChessMove(pos, test_pos, null));
            } else {search_down_cond = false;}}
        }

        pathed_square_content = null;
        row_search_up = row + 1;
        search_up_cond = true;
        row_search_down = row - 1;
        search_down_cond = true;
        test_pos = null;

        for (int c = col - 1; c >= 1; c--){
            if (row_search_up > 8){
                search_up_cond = false;
            } else {row_search_up = row_search_up + 1;}
            if (row_search_down < 1){
                search_down_cond = false;
            } else {row_search_down = row_search_down - 1;}

            test_pos = new ChessPosition(row_search_up - 1, c);
            if (search_up_cond){
                pathed_square_content = board.getPiece(test_pos);
            if (search_up_cond && (pathed_square_content == null || pathed_square_content.getTeamColor() != bishop.getTeamColor())){
                moves.add(new ChessMove(pos, test_pos, null));
            } else {search_up_cond = false;}}

            test_pos = new ChessPosition(row_search_down + 1, c);
            if (search_down_cond){
                pathed_square_content = board.getPiece(test_pos);
            if (search_down_cond && (pathed_square_content == null || pathed_square_content.getTeamColor() != bishop.getTeamColor())){
                moves.add(new ChessMove(pos, test_pos, null));
            } else {search_down_cond = false;}}
        }
        return moves;
    }
}
