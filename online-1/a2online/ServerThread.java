package a2online;

import utility.NetworkUtility;

import java.util.Vector;

public class ServerThread implements Runnable {
    private NetworkUtility networkUtility;
    private Vector<String> statusTable;
    private Thread thread;

    public ServerThread(NetworkUtility networkUtility, Vector<String> statusTable) {
        this.networkUtility = networkUtility;
        this.statusTable = statusTable;
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        while(true) {
            Object object = networkUtility.read();

            if(object != null) {
                String input = (String) object;
                String[] request = input.split(" ");

                if(request[1].equalsIgnoreCase("GET")) {
                    String status = statusTable.get(Integer.parseInt(request[2])-1);
                    networkUtility.write((status==null? "no such client": status));
                    System.out.println("SERVER: status sent to client"+request[0]);
                }
                if(request[1].equalsIgnoreCase("SET")) {
                    statusTable.remove(Integer.parseInt(request[0])-1);
                    statusTable.insertElementAt(request[2], Integer.parseInt(request[0])-1);
                    System.out.println("SERVER: status set for client"+request[0]);
                }
            }
        }
    }
}
