package groupchat;

import utility.NetworkUtility;

import java.util.Scanner;

public class ClientWriteThread implements Runnable {
    private NetworkUtility networkUtility;
    private Thread thread;

    public ClientWriteThread(NetworkUtility networkUtility) {
        this.networkUtility = networkUtility;
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);

        while(true) {
            networkUtility.write(scanner.nextLine());
        }
    }
}
