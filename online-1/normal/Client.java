package normal;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Socket socket = new Socket("127.0.0.1", 6666);  // server is local machine, that is localhost
        /*
            after this line is executed, a client socket is created and client is connected to server.
            an arbitrary port number is assigned to client.
        */
        System.out.println("CLIENT: connected");

        /* status */
        System.out.println("\n"+"CLIENT: local IP address - "+socket.getLocalAddress());
        System.out.println("CLIENT: local port - "+socket.getLocalPort());
        System.out.println("CLIENT: remote IP address - "+socket.getInetAddress());
        System.out.println("CLIENT: remote port - "+socket.getPort());

        /* i/o buffer */
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());  // NOTICE: order for initialization
        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

        /* sending message to server */
        oos.writeObject("CLIENT: যে বলে সে হয়");

        /* receiving message from server */
        System.out.println("\n"+ois.readObject());  // readObject() throws ClassNotFoundException

        return ;
    }
}
