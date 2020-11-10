package utilities;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.net.Socket;

public class NetworkUtility {
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;
    private Socket socket;

    public NetworkUtility(String host, int port) {
        try {
            socket = new Socket(host, port);
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectInputStream = new ObjectInputStream(socket.getInputStream());
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public NetworkUtility(Socket socket) {
        try {
            this.socket = socket;
            objectOutputStream = new ObjectOutputStream(this.socket.getOutputStream());
            objectInputStream = new ObjectInputStream(this.socket.getInputStream());
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void write(Object object) {
        if(object != null) {
            try {
                objectOutputStream.writeObject(object);
                objectOutputStream.flush();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Object read() {
        Object object = null;
        try {
            object = objectInputStream.readObject();
        } catch(IOException|ClassNotFoundException e) {
            e.printStackTrace();
        }
        return object;
    }

    public void closeConnection() {
        try {
            objectOutputStream.close();
            objectInputStream.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("");
        stringBuilder.append("local address: ").append(socket.getLocalAddress().toString());
        stringBuilder.append("inet address: ").append(socket.getInetAddress().toString());
        stringBuilder.append("remote socket address: ").append(socket.getRemoteSocketAddress().toString());
        stringBuilder.append("local socket address: ").append(socket.getLocalSocketAddress().toString());
        return stringBuilder.toString();
    }
}
