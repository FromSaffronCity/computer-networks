package a2online;

import utility.NetworkUtility;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Vector;

public class Server {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(6666);
        Vector<String> statusTable = new Vector<>();  // as vector is thread safe
        int clientCount=0;

        while(true) {
            NetworkUtility networkUtility = new NetworkUtility(serverSocket.accept());
            System.out.println("SERVER: connected to client"+(++clientCount));
            networkUtility.write(new String(clientCount+""));  // NOTICE
            statusTable.add("No status");
            new ServerThread(networkUtility, statusTable);
        }
    }
}
