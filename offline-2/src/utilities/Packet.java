package utilities;

import java.io.Serializable;

public class Packet implements Serializable {
    private String message;
    private String specialMessage;
    private IPAddress sourceIP;
    private IPAddress destinationIP;
    private int hopCount;

    public Packet(String message, String specialMessage, IPAddress sourceIP, IPAddress destinationIP) {
        this.message = message;
        this.specialMessage = specialMessage;
        this.sourceIP = sourceIP;
        this.destinationIP = destinationIP;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setSpecialMessage(String specialMessage) {
        this.specialMessage = specialMessage;
    }

    public String getSpecialMessage() {
        return specialMessage;
    }

    public void setSourceIP(IPAddress sourceIP) {
        this.sourceIP = sourceIP;
    }

    public IPAddress getSourceIP() {
        return sourceIP;
    }

    public void setDestinationIP(IPAddress destinationIP) {
        this.destinationIP = destinationIP;
    }

    public IPAddress getDestinationIP() {
        return destinationIP;
    }

    public void setHopCount(int hopCount) {
        this.hopCount = hopCount;
    }

    public int getHopCount() {
        return hopCount;
    }
}
