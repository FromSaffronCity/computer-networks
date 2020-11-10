package mains;

import utilities.NetworkUtility;
import utilities.EndDevice;
import utilities.Packet;

import java.util.Random;

public class Client {
    public static void main(String[] args) throws InterruptedException {
        Random random = new Random();
        NetworkUtility networkUtility = new NetworkUtility("localhost", 4444);
        System.out.println("connected to server");

        /* receiving EndDevice configuration from ServerThread */
        EndDevice endDevice = (EndDevice) networkUtility.read();
        System.out.println("endDevice configuration received from server");

        /* preparing for sending&receiving packets to&from ServerThread */
        Packet packet;
        double averageHops = 0;
        int dropRate = 0;

        /* sending&receiving packets to&from ServerThread */
        for(int i=0, randomValue=random.nextInt(100); i<100; i++) {
            networkUtility.write(new Packet("message from client"+(endDevice.getDeviceID()+1), (i==randomValue? "SHOW_ROUTE": null), endDevice.getIPAddress(), null));
            packet = (Packet) networkUtility.read();

            System.out.println("packet"+i+": "+packet.getMessage());
            if(i == randomValue) {
                System.out.println("\n-----------------------\n"+packet.getSpecialMessage()+"\n-----------------------\n");
            }
            if(packet.getHopCount() == -1) {
                dropRate++;
            } else {
                averageHops += packet.getHopCount();
            }
            Thread.sleep(1000);
        }
        System.out.println(">> average number of hops: "+(dropRate==100? 0: averageHops/(100-dropRate)));
        System.out.println(">> drop rate: "+dropRate+" packets dropped among 100 packets");

        /* confirming ServerThread about end of operation */
        networkUtility.write(new Packet(null, "END", null, null));
        networkUtility.closeConnection();
    }
}
