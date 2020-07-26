package a1online;

import utility.NetworkUtility;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws IOException {
        NetworkUtility nu = new NetworkUtility(new Socket("localhost", 6666));
        System.out.println(">> connected to local server");
        Scanner scanner = new Scanner(System.in);

        while(true) {
            nu.write(scanner.nextLine().toLowerCase());

            Object o = nu.read();

            if(o != null) {
                System.out.println((String) o);
            }
        }
    }
}
