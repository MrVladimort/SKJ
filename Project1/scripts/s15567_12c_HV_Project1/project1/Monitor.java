package project1;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;

public class Monitor {
    private LinkedHashSet<String> agents = new LinkedHashSet<>();
    private ServerSocket serverSocket;

    public static void main(String[] args) {
        new Monitor(8080);
    }

    private static void log(String info) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        System.out.println("["+dtf.format(now)+"]" + " Monitor localhost:8080: " + info);
    }

    private static void write(BufferedWriter out, String msg) {
        try {
            out.write(msg);
            out.newLine();
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeHTTP(BufferedWriter out, String msg) throws IOException {
        String response = "HTTP/1.1 200 OK\r\n" +
                "Server: YarServer/2009-09-09\r\n" +
                "Content-Type: text/html\r\n" +
                "Content-Length: " + msg.length() + "\r\n" +
                "Connection: close\r\n\r\n";
        String result = response + msg;
        out.write(result);
        out.flush();
    }

    private Monitor(int port) {
        KekAddress address = new KekAddress("localhost", port);

        try {
            this.serverSocket = new ServerSocket(address.port);
            log("Server is running");
        } catch (IOException e) {
            e.printStackTrace();
        }

        Thread serverThread = new Thread(this::server500);
        serverThread.start();
    }

    private void server500() {
        try {
            while (true) {
                Socket socket = serverSocket.accept();
                KekAddress client = new KekAddress(socket.getInetAddress().toString(), socket.getPort());
                log("Client connected on " + client);

                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                String[] commWithArguments = in.readLine().split(" ");

                log("Request " + commWithArguments[0]);

                label:
                switch (commWithArguments[0].toLowerCase()) {
                    case "get":
                        if (commWithArguments[1].equals("/favicon.ico")) {
                            log("Say no to FAVICON!");
                            break;
                        } else {
                            String response = "<html><body>";
                            for (String agent : this.agents)
                                response += "<h1>" + agent + " " + getTime(agent) + "</h1>"
                                        + "<form action=\"/synchronize/" + agent + "\" method=\"post\">"
                                        + "<button>Synchronize</button></form>"
                                        + "<form action=\"/delete/" + agent + "\" method=\"post\">"
                                        + "<button>Delete</button></form>";
                            response += "</body></html>";
                            writeHTTP(out, response);
                            log("getHTTP request 200");
                            break;
                        }
                    case "post":
                        String[] whatToDo = commWithArguments[1].split("/");
                        log("post request with command " + whatToDo[1]);
                        switch (whatToDo[1]) {
                            case "synchronize":
                                sendRequest("syn", whatToDo[2]);
                                break;
                            case "delete":
                                sendRequest("die", whatToDo[2]);
                                this.agents.remove(whatToDo[2]);
                                for (String agent : this.agents)
                                    sendRequest("del " + whatToDo[2], agent);
                                break;
                            default:
                                break;
                        }
                        writeHTTP(out, "<html><body><h1>Command " + whatToDo[1]
                                + " for " + whatToDo[2] + " OK</h1></body></html>"
                                + "<form action=\"/\" method=\"get\">"
                                + "<button>Get Back</button></form>");
                        log("post request 200");
                        break;
                    case "syn":
                        sendRequest(commWithArguments[0], commWithArguments[1]);
                        log("syn request 200");
                        break;
                    case "die":
                        sendRequest(commWithArguments[0], commWithArguments[1]);
                        this.agents.remove(commWithArguments[1]);
                        log("die request 200");
                        break;
                    case "agn":
                        this.agents.add(commWithArguments[1]);
                        log("Added agent " + commWithArguments[1]);
                        log("agn request 200");
                        break;
                    case "net":
                        for (String agent : this.agents)
                            write(out, agent);
                        log("net request 200");
                        break;
                    default:
                        log(client + " bad request " + commWithArguments[0] + ", connections suicide ...");
                }

                log("Closing connection & channels for " + client);

                socket.close();
            }
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            server500();
        }
    }

    private void sendRequest(String request, String host) throws IOException {
        KekAddress address = new KekAddress(host);
        Socket requestSocket = new Socket(address.ip, address.port);
        BufferedWriter requestOut = new BufferedWriter(new OutputStreamWriter(requestSocket.getOutputStream()));
        write(requestOut, request);
    }

    private int getTime(String agent) {
        KekAddress address = new KekAddress(agent);
        try {
            Socket socket = new Socket(address.ip, address.port);

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            write(out, "clk");

            int res = Integer.parseInt(in.readLine());
            socket.close();

            return res;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
