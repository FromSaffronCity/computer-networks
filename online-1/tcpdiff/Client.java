package tcpdiff;

import utility.NetworkUtility;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.print(">> ENTER YOUR NAME: ");
        String name = scanner.nextLine();

        NetworkUtility networkUtility = new NetworkUtility(new Socket("localhost", 6666));

        networkUtility.write(new Data(name, ""));  // NOTICE: sending client name to erver
        new ReadClientThread(networkUtility);
        new WriteClientThread(networkUtility, name);

        return ;
    }
}
