package tcpdiff;

import utility.NetworkUtility;

public class ReadClientThread implements Runnable {
    private NetworkUtility networkUtility;
    private Thread thread;

    public ReadClientThread(NetworkUtility networkUtility) {
        this.networkUtility = networkUtility;
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        /* client can add other functionality here (not only reading and printing message) */
        while(true) {
            Data data = (Data) networkUtility.read();

            if(data != null) {
                System.out.println(data);
            }
        }
        // networkUtility.closeConnection() not used
    }
}
