package a1online;

import utility.NetworkUtility;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;

public class LocalServer {
    public static void main(String[] args) throws IOException {
        Hashtable<String, String> list = new Hashtable<>();
        list.put("buet.ac.bd", "103.94.135.240");
        list.put("uiu.ac.bd", "103.109.52.58");

        NetworkUtility nu = new NetworkUtility(new Socket("localhost", 50000));
        System.out.println(">> connected to root server");
        ServerSocket ss = new ServerSocket(6666);

        while(true) {
            System.out.println(">> waiting for connection ...");
            NetworkUtility nu2 = new NetworkUtility(ss.accept());
            System.out.println(">> connected to new client");
            new LocalThread(nu, nu2, list);
        }
    }
}
