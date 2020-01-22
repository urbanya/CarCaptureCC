package hnu.example.bttest;

/**
 * INterface for Activtities to implement
 */
public interface IBTMsgClient {
     void receiveMessage(String msg);
     void receiveConnectStatus(boolean isConnected);
     void handleException(Exception e);
}
