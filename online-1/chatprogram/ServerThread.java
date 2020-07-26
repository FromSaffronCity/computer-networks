package chatprogram;

import utility.NetworkUtility;

import java.util.Hashtable;
import java.util.Vector;

public class ServerThread implements Runnable {
    private NetworkUtility networkUtility;
    private Thread thread;
    private Hashtable<String, ClientUtil> clientInfoTable;
    private Vector<String> clientList;

    public ServerThread(NetworkUtility networkUtility, Hashtable<String, ClientUtil> clientInfoTable, Vector<String> clientList) {
        this.networkUtility = networkUtility;
        this.clientInfoTable = clientInfoTable;
        this.clientList = clientList;
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        while(true) {
            Object object = networkUtility.read();

            if(object != null) {
                Packet packet = (Packet) object;

                /* password checking */
                if(packet.getPassword().equals(clientInfoTable.get(packet.getSource()).getPassword())) {
                    if(packet.getType().equalsIgnoreCase("s")) {
                        System.out.println(packet);

                    } else if(packet.getType().equalsIgnoreCase("u")) {
                        ClientUtil temp = clientInfoTable.get(packet.getDestination());

                        if(temp != null) {
                            temp.getNetworkUtility().write(packet);
                        } else {
                            /* do nothing */
                        }

                    } else if(packet.getType().equalsIgnoreCase("b")) {
                        for(int i=0; i<clientList.size(); i++) {
                            if(!clientList.get(i).equalsIgnoreCase(packet.getSource())) {
                                clientInfoTable.get(clientList.get(i)).getNetworkUtility().write(packet);
                            }
                        }

                    } else {
                        /* do nothing */
                    }
                } else {
                    /* do nothing */
                }
            }
        }
    }
}
