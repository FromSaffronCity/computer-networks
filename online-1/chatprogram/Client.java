package chatprogram;

import utility.NetworkUtility;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws IOException {
        NetworkUtility networkUtility = new NetworkUtility(new Socket("localhost", 6666));
        Scanner scanner = new Scanner(System.in);

        String name = scanner.nextLine();
        String password = scanner.nextLine();
        networkUtility.write(new ClientInfo(name, password));

        new ClientReadThread(networkUtility);
        new ClientWriteThread(networkUtility, new ClientInfo(name, password));
    }
}
