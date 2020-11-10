package utilities;

import java.io.Serializable;

public class IPAddress implements Serializable {
    private String ipAddress;
    private short[] octaves;

    public IPAddress(String ipAddress) {
        this.ipAddress = ipAddress;
        String[] temp = ipAddress.split("\\.");
        octaves = new short[4];
        for(int i=0; i<4; i++) {
            octaves[i] = Short.parseShort(temp[i]);
        }
    }

    public short[] getOctaves() {
        return octaves;
    }

    public String getIPAddress() {
        return ipAddress;
    }

    @Override
    public String toString() {
        return ipAddress;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) {
            return true;
        }
        if(obj==null || !(obj instanceof IPAddress)) {
            return false;
        }
        IPAddress ipAddress = (IPAddress) obj;
        return this.ipAddress.equals(ipAddress.ipAddress);
    }

    @Override
    public int hashCode() {
        return ipAddress.hashCode();
    }
}
