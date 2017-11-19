import java.io.*;
import java.net.*;
import java.util.*;

public class Monitor {
    Address address;
    ArrayList<String> agents = new ArrayList<>();
    ServerSocket serverSocket;
    Thread serverThread;

    public static void main(String[]args){
        Monitor monitor = new Monitor(8080);
    }

    private static void log(String info) {
        System.out.println("Monitor localhost:8080: " + info);
    }

    private static void write(BufferedWriter out, String msg){
        try {
            out.write(msg);
            out.newLine();
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeHTTP(BufferedWriter out, String msg) throws IOException{
        String response = "HTTP/1.1 200 OK\r\n" +
                "Server: YarServer/2009-09-09\r\n" +
                "Content-Type: text/html\r\n" +
                "Content-Length: " + msg.length() + "\r\n" +
                "Connection: close\r\n\r\n";
        String result = response + msg;
        out.write(result);
        out.flush();
    }

    public Monitor(int port){
        this.address = new Address("localhost", port);

        try {
            this.serverSocket = new ServerSocket(this.address.port);
            log("Server is running");
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.serverThread = new Thread(this::server500);
        this.serverThread.start();
    }

    private void server500() {
        try {
            Socket socket = serverSocket.accept();
            Address client = new Address(socket.getInetAddress().toString(), socket.getPort());
            log("Client connected on " + client);

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            String [] commWithArguments = in.readLine().split(" ", 3);
            log("Request " + commWithArguments[0]);

            switch (commWithArguments[0].toLowerCase()){
                case "net":
                    for (String agent : this.agents)
                        write(out, agent);
                    System.out.println(this.agents);
                    log("net request 200");
                    break;
                case "get":
                    String response = "<html><body>";
                    for (String agent : this.agents)
                        response += "<h1>" + agent + " " + getTime(agent) + "</h1>";
                    response += "</body></html>";
                    writeHTTP(out, response);
                    log("getHTTP request 200");
                    break;
                case "agn":
                    this.agents.add(commWithArguments[1]);
                    log("Added agent " + commWithArguments[1]);
                    log("agn request 200");
                    break;
                case "syn":
                    Address address = new Address(commWithArguments[1]);
                    Socket requestSocket = new Socket(address.ip, address.port);
                    BufferedWriter requestOut = new BufferedWriter(new OutputStreamWriter(requestSocket.getOutputStream()));
                    write(requestOut, "syn");
                    log("syn request 200");
                    break;
                default:
                    log(client + " bad request " + commWithArguments[0] + ", connections suicide ...");
            }

            log("Closing connection & channels for " + client);

            socket.close();
            server500();
        } catch (IOException | NullPointerException e) {
            server500();
        }
    }

    private int getTime(String agent){
        Address address = new Address(agent);
        try {
            Socket socket = new Socket(address.ip, address.port);

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            write(out, "clk");

            int res = Integer.parseInt(in.readLine());
            socket.close();

            return  res;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
