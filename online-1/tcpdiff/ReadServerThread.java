package tcpdiff;

import utility.NetworkUtility;

public class ReadServerThread implements Runnable {
    private NetworkUtility networkUtility;
    private Thread thread;

    public ReadServerThread(NetworkUtility networkUtility) {
        this.networkUtility = networkUtility;
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        /* server can add other functionality here (not only reading and printing message) */
        while(true) {
            Data data = (Data) networkUtility.read();

            if(data != null) {
                System.out.println(data);
            }
        }
        // networkUtility.closeConnection() not used
        // server may take login password and check, forward message to other client (how?)
    }
}
