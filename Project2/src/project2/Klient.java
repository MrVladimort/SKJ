package project2;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Klient {
    private DatagramSocket serverSocket;
    private InetAddress address;
    private int kwant, zegar, port;

    public static void main(String[] args) throws UnknownHostException {
        new Klient(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
    }

    private void log(String info) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        System.out.println("[" + dtf.format(now) + "]" + " Klient " + this.address + ":" + this.port + " = " + info);
    }

    private void setThreads() {
        Thread zegarThread = new Thread(this::timeIsMoney);
        zegarThread.start();

        Thread serverThread = new Thread(this::server404);
        serverThread.start();
    }

    private void timeIsMoney() {
        int tmp = 0;
        while (true) {
            if (tmp == this.kwant) {
                synchronize();
                tmp = 0;
            }
            tmp++;
            this.zegar++;
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public Klient(int zegar, int kwant) throws UnknownHostException {
        this.zegar = zegar;
        this.kwant = kwant * 1000;
        this.port = 8080;
        this.address = InetAddress.getByName("localhost");

        setThreads();
    }

    private void server404() {
        try {
            log("Server is running");
            this.serverSocket = new DatagramSocket(this.port);
            byte[] receiveData = new byte[256];

            while (true) {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);

                byte[] buffer = receivePacket.getData();

                log(receivePacket.getSocketAddress() + " " + new String(buffer, StandardCharsets.UTF_8));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<InetAddress> listAllBroadcastAddresses() {
        try {
            List<InetAddress> broadcastList = new ArrayList<>();
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();

                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }

                networkInterface.getInterfaceAddresses().stream()
                        .map(InterfaceAddress::getBroadcast)
                        .filter(Objects::nonNull)
                        .forEach(broadcastList::add);
            }
            return broadcastList;
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    private void synchronize() {
        log("Synchronize on time " + this.zegar + " and kwant " + this.kwant);
        List<InetAddress> broadcastList = listAllBroadcastAddresses();
        log(broadcastList.toString());
    }
}
