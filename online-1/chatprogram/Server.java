package chatprogram;

import utility.NetworkUtility;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Hashtable;
import java.util.Vector;

public class Server {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(6666);
        Hashtable<String, ClientUtil> clientInfoTable = new Hashtable<>();
        Vector<String> clientList = new Vector<>();

        while(true) {
            NetworkUtility networkUtility = new NetworkUtility(serverSocket.accept());
            Object object = networkUtility.read();

            if(object != null) {
                ClientInfo temp = (ClientInfo) object;
                clientInfoTable.put(temp.getName(), new ClientUtil(temp.getPassword(), networkUtility));
                clientList.add(temp.getName());

                new ServerThread(networkUtility, clientInfoTable, clientList);
            }
        }
    }
}
