package websocket.messages;

public class NotificationMessage extends ServerMessage{
    private final String message;

    public NotificationMessage(String notificationMessage){
        super(ServerMessageType.NOTIFICATION);
        this.message = notificationMessage;
    }

    public String getMessage() {return message;}
}
