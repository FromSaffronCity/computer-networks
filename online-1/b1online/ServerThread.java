package b1online;

import utility.NetworkUtility;

import java.util.Vector;

public class ServerThread implements Runnable {
    private NetworkUtility networkUtility;
    Vector<Vector<Message>> inboxList;
    private Thread thread;

    public ServerThread(NetworkUtility networkUtility, Vector<Vector<Message>> inboxList) {
        this.networkUtility = networkUtility;
        this.inboxList = inboxList;
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        while(true) {
            Object object = networkUtility.read();

            if(object != null) {
                if(object instanceof Message) {
                    Message message = (Message) object;
                    inboxList.get(Integer.parseInt(message.getReceiver())-1).add(message);
                }
                if(object instanceof String) {
                    String id = (String) object;
                    networkUtility.write(inboxList.get(Integer.parseInt(id)-1).size());

                    for(int i=0; i<inboxList.get(Integer.parseInt(id)-1).size(); i++) {
                        networkUtility.write(inboxList.get(Integer.parseInt(id)-1).get(i));
                    }
                }
            }
        }
    }
}

