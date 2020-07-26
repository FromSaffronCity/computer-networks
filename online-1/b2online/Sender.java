package b2online;

import utility.NetworkUtility;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Sender {
    public static void main(String[] args) throws IOException {
        NetworkUtility networkUtility = new NetworkUtility(new Socket("localhost", 6666));
        System.out.println("SENDER: connected to server");

        Scanner scanner = new Scanner(System.in);

        while(true) {
            networkUtility.write("SENDER: "+scanner.nextLine());
            System.out.println("SENDER: message sent to RECEIVER");
        }
    }
}
