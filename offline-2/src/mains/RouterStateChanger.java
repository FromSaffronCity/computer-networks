package mains;

import interfaces.Constants;

import java.util.Random;

public class RouterStateChanger implements Runnable {
    private Random random;
    static boolean isLocked = false;
    static final Boolean message = true;

    public RouterStateChanger() {
        random = new Random();
        new Thread(this).start();
    }

    private void revertRandomRouter() {
        int routerID = random.nextInt(NetworkLayerServer.routers.size());
        NetworkLayerServer.routers.get(routerID).revertState();
        System.out.println(">> state changed for router with ID: "+NetworkLayerServer.routers.get(routerID).getRouterId());
    }

    @Override
    public void run() {
        while(true) {
            if(isLocked) {
                try {
                    synchronized(message) {
                        message.wait();
                    }
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if(random.nextDouble() < Constants.LAMBDA) {
                revertRandomRouter();
            }
            try {
                Thread.sleep(1000);
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
