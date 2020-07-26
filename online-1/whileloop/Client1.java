package whileloop;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client1 {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Socket socket = new Socket("127.0.0.1", 6666);  // server is local machine, that is localhost
        /*
            after this line is executed, a client socket is created and client is connected to server.
            an arbitrary port number is assigned to client.
        */
        System.out.println("CLIENT1: connected");

        /* status */
        System.out.println("\n"+"CLIENT1: local IP address - "+socket.getLocalAddress());
        System.out.println("CLIENT1: local port - "+socket.getLocalPort());
        System.out.println("CLIENT1: remote IP address - "+socket.getInetAddress());
        System.out.println("CLIENT1: remote port - "+socket.getPort()+"\n");

        /* i/o buffer */
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());  // NOTICE: order for initialization
        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

        /* sending message to server */
        oos.writeObject("CLIENT1: যে বলে সে হয়");

        /* receiving message from server */
        System.out.println((String) ois.readObject());  // readObject() throws ClassNotFoundException

        /* closing streams */
        oos.close();
        ois.close();

        /* closing socket */
        socket.close();

        return ;
    }
}
