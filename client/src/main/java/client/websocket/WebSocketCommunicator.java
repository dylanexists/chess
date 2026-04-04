package client.websocket;

import com.google.gson.Gson;
import facade.ResponseException;

import jakarta.websocket.*;
import websocket.commands.UserGameCommand;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;


import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketCommunicator extends Endpoint {
    Session session;
    ServerMessageObserver serverMessageObserver;
    Gson gson = new Gson();

    public WebSocketCommunicator(String url, ServerMessageObserver serverMessageObserver) throws ResponseException {
        try {
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/ws");
            this.serverMessageObserver = serverMessageObserver;

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    ServerMessage serverMessage = gson.fromJson(message, ServerMessage.class);
                    serverMessageObserver.notify(serverMessage); //might need to be ServerMessage not just noti
                }
            });
        } catch (DeploymentException | IOException | URISyntaxException ex) {
            throw new ResponseException(ex.getMessage(), ex);
        }
    }

    public void leaveGame(String authToken, Integer gameID) throws ResponseException {
        try {
            var command = new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID);
            this.session.getBasicRemote().sendText(gson.toJson(command));
        } catch (IOException ex) {
            throw new ResponseException(ex.getMessage(), ex);
        }
    }

    //mandatory Endpoint override
    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {}
}
