package chatprogram;

import utility.NetworkUtility;

public class ClientUtil {
    private String password;
    private NetworkUtility networkUtility;

    public ClientUtil(String password, NetworkUtility networkUtility) {
        this.password = password;
        this.networkUtility = networkUtility;
    }

    public String getPassword() {
        return password;
    }

    public NetworkUtility getNetworkUtility() {
        return networkUtility;
    }
}
