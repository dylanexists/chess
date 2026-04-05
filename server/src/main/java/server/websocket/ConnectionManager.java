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
    public final ConcurrentHashMap<Integer, Set<Session>> connections = new ConcurrentHashMap<>();

    public void add(int gameID, Session session) {
        Set<Session> sessionSet =
                connections.computeIfAbsent(gameID, a -> ConcurrentHashMap.newKeySet());
        sessionSet.add(session);
    }

    public void remove(int gameID, Session session) {
        connections.computeIfPresent(gameID, (id, sessions) -> {
            sessions.remove(session);
            return sessions.isEmpty() ? null : sessions;
        });
    }

    public void broadcastNotification(int gameID, Session rootSession, NotificationMessage notification) {
        Set<Session> sessions = connections.get(gameID);
        if (sessions == null) {return;}
        String message = new Gson().toJson(notification);
        for (Session s : sessions) {
            if (s.equals(rootSession)) {
                continue; //excludes rootSession
            }
            try {
                if (s.isOpen()) {
                    s.getRemote().sendString(message);
                } //possibly remove session if not open??
            } catch (IOException ex) {remove(gameID, s);} //remove non-existent (broken) session
        }
    }

    public void broadcastLoadGame(int gameID, LoadGameMessage gameMessage) {
        Set<Session> sessions = connections.get(gameID);
        if (sessions == null) {return;}
        String message = new Gson().toJson(gameMessage);
        for (Session s : sessions) {
            try {
                if (s.isOpen()) {
                    s.getRemote().sendString(message);
                } //possibly remove session if not open??
            } catch (IOException ex) {remove(gameID, s);} //remove non-existent (broken) session
        }
    }

}
