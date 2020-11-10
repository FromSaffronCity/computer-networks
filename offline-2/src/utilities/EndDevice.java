package utilities;

import java.io.Serializable;

public class EndDevice implements Serializable {
    private IPAddress ipAddress;
    private IPAddress gateway;
    private int deviceID;

    public EndDevice(IPAddress ipAddress, IPAddress gateway, int deviceID) {
        this.ipAddress = ipAddress;
        this.gateway = gateway;
        this.deviceID = deviceID;
    }

    public IPAddress getIPAddress() {
        return ipAddress;
    }

    public IPAddress getGateway() {
        return gateway;
    }

    public int getDeviceID() {
        return deviceID;
    }
}
