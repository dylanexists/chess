package server.websocket;

import com.google.gson.Gson;
import io.javalin.websocket.*;
import org.eclipse.jetty.websocket.api.Session;
import org.jetbrains.annotations.NotNull;
import websocket.commands.UserGameCommand;

public class WebSocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {
    private final ConnectionManager connections = new ConnectionManager();
    private final Gson gson= new Gson();

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
            String authToken = command.getAuthToken();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void handleClose(WsCloseContext ctx) {
        System.out.println("WS closed");
    }

    private <T extends UserGameCommand> T serialize(String json, Class<T> givenClass) {
        return gson.fromJson(json, givenClass);
    }
}
