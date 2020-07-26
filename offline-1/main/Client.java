package main;

import threads.ClientThread;

import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        /* taking file name and starting process */
        while(true) {
            new ClientThread(scanner.nextLine());
        }
    }
}

