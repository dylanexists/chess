package server.websocket;

import chess.*;
import com.google.gson.Gson;
import dataaccess.*;
import facade.ConsoleTextHandler;
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
import java.util.Set;

public class WebSocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {
    private final ConnectionManager connectionMan = new ConnectionManager();
    private final ConsoleTextHandler consoleTextHandler = new ConsoleTextHandler();
    private final GameDao gameDao;
    private final AuthDao authDao;
    private final Gson gson = new Gson();

    public WebSocketHandler(GameDao gameDao, AuthDao authDao) {
        this.authDao = authDao;
        this.gameDao = gameDao;
    }

    @Override
    public void handleConnect(WsConnectContext ctx) {
        ctx.enableAutomaticPings();
    }

    @Override
    public void handleMessage(@NotNull WsMessageContext wsMessageContext) {
        int gameID;
        Session session = wsMessageContext.session;

        try {
            UserGameCommand command = serialize(wsMessageContext.message(), UserGameCommand.class);
            gameID = command.getGameID();
            String username = getUsername(command.getAuthToken());
            ChessGame game = getGame(gameID);
            PlayerSession playerSession = new PlayerSession(username, session);
            saveSession(gameID, playerSession);

            switch (command.getCommandType()) {
                case CONNECT -> connect(playerSession, username, game, command);
                case MAKE_MOVE -> makeMove(playerSession, username, serialize(wsMessageContext.message(), MakeMoveCommand.class));
                case LEAVE -> leave(playerSession, username, command.getGameID());
                case RESIGN -> resign(playerSession, username, command);
            }
        } catch (DataAccessException ex) {
            sendSessionOnlyMessage(session, new ErrorMessage("User or Game not found"));
        } catch (Exception ex) {
            sendSessionOnlyMessage(session, new ErrorMessage("Error: undefined"));
        }
    }

    @Override
    public void handleClose(@NotNull WsCloseContext ctx) { //IntelliJ suggests doing NotNull
        Session session = ctx.session;
        Integer gameID = findGameIdOfSession(session);
        PlayerSession playerSession = findPlayerSession(session, gameID);
        try {
            if (playerSession != null) {
                leave(playerSession, playerSession.username(), gameID);
            }
        } catch (DataAccessException ex) {
            throw new ResponseException("On-close socket method went crazy", ex);
        }
    }


    private Integer findGameIdOfSession(Session session) {
        for (var entry : connectionMan.connections.entrySet()) {
            Integer key = entry.getKey();
            Set<PlayerSession> sessions = entry.getValue();
            for (PlayerSession ps : sessions) {
                if (ps.session().equals(session)) {
                    return key;
                }
            }
        }
        return null;
    }

    private PlayerSession findPlayerSession(Session session, Integer gameID) {
        if (gameID == null) {return null;}
        Set<PlayerSession> sessions = connectionMan.connections.get(gameID);
        if (sessions == null) {return null;}
        for (PlayerSession playerSession : sessions) {
            if (playerSession.session().equals(session)) {return playerSession;}
        }
        return null;
    }

    private <T extends UserGameCommand> T serialize(String json, Class<T> givenClass) {
        return gson.fromJson(json, givenClass);
    }

    private void connect(PlayerSession playerSession, String username, ChessGame game, UserGameCommand command) {
        int gameID = command.getGameID();
        connectionMan.add(gameID, playerSession);
        sendSessionOnlyMessage(playerSession.session(), new LoadGameMessage(game));
        String message = username + " has joined the game.";
        var notification = new NotificationMessage(message);
        connectionMan.broadcastNotification(gameID, playerSession.session(), notification);
    }

    private void makeMove(PlayerSession playerSession, String username, MakeMoveCommand command) throws DataAccessException {
        int gameID = command.getGameID();
        ChessMove move = command.getMove();
        ChessGame game = getGame(gameID);
        GameData gameData = gameDao.getGame(gameID);
        ErrorMessage errorMessage = validateMove(username, move, game, gameData);
        if (errorMessage == null) {
            ChessGame updatedGame = chessMoveLogic(move, game, gameData);
            connectionMan.broadcastLoadGame(gameID, new LoadGameMessage(updatedGame));
            String promotionPiece = (move.getPromotionPiece() != null ? move.getPromotionPiece().name() : "");
            String message = username + " moved their " + promotionPiece +
                    " from " + consoleTextHandler.prettyPrintPosition(move.getStartPosition()) +
                    " to " + consoleTextHandler.prettyPrintPosition(move.getEndPosition());
            var notification = new NotificationMessage(message);
            connectionMan.broadcastNotification(gameID, playerSession.session(), notification);
            if (updatedGame.isGameOver()) { //if game is over
                String stringColor = updatedGame.getLoser().name();
                var gameOverNotification = new NotificationMessage(stringColor + " has lost! Game over!");
                sendSessionOnlyMessage(playerSession.session(), gameOverNotification);
                connectionMan.broadcastNotification(gameID, playerSession.session(), gameOverNotification);
            }
        } else {sendSessionOnlyMessage(playerSession.session(), errorMessage);}
    }

    private void leave(PlayerSession playerSession, String username, Integer gameID) throws DataAccessException{
        removeUserFromGame(gameID, username);
        connectionMan.remove(gameID, playerSession);
        String message = username + " has left the game";
        var notification = new NotificationMessage(message);
        connectionMan.broadcastNotification(gameID, playerSession.session(), notification);
    }

    private void resign(PlayerSession playerSession, String username, UserGameCommand command) throws DataAccessException {
        int gameID = command.getGameID();
        ChessGame game = getGame(gameID);
        GameData gameData = gameDao.getGame(gameID);
        ErrorMessage errorMessage = validateResign(username, game, gameData);
        if (errorMessage == null) {
            game.setGameOver(true);
            ChessGame.TeamColor loser = username.equals(gameData.whiteUsername()) ?
                        ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
            game.setLoser(loser);
            gameDao.updateGame(new GameData(gameData.gameID(),
                    gameData.whiteUsername(),
                    gameData.blackUsername(),
                    gameData.gameName(),
                    game));
            String message = username + " has resigned. Game Over!";
            var notification = new NotificationMessage(message);
            sendSessionOnlyMessage(playerSession.session(), notification);
            connectionMan.broadcastNotification(gameID, playerSession.session(), notification);
        } else {sendSessionOnlyMessage(playerSession.session(), errorMessage);}
    }

    private ErrorMessage validateResign(String username, ChessGame game, GameData gData) {
        String whiteUser = gData.whiteUsername();
        String blackUser = gData.blackUsername();
        if (!username.equals(whiteUser) && !username.equals(blackUser)) {
            return new ErrorMessage("You are an observer. You cannot resign!");
        } else if (game.isGameOver()) {
            return new ErrorMessage("The game has already ended! Type 'leave' to exit.");
        }
        return null;
    }

    private ErrorMessage validateMove(String username, ChessMove move, ChessGame game, GameData gData) {
        String whiteUser = gData.whiteUsername();
        String blackUser = gData.blackUsername();
        ChessGame.TeamColor white = ChessGame.TeamColor.WHITE;
        ChessGame.TeamColor black = ChessGame.TeamColor.BLACK;
        if (!username.equals(whiteUser) && !username.equals(blackUser)) {
            return new ErrorMessage("You are an observer. You cannot play!");
        } else if (game.isGameOver()) {
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
                    consoleTextHandler.prettyPrintPosition(startPos) + "' to see that piece's legal moves");
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
        ChessGame.TeamColor newColor = color == ChessGame.TeamColor.WHITE ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;
        game.setTeamTurn(newColor);
        if (game.isInCheckmate(newColor) || game.isInStalemate(newColor)) { //if game is over
            game.setGameOver(true);
            game.setLoser(newColor);
        }
        gameDao.updateGame(new GameData(gameData.gameID(),
                                gameData.whiteUsername(),
                                gameData.blackUsername(),
                                gameData.gameName(),
                                game));
        return game;
    }

    private void saveSession(Integer gameID, PlayerSession playerSession) {
        connectionMan.add(gameID, playerSession);
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

    public void sendSessionOnlyMessage(Session session, ServerMessage selfSendingMessage) {
        try {
            session.getRemote().sendString(gson.toJson(selfSendingMessage));
        } catch (IOException e) {
            throw new ResponseException("invalid session");
        }
    }
}
