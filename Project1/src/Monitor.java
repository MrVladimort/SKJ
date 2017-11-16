import java.io.*;
import java.net.*;
import java.util.*;

public class Monitor {
    Address address;
    List<String> agents = new ArrayList<>();
    Thread serverThread;

    public static void main(String[]args){
        Monitor monitor = new Monitor(8080);
    }

    private static void log(String info) {
        System.out.println("Monitor: " + info);
    }

    public Monitor(int port){
        this.address.port = port;
        this.address.ip = "localhost";
        this.serverThread = new Thread(this::server500);
        this.serverThread.start();
    }

    private void server500() {
        try (ServerSocket serverSocket = new ServerSocket(this.address.port)) {
            Socket socket = serverSocket.accept();
            Address client = new Address(socket.getInetAddress().toString(), socket.getPort());
            log("Client connected on " + client);

            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());

            String msg = in.readLine();

            if (msg.equalsIgnoreCase("clk")) {
                out.flush();
                Thread.sleep(3000);
            } else if (msg.equalsIgnoreCase("net")) {
                for (String agent : this.agents)
                    out.writeUTF(agent);
                out.flush();
                Thread.sleep(3000);
            } else if (msg.equalsIgnoreCase("syn")) {
                Thread.sleep(3000);
            } else {
                out.writeUTF("Invalid request - " + msg + '\n');
                log(client + " initialize connections suicide ...");
                out.writeUTF("Initialize connections suicide ..." + '\n');
                out.flush();
                Thread.sleep(5000);
            }


            log("Closing connections & channels for " + client);
            in.close();
            out.close();
            serverSocket.close();
            server500();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
