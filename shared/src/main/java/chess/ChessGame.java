package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private ChessBoard board;
    private TeamColor turn;
    public boolean whiteKingMoved = false;
    public boolean whiteCol1RookMoved = false;
    public boolean whiteCol8RookMoved = false;
    public boolean blackKingMoved = false;
    public boolean blackCol1RookMoved = false;
    public boolean blackCol8RookMoved = false;

    public ChessGame() {
        board = new ChessBoard();
        board.resetBoard();
        turn = TeamColor.WHITE;
    }

    @Override
    public String toString() {
        return "ChessGame{" +
                "board=" + board +
                ", turn=" + turn +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return Objects.equals(board, chessGame.board) && turn == chessGame.turn;
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, turn);
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return turn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        turn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece currentPiece = board.getPiece(startPosition);
        if (currentPiece == null){return null;}
        TeamColor color = currentPiece.getTeamColor();
        Collection<ChessMove> pieceMoves = currentPiece.pieceMoves(board, startPosition);
        List<ChessMove> validMoves = new ArrayList<>();
        for (ChessMove move : pieceMoves){
            ChessBoard clonedBoard = board.clone();
            movePieceHelper(clonedBoard, move);
            if (! isInCheck(color, clonedBoard)){
                validMoves.add(move);
            }
        }
        //add possible castling moves for king pieces
        if (currentPiece.getPieceType() == ChessPiece.PieceType.KING && !isInCheck(color, board)) {
            Collection<ChessMove> castlingMoves = castlingHelper(color, startPosition);
            if (castlingMoves != null) {validMoves.addAll(castlingMoves);}
        }
        return validMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition startPos = move.getStartPosition();
        ChessPiece movingPiece = board.getPiece(startPos);
        if (movingPiece == null || movingPiece.getTeamColor() != turn) {throw new InvalidMoveException("Move isn't valid:" + move);}
        ChessPiece.PieceType type = movingPiece.getPieceType();
        Collection<ChessMove> validMovesOfPiece = validMoves(startPos);
        if (validMovesOfPiece.contains(move)){
            movePieceHelper(board, move);
            if (type == ChessPiece.PieceType.KING || type == ChessPiece.PieceType.ROOK){
                updateCastlingPieces(movingPiece, startPos, type);
            }
            if (turn == TeamColor.WHITE) {  //switch turns
                turn = TeamColor.BLACK;
            } else {turn = TeamColor.WHITE;}
        } else {
            throw new InvalidMoveException("Move isn't valid:" + move);
        }
    }

    public void movePieceHelper(ChessBoard board, ChessMove move){
        //change our piece's position from current position to move position, and promote if specified
        ChessPosition startPos = move.getStartPosition();
        ChessPosition endPos = move.getEndPosition();
        ChessPiece.PieceType promotionPiece = move.getPromotionPiece();
        ChessPiece piece = board.getPiece(startPos);
        if (promotionPiece != null) {piece.setPieceType(promotionPiece);}
        board.grid[endPos.getRow()-1][endPos.getColumn()-1] =
                board.grid[startPos.getRow()-1][startPos.getColumn()-1];
        board.grid[startPos.getRow()-1][startPos.getColumn()-1] = null;
        moveRookIfKingCastled(piece, startPos, endPos);
    }

    public void moveRookIfKingCastled(ChessPiece piece, ChessPosition startPos, ChessPosition endPos){
        if (piece.getPieceType() == ChessPiece.PieceType.KING){ //only perform if piece is king
            TeamColor color = piece.getTeamColor();
            int row;
            if (color == TeamColor.WHITE) {row = 1;}
            else {row = 8;}
            ChessPosition kingPos = new ChessPosition(row, 5);
            if (startPos.equals(kingPos) && endPos.equals(new ChessPosition(row, 3))){ //if king castled left
                ChessPosition leftRookPos = new ChessPosition(row, 1);
                ChessPosition jumpedLeftRookPos = new ChessPosition(row, 4);
                movePieceHelper(board, new ChessMove(leftRookPos, jumpedLeftRookPos, null)); //then "jump" left rook
            } else if (startPos.equals(kingPos) && endPos.equals(new ChessPosition(row, 7))) { //if king castled right
                ChessPosition rightRookPos = new ChessPosition(row, 8);
                ChessPosition jumpedRightRookPos = new ChessPosition(row, 6);
                movePieceHelper(board, new ChessMove(rightRookPos, jumpedRightRookPos, null)); //then "jump" right rook
            }
        }
    }

    public void updateCastlingPieces (ChessPiece piece, ChessPosition startPos, ChessPiece.PieceType type){
        //logic for castling
        if (piece.getTeamColor() == TeamColor.WHITE){
            if (!whiteKingMoved && type == ChessPiece.PieceType.KING){
                whiteKingMoved = true;
            } else if (!whiteCol1RookMoved && type == ChessPiece.PieceType.ROOK && startPos.equals(new ChessPosition(1, 1))){
                whiteCol1RookMoved = true;
            } else if (!whiteCol8RookMoved && type == ChessPiece.PieceType.ROOK && startPos.equals(new ChessPosition(1, 8))) {
                whiteCol8RookMoved = true;
            }
        } else {
            if (!blackKingMoved && type == ChessPiece.PieceType.KING){
                blackKingMoved = true;
            } else if (!blackCol1RookMoved && type == ChessPiece.PieceType.ROOK && startPos.equals(new ChessPosition(8, 1))){
                blackCol1RookMoved = true;
            } else if (!blackCol8RookMoved && type == ChessPiece.PieceType.ROOK && startPos.equals(new ChessPosition(8, 8))) {
                blackCol8RookMoved = true;
            }
        }
    }

    public Collection<ChessMove> castlingHelper(TeamColor color, ChessPosition kingStartPos){
        Collection<ChessMove> castlingMoves = new ArrayList<>();
        boolean kingMovedChecker;
        boolean col1RookMovedChecker;
        boolean col8RookMovedChecker;
        int row;
        //set appropriate castling checker variables according to TeamColor
        if (color == TeamColor.WHITE){
            kingMovedChecker = whiteKingMoved;
            col1RookMovedChecker = whiteCol1RookMoved;
            col8RookMovedChecker = whiteCol8RookMoved;
            row = 1;
        } else {
            kingMovedChecker = blackKingMoved;
            col1RookMovedChecker = blackCol1RookMoved;
            col8RookMovedChecker = blackCol8RookMoved;
            row = 8;
        }
        if (!kingStartPos.equals(new ChessPosition(row, 5))) {return null;} //ensure that king is in starting chess position
        if (!kingMovedChecker) {
            //calculate if castling left is possible
            if (!col1RookMovedChecker){
                ChessPosition col1RookPos = new ChessPosition(row, 1);
                if (!col1RookPos.equals(new ChessPosition(row, 1))) {return null;} //ensure that rook is in starting chess position
                ChessPosition oneLeftOfStartKing = new ChessPosition(row,4);
                ChessPosition twoLeftOfStartKing = new ChessPosition(row,3);
                castlingMoves.add (checkAndAddKingCastlingMove(color, kingStartPos, oneLeftOfStartKing, twoLeftOfStartKing));

            }
            //calculate if castling right is possible
            if (!col8RookMovedChecker){
                ChessPosition col1RookPos = new ChessPosition(row, 8);
                if (!col1RookPos.equals(new ChessPosition(row, 8))) {return null;} //ensure that rook is in starting chess position
                ChessPosition oneRightOfStartKing = new ChessPosition(row,6);
                ChessPosition twoRightOfStartKing = new ChessPosition(row,7);
                castlingMoves.add (checkAndAddKingCastlingMove(color, kingStartPos, oneRightOfStartKing, twoRightOfStartKing));
            }
        }
        return castlingMoves;
    }

    public ChessMove checkAndAddKingCastlingMove(TeamColor color, ChessPosition kingStartPos, ChessPosition oneSquareOver, ChessPosition twoSquaresOver){
        ChessMove kingCastledMove = null;
        if (board.getPiece(oneSquareOver) == null && board.getPiece(twoSquaresOver) == null){ //no pieces in between
            ChessMove testMove = new ChessMove(kingStartPos, oneSquareOver, null);
            ChessBoard clonedBoard = board.clone();
            movePieceHelper(clonedBoard, testMove);
            if (!isInCheck(color, clonedBoard)) { //not in check moving one spot
                testMove = new ChessMove(oneSquareOver, twoSquaresOver, null);
                movePieceHelper(clonedBoard, testMove);
                if (!isInCheck(color, clonedBoard)) { //not in check moving two spots
                    kingCastledMove = new ChessMove(kingStartPos, twoSquaresOver, null);
                }
            }
        }
        return kingCastledMove;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor, ChessBoard board) {
        List<ChessPosition> opponentReachablePositions = new ArrayList<>();
        ChessPosition kingPos = new ChessPosition(0, 0); //default position that doesn't exist
        ChessPosition testPos;
        ChessPiece testPiece;
        for (int r = 1; r <= 8; r++) {
            for (int c = 1; c <= 8; c++) {
                testPos = new ChessPosition(r, c);
                testPiece = board.getPiece(testPos);
                if (testPiece == null){continue;}
                if (testPiece.getTeamColor() != teamColor) {
                    for (ChessMove move : testPiece.pieceMoves(board, testPos)) {
                        opponentReachablePositions.add(move.getEndPosition());
                    }
                } else if (testPiece.getTeamColor() == teamColor && testPiece.getPieceType() == ChessPiece.PieceType.KING) {
                    kingPos = testPos; //in any game of chess, the king should always be on the board, so we should always reach this assignment
                }
            }
        }
        return opponentReachablePositions.contains(kingPos);
    }

    public boolean isInCheck(TeamColor teamColor){ //method overload
        return isInCheck(teamColor, board);
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        List<ChessMove> legalMoves = checkLegalMoves(teamColor);

        return isInCheck(teamColor) && legalMoves.isEmpty();
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        List<ChessMove> legalMoves = checkLegalMoves(teamColor);

        return !isInCheck(teamColor) && legalMoves.isEmpty();
    }

    public List<ChessMove> checkLegalMoves (TeamColor teamColor){
        List<ChessPosition> teamPiecePosition = new ArrayList<>();
        List<ChessMove> legalMoves = new ArrayList<>();
        ChessPosition testPos;
        ChessPiece testPiece;
        for (int r = 1; r <= 8; r++) {
            for (int c = 1; c <= 8; c++) {
                testPos = new ChessPosition(r, c);
                testPiece = board.getPiece(testPos);
                if (testPiece != null){
                    if (testPiece.getTeamColor() == teamColor) {
                        teamPiecePosition.add(testPos);
                    }
                }
            }
        }

        for (ChessPosition pos : teamPiecePosition){
            legalMoves.addAll(validMoves(pos));
        }
        return legalMoves;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }
}
