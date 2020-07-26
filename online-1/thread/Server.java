package thread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(6666);  // NOTICE: port number for server

        while(true) {
            System.out.println("SERVER: waiting for connection response");

            Socket socket = serverSocket.accept();
            /*
                this is executed when a client program connects with server.
                socket for server is returned.
            */
            System.out.println("\n"+"SERVER: connected");

            /* open thread */
            new DateThread(socket);
        }
    }
}
