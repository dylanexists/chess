package server.websocket;

import org.eclipse.jetty.websocket.api.Session;

public record PlayerSession(String username, Session session){}
