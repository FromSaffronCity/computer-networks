package groupchat;

import utility.NetworkUtility;

import java.io.IOException;
import java.net.Socket;

public class Client {
    public static void main(String[] args) throws IOException {
        NetworkUtility networkUtility = new NetworkUtility(new Socket("192.168.0.106", 6666));
        networkUtility.write("sahil");

        new ClientWriteThread(networkUtility);
        new ClientReadThread(networkUtility);
    }
}
