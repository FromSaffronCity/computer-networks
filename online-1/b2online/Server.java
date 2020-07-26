package b2online;

import utility.NetworkUtility;

import java.io.IOException;
import java.net.ServerSocket;

public class Server {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(6666);

        System.out.println("SERVER: waiting for SENDER connection");
        NetworkUtility senderNU = new NetworkUtility(serverSocket.accept());

        System.out.println("SERVER: waiting for RECEIVER connection");
        NetworkUtility receiverNU = new NetworkUtility(serverSocket.accept());

        while(true) {
            Object object = senderNU.read();

            if(object != null) {
                receiverNU.write(object);
            }

            System.out.println("SERVER: message received from SENDER and forwarded to CLIENT");
        }
    }
}
