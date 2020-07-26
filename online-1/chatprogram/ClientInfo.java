package chatprogram;

import java.io.Serializable;

public class ClientInfo implements Serializable {
    private String name;
    private String password;

    public ClientInfo(String name, String password) {
        this.name = name;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }
}
