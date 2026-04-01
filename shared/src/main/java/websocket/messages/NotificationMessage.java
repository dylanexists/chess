package websocket.messages;

public class NotificationMessage extends ServerMessage{
    private final String notificationMessage;

    public NotificationMessage(String notificationMessage){
        super(ServerMessageType.ERROR);
        this.notificationMessage = notificationMessage;
    }

    public String getMessage() {return notificationMessage;}
}
