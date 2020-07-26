package tcpdiff;

import java.io.Serializable;

public class Data implements Serializable {
    private String name;
    private String msg;

    public Data(String name, String msg) {
        this.name = name;
        this.msg = msg;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name+": "+msg;
    }
}
