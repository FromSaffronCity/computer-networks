package utilities;

public class RoutingTableEntry {
    private int routerId;
    private int distance;
    private int gatewayRouterId;

    public RoutingTableEntry(int routerId, int distance, int gatewayRouterId) {
        this.routerId = routerId;
        this.distance = distance;
        this.gatewayRouterId = gatewayRouterId;
    }

    public int getRouterId() {
        return routerId;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public int getDistance() {
        return distance;
    }

    public void setGatewayRouterId(int gatewayRouterId) {
        this.gatewayRouterId = gatewayRouterId;
    }

    public int getGatewayRouterId() {
        return gatewayRouterId;
    }
}
