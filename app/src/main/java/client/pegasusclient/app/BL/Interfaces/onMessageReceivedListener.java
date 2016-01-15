package client.pegasusclient.app.BL.Interfaces;

/**
 * This interface is used when new message is received from the server, it fires an event to the
 * Connection Service to handle the message.
 */
public interface onMessageReceivedListener {

    /**
     * fire the message from bluetooth server
     * @param receivedMessage
     */
    void onMessageReceived(String receivedMessage);


}
