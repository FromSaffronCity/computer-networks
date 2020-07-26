package a1online;

import utility.NetworkUtility;

import java.util.Hashtable;

public class LocalThread implements Runnable {
    private NetworkUtility root;
    private NetworkUtility local;
    private Hashtable<String, String> list;
    private Thread thread;

    public LocalThread(NetworkUtility root, NetworkUtility local, Hashtable<String, String> list) {
        this.root = root;
        this.local = local;
        this.list = list;

        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        while(true) {
            Object o = local.read();

            if(o != null) {
                String domain = (String) o;

                if(list.containsKey(domain)) {
                    local.write(list.get(domain));
                    System.out.println(">> IP address found");
                } else {
                    root.write(domain);
                    System.out.println(">> IP address not found, forwarding to root");
                    local.write(root.read());
                    System.out.println(">> response from root forwarded to client");
                }
            }
        }
    }
}
