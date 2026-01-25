package chess;

import piececalc.PawnMoveCalculator;
import piececalc.RookMoveCalculator;
import piececalc.KnightMoveCalculator;
import piececalc.BishopMoveCalculator;
import piececalc.QueenMoveCalculator;
import piececalc.KingMoveCalculator;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece implements Cloneable{

    private final ChessGame.TeamColor pieceColor;
    private PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }

    @Override
    public ChessPiece clone() {
        try {
            ChessPiece clone = (ChessPiece) super.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    @Override
    public String toString() {
        return "ChessPiece{" +
                pieceColor +
                type +
                '}';
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    public void setPieceType(PieceType newType) {
        type = newType;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece piece = board.getPiece(myPosition);
        if (piece.getPieceType() == PieceType.PAWN){
            return new PawnMoveCalculator().calculateMoves(board, myPosition);
        }else if (piece.getPieceType() == PieceType.ROOK){
            return new RookMoveCalculator().calculateMoves(board, myPosition);
        } else if (piece.getPieceType() == PieceType.KNIGHT){
            return new KnightMoveCalculator().calculateMoves(board, myPosition);
        } else if (piece.getPieceType() == PieceType.BISHOP){
            return new BishopMoveCalculator().calculateMoves(board, myPosition);
        } else if (piece.getPieceType() == PieceType.QUEEN){
            return new QueenMoveCalculator().calculateMoves(board, myPosition);
        } else if (piece.getPieceType() == PieceType.KING){
            return new KingMoveCalculator().calculateMoves(board, myPosition);
        }
        return null;
    }
}
