package server.websocket;

import chess.*;
import com.google.gson.Gson;
import dataaccess.*;
import facade.ResponseException;
import io.javalin.websocket.*;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.jetbrains.annotations.NotNull;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.io.IOException;

public class WebSocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {
    private final ConnectionManager connections = new ConnectionManager();
    private final Gson gson= new Gson();
    private final GameDao gameDao;
    private final AuthDao authDao;

    public WebSocketHandler(GameDao gameDao, AuthDao authDao) {
        this.authDao = authDao;
        this.gameDao = gameDao;
    }

    @Override
    public void handleConnect(WsConnectContext ctx) {
        System.out.println("WS connected");
        ctx.enableAutomaticPings();
    }

    @Override
    public void handleMessage(@NotNull WsMessageContext wsMessageContext) throws Exception {
        int gameID = -1;
        Session session = wsMessageContext.session;

        try {
            UserGameCommand command = serialize(wsMessageContext.message(), UserGameCommand.class);
            gameID = command.getGameID();
            String username = getUsername(command.getAuthToken());
            ChessGame game = getGame(gameID);
            saveSession(gameID, session);

            switch (command.getCommandType()) {
                case CONNECT -> connect(session, username, game, command);
                case MAKE_MOVE -> makeMove(session, username, serialize(wsMessageContext.message(), MakeMoveCommand.class));
                case LEAVE -> leave(session, username, command);
                case RESIGN -> resign(session, username, command);
            }
        } catch (DataAccessException ex) {
            sendLoadGameOrErrorMessage(session, new ErrorMessage("User or Game not found"));
        } catch (Exception ex) {
            sendLoadGameOrErrorMessage(session, new ErrorMessage("Error: undefined"));
        }
    }

    @Override
    public void handleClose(WsCloseContext ctx) {
        System.out.println("WS closed");
    }

    private <T extends UserGameCommand> T serialize(String json, Class<T> givenClass) {
        return gson.fromJson(json, givenClass);
    }

    private void connect(Session session, String username, ChessGame game, UserGameCommand command) {
        int gameID = command.getGameID();
        connections.add(gameID, session);
        sendLoadGameOrErrorMessage(session, new LoadGameMessage(game));
        String message = username + " has joined the game.";
        var notification = new NotificationMessage(message);
        connections.broadcastNotification(gameID, session, notification);
    }

    private void makeMove(Session session, String username, MakeMoveCommand command) throws DataAccessException {
        int gameID = command.getGameID();
        ChessMove move = command.getMove();
        ChessGame game = getGame(gameID);
        GameData gameData = gameDao.getGame(gameID);
        ErrorMessage errorMessage = validateMove(username, move, game, gameData);
        if (errorMessage == null) {
            ChessGame updatedGame = chessMoveLogic(move, game, gameData);
            connections.broadcastLoadGame(gameID, new LoadGameMessage(updatedGame));
            String promotionPiece = (move.getPromotionPiece() != null ? move.getPromotionPiece().name() : "");
            String message = username + " moved their " + promotionPiece +
                    " from " + prettyPrintPosition(move.getStartPosition()) +
                    " to " + prettyPrintPosition(move.getEndPosition());
            var notification = new NotificationMessage(message);
            connections.broadcastNotification(gameID, session, notification);
            ChessGame.TeamColor currentColor = updatedGame.getTeamTurn();
            if (updatedGame.isInCheckmate(currentColor) || updatedGame.isInStalemate(currentColor)) { //if game is over
                String stringColor = currentColor.name();
                var gameOverNotification = new NotificationMessage(stringColor + "has lost! Game over!");
                sendLoadGameOrErrorMessage(session, gameOverNotification);
                connections.broadcastNotification(gameID, session, gameOverNotification);
            }
        } else {sendLoadGameOrErrorMessage(session, errorMessage);}
    }

    private ErrorMessage validateMove(String username, ChessMove move, ChessGame game, GameData gData) {
        String whiteUser = gData.whiteUsername();
        String blackUser = gData.blackUsername();
        ChessGame.TeamColor white = ChessGame.TeamColor.WHITE;
        ChessGame.TeamColor black = ChessGame.TeamColor.BLACK;
        if (!username.equals(whiteUser) && !username.equals(blackUser)) {
            return new ErrorMessage("You are an observer. You cannot play!");
        } else if ((game.getTeamTurn() == white && (game.isInCheckmate(white) || game.isInStalemate(white))) ||
                (game.getTeamTurn() == black && (game.isInCheckmate(black) || game.isInStalemate(black)))) {
            return new ErrorMessage("The game has already ended! Type 'leave' to exit.");
        } else if ((username.equals(whiteUser) && game.getTeamTurn() != white) ||
                (username.equals(blackUser) && game.getTeamTurn() != black)) {
            return new ErrorMessage("It is not yet your turn! Wait for your opponent to play a move.");
        }
        ChessPosition startPos = move.getStartPosition();
        ChessPiece movingPiece = game.getBoard().getPiece(new ChessPosition(startPos.getRow(), startPos.getColumn()));
        if (movingPiece == null ||
            (username.equals(whiteUser) && movingPiece.getTeamColor() != white) ||
            (username.equals(blackUser) && movingPiece.getTeamColor() != black)) {
                return new ErrorMessage("Invalid move. You can only move your team's pieces.");
        }
        if (!game.validMoves(startPos).contains(move)) {
            return new ErrorMessage("Illegal move. Type 'highlight " +
                    prettyPrintPosition(startPos) + "' to see that piece's legal moves");
        }
        return null;
    }

    private ChessGame chessMoveLogic(ChessMove move, ChessGame game, GameData gameData) throws DataAccessException{
        ChessPosition startPos = move.getStartPosition();
        ChessPosition endPos = move.getEndPosition();
        ChessBoard beforeBoard = game.getBoard();
        ChessPiece movingPiece = beforeBoard.getPiece(startPos);
        ChessBoard afterBoard = beforeBoard.clone();
        if (move.getPromotionPiece() != null) {
            movingPiece = new ChessPiece(movingPiece.getTeamColor(), move.getPromotionPiece());
        }
        afterBoard.addPiece(endPos, movingPiece);
        afterBoard.addPiece(startPos, null);
        game.setBoard(afterBoard);
        ChessGame.TeamColor color = game.getTeamTurn();
        game.setTeamTurn(color == ChessGame.TeamColor.WHITE ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE);
        gameDao.updateGame(new GameData(gameData.gameID(),
                                gameData.whiteUsername(),
                                gameData.blackUsername(),
                                gameData.gameName(),
                                game));
        return game;
    }

    private String prettyPrintPosition(ChessPosition position) {
        int letterPos = position.getColumn();
        String letter = columnLetters[letterPos - 1];
        String number = String.valueOf(position.getRow());
        return letter + number;
    }

    private final String [] columnLetters = { //back rank setup to use in for-loop
            "a",
            "b",
            "c",
            "d",
            "e",
            "f",
            "g",
            "h"
    };

    private void leave(Session session, String username, UserGameCommand command) throws DataAccessException{
        int gameID = command.getGameID();
        removeUserFromGame(gameID, username);
        connections.remove(gameID, session);
        String message = username + " has left the game";
        var notification = new NotificationMessage(message);
        connections.broadcastNotification(gameID, session, notification);
    }

    private void resign(Session session, String username, UserGameCommand command) {

    }

    private void saveSession(Integer gameID, Session session) {
        connections.add(gameID, session);
    }

    private String getUsername(String authToken) throws NotFoundException, QueryException {
        AuthData auth = authDao.getAuth(authToken);
        return auth.username();
    }

    private ChessGame getGame(int gameID) throws NotFoundException, QueryException {
        GameData game = gameDao.getGame(gameID);
        return game.game();
    }

    private void removeUserFromGame(int gameID, String username) throws DataAccessException {
        GameData gameData = gameDao.getGame(gameID);
        if (username.equals(gameData.whiteUsername())){
            gameDao.updateGame(new GameData(gameData.gameID(),
                                null,
                                gameData.blackUsername(),
                                gameData.gameName(),
                                gameData.game()));
        } else if (username.equals(gameData.blackUsername())) {
            gameDao.updateGame(new GameData(gameData.gameID(),
                    gameData.whiteUsername(),
                    null,
                    gameData.gameName(),
                    gameData.game()));
        }
    }

    public void sendLoadGameOrErrorMessage(Session session, ServerMessage loadGameOrErrorMessage) {
        try {
            session.getRemote().sendString(gson.toJson(loadGameOrErrorMessage));
        } catch (IOException e) {
            throw new ResponseException("invalid session");
        }
    }
}
