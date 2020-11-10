package mains;

import interfaces.Constants;
import utilities.*;

import java.util.Vector;
import java.util.Random;

public class ServerThread implements Runnable {
    private EndDevice endDevice;
    private Vector<IPAddress> activeClientList;
    private NetworkUtility networkUtility;

    public ServerThread(EndDevice endDevice, Vector<IPAddress> activeClientList, NetworkUtility networkUtility) {
        this.endDevice = endDevice;
        this.activeClientList = activeClientList;
        this.networkUtility = networkUtility;
        System.out.println("server ready for client"+NetworkLayerServer.clientCount);
        new Thread(this).start();
    }

    private boolean deliverPacket(Packet packet) {
        boolean isSuccessful = true;
        String routingPathWithHopCount = "source/"+packet.getSourceIP()+"("+packet.getHopCount()+")";
        String routingTables = "";

        /* looking for source and destination gateway-routers for sending given packet */
        Router sourceGateway = NetworkLayerServer.routerMap.get(NetworkLayerServer.deviceIDtoRouterID.get(NetworkLayerServer.endDeviceMap.get(packet.getSourceIP()).getDeviceID()));
        Router destinationGateway = NetworkLayerServer.routerMap.get(NetworkLayerServer.deviceIDtoRouterID.get(NetworkLayerServer.endDeviceMap.get(packet.getDestinationIP()).getDeviceID()));

        /* forwarding packet via sequence of routers */
        if(!sourceGateway.getState()) {
            isSuccessful = false;
        } else {
            Router currentRouter=sourceGateway, previousRouter=null;
            packet.setHopCount(packet.getHopCount()+1);
            routingPathWithHopCount += " -> router"+currentRouter.getRouterId()+"("+packet.getHopCount()+")";
            routingTables += currentRouter.toStringRoutingTable();

            while(currentRouter.getRouterId()!=destinationGateway.getRouterId() && isSuccessful && packet.getHopCount()<Constants.INFINITY) {
                for(int i=0; previousRouter!=null && i<currentRouter.getRoutingTable().size(); i++) {
                    if(currentRouter.getRoutingTable().get(i) == null) {
                        isSuccessful = false;
                        break;
                    }
                    if(currentRouter.getRoutingTable().get(i).getRouterId() == previousRouter.getRouterId()) {
                        if(currentRouter.getRoutingTable().get(i).getGatewayRouterId() == -1) {
                            /* case-3(b) */
                            currentRouter.getRoutingTable().get(i).setDistance(1);
                            currentRouter.getRoutingTable().get(i).setGatewayRouterId(previousRouter.getRouterId());
                            RouterStateChanger.isLocked = true;
                            if(NetworkLayerServer.isDVR) {
                                NetworkLayerServer.DVR(currentRouter.getRouterId());
                            } else {
                                NetworkLayerServer.simpleDVR(currentRouter.getRouterId());
                            }
                            synchronized(RouterStateChanger.message) {
                                RouterStateChanger.isLocked = false;
                                RouterStateChanger.message.notify();
                            }
                        }
                        break;
                    }
                }
                for(int i=0; i<currentRouter.getRoutingTable().size(); i++) {
                    if(currentRouter.getRoutingTable().get(i) == null) {
                        isSuccessful = false;
                        break;
                    }
                    if(currentRouter.getRoutingTable().get(i).getRouterId() == destinationGateway.getRouterId()) {
                        if(currentRouter.getRoutingTable().get(i).getGatewayRouterId() == -1) {
                            isSuccessful = false;
                            break;
                        }
                        previousRouter = currentRouter;
                        currentRouter = NetworkLayerServer.routerMap.get(currentRouter.getRoutingTable().get(i).getGatewayRouterId());
                        if(!currentRouter.getState()) {
                            /* case-3(a) */
                            isSuccessful = false;
                            for(int j=0; j<previousRouter.getRoutingTable().size(); j++) {
                                if(previousRouter.getRoutingTable().get(j).getRouterId() == currentRouter.getRouterId()) {
                                    previousRouter.getRoutingTable().get(j).setDistance(Constants.INFINITY);
                                    previousRouter.getRoutingTable().get(j).setGatewayRouterId(-1);
                                    break;
                                }
                            }
                            RouterStateChanger.isLocked = true;
                            if(NetworkLayerServer.isDVR) {
                                NetworkLayerServer.DVR(previousRouter.getRouterId());
                            } else {
                                NetworkLayerServer.simpleDVR(previousRouter.getRouterId());
                            }
                            synchronized(RouterStateChanger.message) {
                                RouterStateChanger.isLocked = false;
                                RouterStateChanger.message.notify();
                            }
                        } else {
                            packet.setHopCount(packet.getHopCount()+1);
                            routingPathWithHopCount += " -> router"+currentRouter.getRouterId()+"("+packet.getHopCount()+")";
                            routingTables += currentRouter.toStringRoutingTable();
                        }
                        break;
                    }
                }
            }
            if(packet.getHopCount() >= Constants.INFINITY) {
                isSuccessful = false;
            }
            if(isSuccessful) {
                packet.setHopCount(packet.getHopCount()+1);
                routingPathWithHopCount += " -> destination/"+packet.getDestinationIP()+"("+packet.getHopCount()+")";
            }
        }
        if(packet.getSpecialMessage()!=null && packet.getSpecialMessage().equalsIgnoreCase("SHOW_ROUTE")) {
            packet.setSpecialMessage("routing path with hop-count: "+routingPathWithHopCount+"\n\nrouting tables:\n\n"+routingTables);
        }
        return isSuccessful;
    }

    @Override
    public void run() {
        Random random = new Random();
        activeClientList.add(endDevice.getIPAddress());

        /* sending EndDevice configuration to Client */
        networkUtility.write(endDevice);

        /* receiving packet from Client, randomly setting recipient, calling deliverPacket(packet), sending packet back to Client */
        while(true) {
            Packet packet = (Packet) networkUtility.read();
            if(packet.getSpecialMessage()!=null && packet.getSpecialMessage().equalsIgnoreCase("END")) {
                break;
            }
            packet.setDestinationIP(activeClientList.get(random.nextInt(activeClientList.size())));
            if(deliverPacket(packet)) {
                packet.setMessage("packet successfully sent with hop-count: "+packet.getHopCount());
            } else {
                packet.setMessage("packet dropped");
                packet.setHopCount(-1);
            }
            packet.setSourceIP(null);
            packet.setDestinationIP(null);
            networkUtility.write(packet);
        }

        /* removing endDevice from active client list */
        activeClientList.remove(endDevice.getIPAddress());
        networkUtility.closeConnection();
    }
}
