package a2online;

import utility.NetworkUtility;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws IOException {
        NetworkUtility networkUtility = new NetworkUtility(new Socket("localhost", 6666));
        String myID = (String) networkUtility.read();
        System.out.println("CLIENT: I am client"+myID);

        Scanner scanner = new Scanner(System.in);

        while(true) {
            System.out.print("CLIENT: commend: ");
            String[] input = scanner.nextLine().split(" ");

            if(input[0].equalsIgnoreCase("GET")) {
                networkUtility.write(myID+" GET "+input[1]);
                System.out.println("CLIENT: client"+input[1]+" status: "+networkUtility.read());

            } else if(input[0].equalsIgnoreCase("SET")) {
                String status = "";

                for(int i=1; i<input.length; i++) {
                    status += input[i]+" ";
                }

                networkUtility.write(myID+" SET "+status);
                System.out.println("CLIENT: request sent to server");

            } else {
                System.out.println("CLIENT: wrong command");
            }
        }
    }
}
