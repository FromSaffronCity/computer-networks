package thread;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Date;

public class DateThread implements Runnable {
    private Socket socket;
    private Thread thread;
    private ObjectOutputStream oos;

    public DateThread(Socket socket) {
        this.socket = socket;
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        /* status */
        System.out.println("\n"+"SERVER: local IP address - "+socket.getLocalAddress());
        System.out.println("SERVER: local port - "+socket.getLocalPort());
        System.out.println("SERVER: remote IP address - "+socket.getInetAddress());
        System.out.println("SERVER: remote port - "+socket.getPort()+"\n");

        try {
            /* output buffer */
            oos = new ObjectOutputStream(socket.getOutputStream());

            /* writing to client */
            while(true) {
                Thread.sleep(10000);
                oos.writeObject(new Date());  // Date is serialized
            }
        } catch(IOException|InterruptedException e) {
            e.printStackTrace();
        } finally {
            /* do nothing */
        }

        /* closing stream */
        try {
            oos.close();
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            /* do nothing */
        }

        return ;
    }
}
