package b1online;

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

            if(input[0].equalsIgnoreCase("inbox")) {
                networkUtility.write(myID);

                Integer number = (Integer) networkUtility.read();
                System.out.println("CLIENT: number of message: "+number);

                for(int i=0; i<number; i++) {
                    System.out.println(networkUtility.read());
                }

            } else if(input[0].equalsIgnoreCase("send")) {
                String temp = "";
                for(int i=2; i<input.length; i++) {
                    temp += input[i]+" ";
                }

                networkUtility.write(new Message(myID, input[1], temp));
                System.out.println("CLIENT: message sent");

            } else {
                System.out.println("CLIENT: wrong command");
            }
        }
    }
}

