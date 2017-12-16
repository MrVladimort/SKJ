package project2;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Klient {
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
                try {
                    startSynchronize();
                } catch (IOException e) {
                    e.printStackTrace();
                }
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

    private Klient(int zegar, int kwant) throws UnknownHostException {
        this.zegar = zegar;
        this.kwant = kwant * 1000;
        this.port = 8080;
        this.address = InetAddress.getByName("localhost");

        setThreads();
    }

    private void server404() {
        try {
            log("Server is running");
            DatagramSocket serverSocket = new DatagramSocket(this.port);
            byte[] receiveData = new byte[256];

            while (true) {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);

                byte[] buffer = receivePacket.getData();
                String msg = new String(buffer, StandardCharsets.UTF_8);
                InetAddress senderAddress = receivePacket.getAddress();

                switch (msg) {
                    case "clk":
                        sendDataToSocket("" + this.zegar, senderAddress);
                        break;
                    case "add":
                        break;
                    case "remove":
                        break;
                }

                log(senderAddress + " " + msg);
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

    private void startSynchronize() throws IOException {
        log("Synchronize on time " + this.zegar + " and kwant " + this.kwant);
        sendDataToBroadcast("clk");
    }

    private void synchronize() {

    }

    private void sendDataToBroadcast(String msg) throws IOException {
        DatagramSocket clientSocket = new DatagramSocket();
        byte[] all = msg.getBytes();

        for (InetAddress broadcast : listAllBroadcastAddresses()) {
            int length = 0;
            while (all.length > length) {
                byte[] sendData = new byte[256];

                for (int i = 0; i < sendData.length && all.length > length; i++) sendData[i] = all[length++];

                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcast, 8080);
                clientSocket.send(sendPacket);
            }
        }

        clientSocket.close();
    }

    private void sendDataToSocket(String msg, InetAddress address) throws IOException {
        DatagramSocket clientSocket = new DatagramSocket();
        byte[] all = msg.getBytes();
        int length = 0;
        while (all.length > length) {
            byte[] sendData = new byte[256];

            for (int i = 0; i < sendData.length && all.length > length; i++) sendData[i] = all[length++];

            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, address, 8080);
            clientSocket.send(sendPacket);
        }

        clientSocket.close();
    }
}
