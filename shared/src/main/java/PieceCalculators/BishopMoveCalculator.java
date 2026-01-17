package PieceCalculators;

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

        ContinualMovingPieceCalc rookCalcMoves = new ContinualMovingPieceCalc(directions);
        List<ChessMove> moves = rookCalcMoves.calcMovePaths(board, pos);

        return moves;
    }
}
        /*
        //search right
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

        // search left
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
        */


