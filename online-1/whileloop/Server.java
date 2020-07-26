package whileloop;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        ServerSocket serverSocket = new ServerSocket(6666);  // NOTICE: port number for server

        while(true) {
            System.out.println("SERVER: waiting for connection response");

            Socket socket = serverSocket.accept();
            /*
                this is executed when a client program connects with server.
                socket for server is returned.
            */
            System.out.println("\n"+"SERVER: connected");

            /* status */
            System.out.println("\n"+"SERVER: local IP address - "+socket.getLocalAddress());
            System.out.println("SERVER: local port - "+socket.getLocalPort());
            System.out.println("SERVER: remote IP address - "+socket.getInetAddress());
            System.out.println("SERVER: remote port - "+socket.getPort()+"\n");

            /* i/o buffer */
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());  // NOTICE: order for initialization
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

            /* sending message to client */
            oos.writeObject("SERVER: fuck you");

            /* receiving message from client */
            System.out.println(ois.readObject()+"\n");  // readObject() throws ClassNotFoundException
        }

        // return ; - dead/unreachable code
    }
}
