import java.io.*;
import java.net.*;
import java.nio.file.Files;

class UDPClient {
    public static void main(String args[]) throws Exception {
        File file = new File(System.getProperty("user.home") + "/small.jpg");
        byte [] all = Files.readAllBytes(file.toPath());

        int length = 0;
        while (all.length > length) {
            byte[] sendData = new byte[1024];

            for (int i = 0; i < sendData.length && all.length > length; i++) {
                sendData[i] = all[length++];
            }
                System.out.println(length);

                DatagramSocket clientSocket = new DatagramSocket();
                InetAddress IPAddress = InetAddress.getByName("localhost");

                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9876);

                clientSocket.send(sendPacket);

                clientSocket.close();
            }
        }
    }
