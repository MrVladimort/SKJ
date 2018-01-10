package project2;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Kontroler {
    private DatagramSocket serverSocket = new DatagramSocket(9000, InetAddress.getLocalHost());

    private void log(String info) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        System.out.println("[" + dtf.format(now) + "]" + " Kontroler" + " = " + info);
    }

    public static void main(String [] args) throws IOException {
        if (args.length == 3)
            new Kontroler(args[0], Integer.parseInt(args[1]), args[2]);
        else if (args.length == 4)
            new Kontroler(args[0], Integer.parseInt(args[1]), args[3]);
        else if (args.length == 5)
            new Kontroler(args[0], Integer.parseInt(args[1]), args[3], Integer.parseInt(args[4]));
    }

    private Kontroler(String address, int clientPort, String need) throws IOException {
        log("Kontroler 1");

        if (need.equalsIgnoreCase("counter"))
            sendDataToSocket("get counter", InetAddress.getByName(address), clientPort);
        else if (need.equalsIgnoreCase("period"))
            sendDataToSocket("get period", InetAddress.getByName(address), clientPort);
        else if (need.equalsIgnoreCase("die"))
            sendDataToSocket("die", InetAddress.getByName(address), clientPort);
        else
            log("Unresolved command for get: " + need);

        Thread serverThread = new Thread(this::server404);
        serverThread.run();
    }

    private Kontroler(String address, int clientPort, String need, int value) throws IOException {
        log("Kontroler 2");

        if (need.equalsIgnoreCase("counter"))
            sendDataToSocket("set counter " + value, InetAddress.getByName(address), clientPort);
        else if (need.equalsIgnoreCase("period"))
            sendDataToSocket("set period " + value, InetAddress.getByName(address), clientPort);
        else
            log("Unresolved command for set: " + need);

        Thread serverThread = new Thread(this::server404);
        serverThread.run();
    }

    private void server404() {
        try {
            log("Kontroler is waiting");

            byte[] receiveData = new byte[256];

            while (true) {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);

                byte[] buffer = receivePacket.getData();
                String msg = new String(buffer, StandardCharsets.UTF_8).trim().toLowerCase();
                System.out.println(msg);
                Thread.sleep(10000);
                System.exit(0);
            }
        } catch (IOException | InterruptedException e1) {
            e1.printStackTrace();
        }
    }

    private void sendDataToSocket(String msg, InetAddress address, int port) throws IOException {
        log("Send data: " + msg + " to " + address);

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
}
