package chatprogram;

import utility.NetworkUtility;

public class ClientReadThread implements Runnable {
    private NetworkUtility networkUtility;
    private Thread thread;

    public ClientReadThread(NetworkUtility networkUtility) {
        this.networkUtility = networkUtility;
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        while(true) {
            Object object = networkUtility.read();

            if(object != null) {
                System.out.println(object);
            }
        }
    }
}
