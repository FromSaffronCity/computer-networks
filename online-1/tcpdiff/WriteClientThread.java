package tcpdiff;

import utility.NetworkUtility;
import java.util.Scanner;

public class WriteClientThread implements Runnable {
    private NetworkUtility networkUtility;
    private Thread thread;
    private String name;
    private Scanner scanner;

    public WriteClientThread(NetworkUtility networkUtility, String name) {
        this.networkUtility = networkUtility;
        thread = new Thread(this);
        this.name = name;
        scanner = new Scanner(System.in);

        thread.start();
    }

    @Override
    public void run() {
        while(true) {
            networkUtility.write(new Data(name, scanner.nextLine()));
        }
        // networkUtility.closeConnection() not used
        // here, client can only sent to server (how can one client send to another client?)
    }
}
