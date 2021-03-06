package project2;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Klient {
    private int kwant, zegar, port;
    private String seconds;
    private InetAddress address;
    private List<Integer> synList;

    public static void main(String[] args) throws IOException {
        new Klient(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
    }

    private void log(String info) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        System.out.println("[" + dtf.format(now) + "]" + " Klient " + this.address + " = " + info);
    }

    private void setThreads() {
        Thread zegarThread = new Thread(this::timeIsMoney);
        zegarThread.start();

        Thread serverThread = new Thread(this::server404);
        serverThread.start();
    }

    private void timeIsMoney() {
        try {
            int tmp = 0;
            while (true) {
                if (tmp == this.kwant) {
                    startSynchronize();
                    tmp = 0;
                }
                tmp++;
                this.zegar++;
                Thread.sleep(1);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Klient(int port, int zegar, int kwant) throws IOException {
        this.zegar = zegar;
        this.kwant = kwant * 1000;
        this.port = port;
        this.address = InetAddress.getLocalHost();
        this.synList = new ArrayList<>();
        this.seconds = "" + kwant + 's';

        setThreads();
    }

    private void server404() {
        try {
            log("Server is running");
            DatagramSocket serverSocket = new DatagramSocket(this.port, this.address);
            byte[] receiveData = new byte[256];

            while (true) {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);
                InetAddress senderAddress = receivePacket.getAddress();
                byte[] buffer = receivePacket.getData();
                String msg = new String(buffer, StandardCharsets.UTF_8).trim().toLowerCase();
                String[] commWithArg = msg.split(" ");
                String comm = commWithArg[0].trim();
                String arg1 = "";

                if (!msg.equals("clk"))
                    log(senderAddress + ":" + receivePacket.getPort() + " " + msg);

                if (commWithArg.length >= 2)
                    arg1 = commWithArg[1].trim();

                switch (comm) {
                    case "clk":
                        for (int i = 8080; i < 9000; i++) if (i != this.port) sendDataToSocket("syn " + this.zegar, senderAddress, i);
                        break;
                    case "syn":
                        this.synList.add(Integer.parseInt(arg1));
                        break;
                    case "get":
                        if (arg1.equalsIgnoreCase("counter"))
                            sendDataToSocket("" + this.zegar, senderAddress, 9000);
                        else if (arg1.equalsIgnoreCase("period"))
                            sendDataToSocket("" + (this.kwant / 1000), senderAddress, 9000);
                        break;
                    case "set":
                        if (arg1.equalsIgnoreCase("counter")) {
                            this.zegar = Integer.parseInt(commWithArg[2]);
                            sendDataToSocket("New counter " + this.zegar + " OK", senderAddress, 9000);
                        } else if (arg1.equalsIgnoreCase("period")) {
                            this.kwant = Integer.parseInt(commWithArg[2]) * 1000;
                            this.seconds = commWithArg[2] + "s";
                            sendDataToSocket("New period " + this.kwant / 1000 + " OK", senderAddress, 9000);
                        }
                        break;
                    case "die":
                        sendDataToSocket("OK", senderAddress, 9000);
                        for (InetAddress inetAddress : listAllBroadcastAddresses())
                            for (int i = 8080; i < 9000; i++) if (i != this.port) sendDataToSocket(this.address + ": I died with honor! Keep working guys", inetAddress, i);
                        System.exit(0);
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startSynchronize() throws IOException {
        log("Synchronize on time " + this.zegar + " and kwant " + this.seconds);
        for (InetAddress broadcast : listAllBroadcastAddresses())
            for (int i = 8080; i < 9000; i++) if (i != this.port) sendDataToSocket("clk", broadcast, i);
        synchronize();
    }

    private void synchronize() {
        Thread thread = new Thread(() -> {
            try {
                this.synList = new ArrayList<>();
                this.synList.add(this.zegar);
                int newZegar = 0;
                Thread.sleep(1000);
                for (int syn : this.synList) newZegar += syn;
                int size = this.synList.size();
                if (size != 0) this.zegar = newZegar / size;
                log("New zegar " + this.zegar);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        thread.run();
    }

    private void sendDataToSocket(String msg, InetAddress address, int port) throws IOException {
        DatagramSocket clientSocket = new DatagramSocket();
        byte[] all = msg.getBytes();
        int length = 0;
        while (all.length > length) {
            byte[] sendData = new byte[256];
            for (int i = 0; i < sendData.length && all.length > length; i++) sendData[i] = all[length++];
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, address, port);
            clientSocket.send(sendPacket);
        }
        clientSocket.close();
    }

    private List<InetAddress> listAllBroadcastAddresses() {
        try {
            List<InetAddress> broadcastList = new ArrayList<>();
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isLoopback() || !networkInterface.isUp())
                    continue;
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
}
