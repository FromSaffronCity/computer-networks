package utilities;

import interfaces.Constants;
import mains.NetworkLayerServer;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

public class Router {
    private int routerId;
    private ArrayList<IPAddress> interfaceAddresses;
    private ArrayList<RoutingTableEntry> routingTable;
    private ArrayList<Integer> neighborRouterIDs;
    private boolean state;
    private Map<Integer, IPAddress> gatewayIDtoIP;

    public Router(int routerId, ArrayList<IPAddress> interfaceAddresses, ArrayList<Integer> neighborRouterIDs, Map<Integer, IPAddress> gatewayIDtoIP) {
        this.routerId = routerId;
        this.interfaceAddresses = interfaceAddresses;
        routingTable = new ArrayList<>();
        this.neighborRouterIDs = neighborRouterIDs;
        Random random = new Random();
        state = random.nextDouble()<0.8;
        state = true;
        this.gatewayIDtoIP = gatewayIDtoIP;
    }

    public void initiateRoutingTable() {
        if(state) {
            routingTable.add(new RoutingTableEntry(routerId, 0, routerId));
            for(Integer neighborRouterID: neighborRouterIDs) {
                if(NetworkLayerServer.routerMap.get(neighborRouterID).getState()) {
                    routingTable.add(new RoutingTableEntry(neighborRouterID, 1, neighborRouterID));
                } else {
                    routingTable.add(new RoutingTableEntry(neighborRouterID, Constants.INFINITY, -1));
                }
            }
        }
    }

    private void clearRoutingTable() {
        routingTable.clear();
    }

    public boolean updateRoutingTable(Router neighbor) {
        boolean hasUpdated=false, isNotFound=true;
        for(int i=0; i<neighbor.routingTable.size(); i++) {
            for(int j=0; j<routingTable.size(); j++) {
                if(neighbor.routingTable.get(i).getRouterId() == routingTable.get(j).getRouterId()) {
                    if(neighbor.routingTable.get(i).getDistance()+1 < routingTable.get(j).getDistance()) {
                        /* Distance Vector Routing Update Rule-2 (Lower Cost) */
                        routingTable.get(j).setDistance(neighbor.routingTable.get(i).getDistance()+1);
                        routingTable.get(j).setGatewayRouterId(neighbor.routerId);
                        hasUpdated = true;
                    }
                    isNotFound = false;
                    break;
                }
            }
            if(isNotFound) {
                /* Distance Vector Routing Update Rule-1 (New Destination) */
                if(neighbor.routingTable.get(i).getDistance() == Constants.INFINITY) {
                    routingTable.add(new RoutingTableEntry(neighbor.routingTable.get(i).getRouterId(), Constants.INFINITY, -1));
                } else {
                    routingTable.add(new RoutingTableEntry(neighbor.routingTable.get(i).getRouterId(), neighbor.routingTable.get(i).getDistance()+1, neighbor.routerId));
                }
                hasUpdated = true;
            }
            isNotFound = true;
        }
        return hasUpdated;
    }

    public boolean sfUpdateRoutingTable(Router neighbor) {
        boolean hasUpdated=false, isNotFound=true;
        for(int i=0; i<neighbor.routingTable.size(); i++) {
            for(int j=0; j<routingTable.size(); j++) {
                if(neighbor.routingTable.get(i).getRouterId() == routingTable.get(j).getRouterId()) {
                    if(neighbor.routingTable.get(i).getDistance()+1<routingTable.get(j).getDistance() && neighbor.routingTable.get(i).getGatewayRouterId()!=routerId) {
                        /* Distance Vector Routing Update Rule-2 (Lower Cost) & Split Horizon */
                        routingTable.get(j).setDistance(neighbor.routingTable.get(i).getDistance()+1);
                        routingTable.get(j).setGatewayRouterId(neighbor.routerId);
                        hasUpdated = true;
                    }
                    if(neighbor.routingTable.get(i).getDistance()+1>routingTable.get(j).getDistance() && neighbor.routerId==routingTable.get(j).getGatewayRouterId() && routingTable.get(j).getDistance()<Constants.INFINITY) {
                        /* Distance Vector Routing Update Rule-3 (Next-Hop Increase aka Forced Update) */
                        if(neighbor.routingTable.get(i).getDistance()==Constants.INFINITY && neighbor.routingTable.get(i).getGatewayRouterId()==-1) {
                            routingTable.get(j).setDistance(Constants.INFINITY);
                            routingTable.get(j).setGatewayRouterId(-1);
                        } else {
                            routingTable.get(j).setDistance(neighbor.routingTable.get(i).getDistance()+1);
                        }
                        hasUpdated = true;
                    }
                    isNotFound = false;
                    break;
                }
            }
            if(isNotFound) {
                /* Distance Vector Routing Update Rule-1 (New Destination) */
                if(neighbor.routingTable.get(i).getDistance() == Constants.INFINITY) {
                    routingTable.add(new RoutingTableEntry(neighbor.routingTable.get(i).getRouterId(), Constants.INFINITY, -1));
                } else {
                    routingTable.add(new RoutingTableEntry(neighbor.routingTable.get(i).getRouterId(), neighbor.routingTable.get(i).getDistance()+1, neighbor.routerId));
                }
                hasUpdated = true;
            }
            isNotFound = true;
        }
        return hasUpdated;
    }

    public void revertState() {
        state = !state;
        if(state) {
            initiateRoutingTable();
        } else {
            clearRoutingTable();
        }
    }

    public int getRouterId() {
        return routerId;
    }

    public ArrayList<IPAddress> getInterfaceAddresses() {
        return interfaceAddresses;
    }

    public ArrayList<RoutingTableEntry> getRoutingTable() {
        return routingTable;
    }

    public ArrayList<Integer> getNeighborRouterIDs() {
        return neighborRouterIDs;
    }

    public boolean getState() {
        return state;
    }

    public Map<Integer, IPAddress> getGatewayIDtoIP() {
        return gatewayIDtoIP;
    }

    public String toStringRoutingTable() {
        StringBuilder stringBuilder = new StringBuilder("");
        stringBuilder.append("routerID: ").append(routerId).append("\ndestID distance next-hop\n");
        for(RoutingTableEntry routingTableEntry: routingTable) {
            stringBuilder.append(routingTableEntry.getRouterId()).append(" ").append(routingTableEntry.getDistance()).append(" ").append(routingTableEntry.getGatewayRouterId()).append("\n");
        }
        stringBuilder.append("-----------------------\n");
        return stringBuilder.toString();
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("");
        stringBuilder.append("routerID: ").append(routerId).append(" (").append((state? "UP": "DOWN")).append(")\ninterfaces: ");
        for(IPAddress interfaceAddress: interfaceAddresses) {
            stringBuilder.append(interfaceAddress.getIPAddress()).append("\t");
        }
        stringBuilder.append("\nneighbors: ");
        for(Integer neighborRouterID: neighborRouterIDs) {
            stringBuilder.append(neighborRouterID).append("\t");
        }
        return stringBuilder.toString();
    }
}
