package thread;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

public class Client2 {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Socket socket = new Socket("127.0.0.1", 6666);  // server is local machine, that is localhost
        /*
            after this line is executed, a client socket is created and client is connected to server.
            an arbitrary port number is assigned to client.
        */
        System.out.println("CLIENT2: connected");

        /* status */
        System.out.println("\n"+"CLIENT2: local IP address - "+socket.getLocalAddress());
        System.out.println("CLIENT2: local port - "+socket.getLocalPort());
        System.out.println("CLIENT2: remote IP address - "+socket.getInetAddress());
        System.out.println("CLIENT2: remote port - "+socket.getPort()+"\n");

        /* input buffer */
        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

        /* receiving message from server */
        while(true) {
            System.out.println("CLIENT2: "+ois.readObject());  // readObject() throws ClassNotFoundException
        }
    }
}
