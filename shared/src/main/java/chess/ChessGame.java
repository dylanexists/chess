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
        throw new RuntimeException("Not implemented");
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
            ChessPosition testPieceStartPos = move.getStartPosition();
            ChessPosition testPieceEndPos = move.getEndPosition();
            //change our test piece's position from current position to move position
            clonedBoard.grid[testPieceEndPos.getRow()-1][testPieceEndPos.getColumn()-1] =
                    clonedBoard.grid[testPieceStartPos.getRow()-1][testPieceStartPos.getColumn()-1];
            clonedBoard.grid[testPieceStartPos.getRow()-1][testPieceStartPos.getColumn()-1] = null;
            if (! isInCheck(color, clonedBoard)){
                validMoves.add(move);
            }
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
                if (testPiece != null){
                    if (testPiece.getTeamColor() != teamColor) {
                        for (ChessMove move : testPiece.pieceMoves(board, testPos)) {
                            opponentReachablePositions.add(move.getEndPosition());
                        }
                    } else if (testPiece.getTeamColor() == teamColor && testPiece.getPieceType() == ChessPiece.PieceType.KING) {
                        kingPos = testPos; //in any game of chess, the king should always be on the board, so we should always reach this assignment
                    }
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
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
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
