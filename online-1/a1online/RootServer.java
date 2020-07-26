package a1online;

import utility.NetworkUtility;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Hashtable;

public class RootServer {
    public static void main(String[] args) throws IOException {
        Hashtable<String, String> list = new Hashtable<>();
        list.put("google.com", "172.217.9.238");
        list.put("wikipedia.org", "208.80.154.224");

        ServerSocket ss = new ServerSocket(50000);
        System.out.println(">> waiting for connection ...");
        NetworkUtility nu = new NetworkUtility(ss.accept());
        System.out.println(">> connected to local server");

        while(true) {
            Object o = nu.read();

            if(nu != null) {
                String domain = (String) o;
                System.out.println(">> request accepted");

                if(list.containsKey(domain)) {
                    nu.write(list.get(domain));
                } else {
                    nu.write("IP Address Not Found");
                }
            }
        }
    }
}
