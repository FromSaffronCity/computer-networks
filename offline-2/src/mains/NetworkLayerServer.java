package mains;

import utilities.EndDevice;
import utilities.IPAddress;
import utilities.NetworkUtility;
import utilities.Router;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Random;
import java.util.Vector;

public class NetworkLayerServer {
    static int clientCount = 0;
    static ArrayList<Router> routers = new ArrayList<>();
    private static Map<IPAddress, Integer> clientInterfaces = new HashMap<>();
    static Map<IPAddress, EndDevice> endDeviceMap = new HashMap<>();
    private static ArrayList<EndDevice> endDevices = new ArrayList<>();
    static Map<Integer, Integer> deviceIDtoRouterID = new HashMap<>();
    private static Map<IPAddress, Integer> interfaceToRouterID = new HashMap<>();
    public static Map<Integer, Router> routerMap = new HashMap<>();
    static boolean isDVR = true;  // false: simpleDVR(); true: DVR();

    private static void readTopology() throws FileNotFoundException {
        Scanner scanner = new Scanner(new File("D:\\Academic 3-2\\CSE-322 (Computer Networks sessional)\\offline\\offline-2\\offline2-src\\topology-file\\topology.txt"));

        /* skipping first 27 lines of topology.txt */
        for(int i=0; i<27; i++) {
            scanner.nextLine();
        }

        /* reading contents from topology.txt */
        while(scanner.hasNext()) {
            /* reading space between two routers */
            scanner.nextLine();

            /* starting reading contents for a router */
            ArrayList<IPAddress> interfaceAddresses = new ArrayList<>();
            ArrayList<Integer> neighborRouterIDs = new ArrayList<>();
            Map<Integer, IPAddress> gatewayIDtoIP = new HashMap<>();

            int routerId=scanner.nextInt(), count=scanner.nextInt();
            for(int i=0; i<count; i++) {
                neighborRouterIDs.add(scanner.nextInt());
            }
            count = scanner.nextInt();

            /* NOTICE: for reading newline between integer and string inputs */
            scanner.nextLine();

            /* continuing reading contents and creating new router */
            for(int i=0; i<count; i++) {
                IPAddress ipAddress = new IPAddress(scanner.nextLine());
                interfaceAddresses.add(ipAddress);
                interfaceToRouterID.put(ipAddress, routerId);

                /* considering first interface to be always client interface */
                if(i == 0) {
                    clientInterfaces.put(ipAddress, 0);
                } else {
                    gatewayIDtoIP.put(neighborRouterIDs.get(i - 1), ipAddress);
                }
            }
            Router router = new Router(routerId, interfaceAddresses, neighborRouterIDs, gatewayIDtoIP);
            routers.add(router);
            routerMap.put(routerId, router);
        }
    }

    private static void printRouters() {
        for(Router router: routers) {
            System.out.println("------------------\n"+router);
        }
        System.out.println("------------------\n");
    }

    private static void initRoutingTables() {
        for(Router router: routers) {
            router.initiateRoutingTable();
        }
    }

    public static synchronized void DVR(int startingRouterId) {
        boolean convergence=true, hasUpdated=false;
        while(convergence) {
            if(routerMap.get(startingRouterId).getState()) {
                for(Integer neighborRouterID: routerMap.get(startingRouterId).getNeighborRouterIDs()) {
                    if(routerMap.get(neighborRouterID).getState()) {
                        hasUpdated = (hasUpdated || routerMap.get(neighborRouterID).sfUpdateRoutingTable(routerMap.get(startingRouterId)));
                    }
                }
            }
            for(Router router: routers) {
                if(router.getState() && router.getRouterId()!=startingRouterId) {
                    for(Integer neighborRouterID: router.getNeighborRouterIDs()) {
                        if(routerMap.get(neighborRouterID).getState()) {
                            hasUpdated = (hasUpdated || routerMap.get(neighborRouterID).sfUpdateRoutingTable(router));
                        }
                    }
                }
            }
            if(!hasUpdated) {
                convergence = false;
            }
            hasUpdated = false;
        }
    }

    public static synchronized void simpleDVR(int startingRouterId) {
        boolean convergence=true, hasUpdated=false;
        while(convergence) {
            if(routerMap.get(startingRouterId).getState()) {
                for(Integer neighborRouterID: routerMap.get(startingRouterId).getNeighborRouterIDs()) {
                    if(routerMap.get(neighborRouterID).getState()) {
                        hasUpdated = (hasUpdated || routerMap.get(neighborRouterID).updateRoutingTable(routerMap.get(startingRouterId)));
                    }
                }
            }
            for(Router router: routers) {
                if(router.getState() && router.getRouterId()!=startingRouterId) {
                    for(Integer neighborRouterID: router.getNeighborRouterIDs()) {
                        if(routerMap.get(neighborRouterID).getState()) {
                            hasUpdated = (hasUpdated || routerMap.get(neighborRouterID).updateRoutingTable(router));
                        }
                    }
                }
            }
            if(!hasUpdated) {
                convergence = false;
            }
            hasUpdated = false;
        }
    }

    private static String toStringRoutingTables() {
        StringBuilder stringBuilder = new StringBuilder("");
        for(Router router: routers) {
            stringBuilder.append("\n------------------\n").append(router.toStringRoutingTable());
        }
        stringBuilder.append("\n");
        return stringBuilder.toString();
    }

    private static EndDevice getClientDeviceSetup() {
        Random random = new Random();
        int randomValue=random.nextInt(clientInterfaces.size()), hitValue=0;
        IPAddress ipAddress=null, gateway=null;

        for(Map.Entry<IPAddress, Integer> mapEntry: clientInterfaces.entrySet()) {
            if(hitValue == randomValue) {
                gateway = mapEntry.getKey();
                ipAddress = new IPAddress(gateway.getOctaves()[0]+"."+gateway.getOctaves()[1]+"."+gateway.getOctaves()[2]+"."+(mapEntry.getValue()+2));
                clientInterfaces.put(mapEntry.getKey(), mapEntry.getValue()+1);
                deviceIDtoRouterID.put(endDevices.size(), interfaceToRouterID.get(gateway));
                break;
            } else {
                hitValue++;
            }
        }
        System.out.println("endDevice: "+ipAddress+"::::"+gateway);
        return new EndDevice(ipAddress, gateway, endDevices.size());
    }

    public static void main(String[] args) throws IOException {
        /* creating activeClientList for further usage */
        Vector<IPAddress> activeClientList = new Vector<>();

        /* starting serverSocket */
        ServerSocket serverSocket = new ServerSocket(4444);
        System.out.println("server ready: "+serverSocket.getInetAddress().getHostAddress());

        /* creating router topology and initiating routingTables */
        System.out.println("creating router topology");
        readTopology();
        printRouters();
        initRoutingTables();

        /* updating routing tables using distance vector routing until convergence */
        if(isDVR) {
            DVR(1);
        } else {
            simpleDVR(1);
        }

        /* starting a new thread which turns on/off routers randomly depending on interfaces.Constants.LAMBDA */
        new RouterStateChanger();

        /* accepting new clients for packet passing */
        while(true) {
            Socket socket = serverSocket.accept();
            System.out.println("client"+(++clientCount)+" attempted to connect");
            EndDevice endDevice = getClientDeviceSetup();
            endDevices.add(endDevice);
            endDeviceMap.put(endDevice.getIPAddress(), endDevice);
            new ServerThread(endDevice, activeClientList, new NetworkUtility(socket));
        }
    }
}
