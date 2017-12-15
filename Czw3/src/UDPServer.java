import java.io.*;
import java.net.*;

class UDPServer {
    public static void main(String args[]) throws Exception {

        DatagramSocket serverSocket = new DatagramSocket(9876);

        byte[] receiveData = new byte[1024];

        while (true) {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);

            byte[] buffer = receivePacket.getData();

            FileOutputStream fos = new FileOutputStream(new File(System.getProperty("user.home") + "/newsmall.jpg"));
            fos.write(buffer);

            String ipAddress = receivePacket.getAddress().toString();
            int port = receivePacket.getPort();
            System.out.println("Address: " + ipAddress + ":" + Integer.toString(port) + " data: ");
        }
    }
}