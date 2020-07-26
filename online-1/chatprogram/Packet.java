package chatprogram;

import java.io.Serializable;

public class Packet implements Serializable {
    private String source;
    private String password;
    private String type;
    private String destination;
    private String message;

    public Packet(String source, String password, String type, String destination, String message) {
        this.source = source;
        this.password = password;
        this.type = type;
        this.destination = destination;
        this.message = message;
    }

    public String getSource() {
        return source;
    }

    public String getPassword() {
        return password;
    }

    public String getType() {
        return type;
    }

    public String getDestination() {
        return destination;
    }

    @Override
    public String toString() {
        return source+": "+message;
    }
}
