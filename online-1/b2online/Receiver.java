package b2online;

import utility.NetworkUtility;

import java.io.IOException;
import java.net.Socket;

public class Receiver {
    public static void main(String[] args) throws IOException {
        NetworkUtility networkUtility = new NetworkUtility(new Socket("localhost", 6666));
        System.out.println("RECEIVER: connected to server");

        while(true) {
            Object object = networkUtility.read();

            if(object != null) {
                System.out.println((String) object);
            }
        }
    }
}