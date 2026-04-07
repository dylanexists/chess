package websocket.messages;

import chess.ChessGame;
import chess.ChessPosition;

public class LoadGameMessage extends ServerMessage {
    private final ChessGame game;
    private final ChessPosition highlightPosition;

    public LoadGameMessage(ChessGame game, ChessPosition highlightPosition) {
        super(ServerMessageType.LOAD_GAME);
        this.game = game;
        this.highlightPosition = highlightPosition;
    }

    public ChessGame getGame() {return game;}

    public ChessPosition getHighlightPosition() {return highlightPosition;}
}
