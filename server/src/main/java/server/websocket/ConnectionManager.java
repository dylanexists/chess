package server.websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    public final ConcurrentHashMap<Integer, Set<PlayerSession>> connections = new ConcurrentHashMap<>();

    public void add(int gameID, PlayerSession playerSession) {
        Set<PlayerSession> sessionSet =
                connections.computeIfAbsent(gameID, a -> ConcurrentHashMap.newKeySet());
        sessionSet.add(playerSession);
    }

    public void remove(int gameID, PlayerSession playerSession) {
        connections.computeIfPresent(gameID, (id, sessions) -> {
            sessions.remove(playerSession);
            return sessions.isEmpty() ? null : sessions;
        });
    }

    public void broadcastNotification(int gameID, Session rootSession, NotificationMessage notification) {
        Set<PlayerSession> sessions = connections.get(gameID);
        if (sessions == null) {return;}
        String message = new Gson().toJson(notification);
        for (PlayerSession s : sessions) {
            if (s.session().equals(rootSession)) {
                continue; //excludes rootSession
            }
            try {
                if (s.session().isOpen()) {
                    s.session().getRemote().sendString(message);
                } //possibly remove session if not open??
            } catch (IOException ex) {remove(gameID, s);} //remove non-existent (broken) session
        }
    }

    public void broadcastLoadGame(int gameID, LoadGameMessage gameMessage) {
        Set<PlayerSession> sessions = connections.get(gameID);
        if (sessions == null) {return;}
        String message = new Gson().toJson(gameMessage);
        for (PlayerSession s : sessions) {
            try {
                if (s.session().isOpen()) {
                    s.session().getRemote().sendString(message);
                } //possibly remove session if not open??
            } catch (IOException ex) {remove(gameID, s);} //remove non-existent (broken) session
        }
    }

}
