package tcpdiff;

import utility.NetworkUtility;
import java.util.HashMap;
import java.util.Scanner;

public class WriteServerThread implements Runnable {
    private HashMap<String, NetworkUtility> hashMap;
    private Thread thread;
    private String name;
    private Scanner scanner;

    public WriteServerThread(HashMap<String, NetworkUtility> hashMap, String name) {
        this.hashMap = hashMap;
        thread = new Thread(this);
        this.name = name;
        scanner = new Scanner(System.in);

        thread.start();
    }

    @Override
    public void run() {
        while(true) {
            NetworkUtility networkUtility = hashMap.get(scanner.nextLine());

            if(networkUtility != null) {
                networkUtility.write(new Data(name, scanner.nextLine()));
            } else {
                /* no such client: do nothing */
            }
        }
        // networkUtility.closeConnection() not used
    }
}
