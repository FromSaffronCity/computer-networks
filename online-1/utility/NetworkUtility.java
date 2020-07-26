package utility;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class NetworkUtility {
    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;

    public NetworkUtility(Socket socket) {
        this.socket = socket;

        try {
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void write(Object object) {
        if(object != null) {
            try {
                oos.writeObject(object);
            } catch(IOException e) {
                e.printStackTrace();
            }
        }

        return ;
    }

    public Object read() {
        Object object = null;

        try {
            object = ois.readObject();
        } catch(IOException|ClassNotFoundException e) {
            e.printStackTrace();
        }

        return object;
    }

    public void closeConnection() {
        try {
            oos.close();  // throw IOException
            ois.close();  // throw IOException
        } catch(IOException e) {
            e.printStackTrace();
        }

        return ;
    }
}
