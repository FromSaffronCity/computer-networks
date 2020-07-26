package main;

import threads.ServerThread;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;

public class HTTPFileServer {
    /* root working directory, log directory, and server port */
    private static final String ABSOLUTE_PATH_TO_ROOT = "D:\\Academic 3-2\\CSE-322 (Computer Networks sessional)\\offline\\offline-1\\source\\root";
    private static final String ABSOLUTE_PATH_TO_LOG = "D:\\Academic 3-2\\CSE-322 (Computer Networks sessional)\\offline\\offline-1\\source\\log directory";
    private static final int SERVER_PORT = 6789;

    public static void main(String[] args) throws IOException {
        /* deleting existing log directory */
        File logDirectory = new File(ABSOLUTE_PATH_TO_LOG);

        if(logDirectory.exists()) {
            String[] entries = logDirectory.list();

            for(String entry: entries){
                File toBeDeleted = new File(logDirectory.getPath(), entry);
                toBeDeleted.delete();
            }
            logDirectory.delete();
        }
        logDirectory.mkdir();

        /* starting listening on port 6789 */
        ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
        System.out.println(">> http file server waiting for connection on port no: "+SERVER_PORT+"\n");

        /* starting accepting http requests */
        while(true) {
            new ServerThread(serverSocket.accept(), ABSOLUTE_PATH_TO_ROOT, ABSOLUTE_PATH_TO_LOG, SERVER_PORT);
        }
    }
}
