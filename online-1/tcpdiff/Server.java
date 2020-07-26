package tcpdiff;

import utility.NetworkUtility;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;

public class Server {
    public static void main(String[] args) throws IOException {
        HashMap<String, NetworkUtility> hashMap = new HashMap<>();
        ServerSocket serverSocket = new ServerSocket(6666);

        while(true) {
            System.out.println(">> WAITING FOR CONNECTION ...");

            NetworkUtility networkUtility = new NetworkUtility(serverSocket.accept());
            System.out.println(">> CONNECTION ESTABLISHED");
            Data data = (Data) networkUtility.read();  // NOTICE: receiving client name

            if(data != null) {
                hashMap.put(data.getName(), networkUtility);
            }

            new ReadServerThread(networkUtility);
            new WriteServerThread(hashMap, "RootServer");
        }
    }
}
