package chatprogram;

import utility.NetworkUtility;

import java.util.Scanner;

public class ClientWriteThread implements Runnable {
    private NetworkUtility networkUtility;
    private Thread thread;
    private ClientInfo clientInfo;

    public ClientWriteThread(NetworkUtility networkUtility, ClientInfo clientInfo) {
        this.networkUtility = networkUtility;
        this.clientInfo = clientInfo;
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);

        while(true) {
            networkUtility.write(new Packet(clientInfo.getName(), clientInfo.getPassword(), scanner.nextLine(), scanner.nextLine(), scanner.nextLine()));
            /*
                NOTICE:-

                1. S->server, U->unicast, B->broadcast
                2. destination client name
                3. message
            */
        }
    }
}
