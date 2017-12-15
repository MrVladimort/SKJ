package project1;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;

public class Agent {
    private LinkedHashSet<String> agents = new LinkedHashSet<>();
    private ServerSocket serverSocket;
    private KekAddress address;
    private int zegar = 0;

    public static void main(String[] args) {
        if (args.length == 2)
            new Agent("localhost", Integer.parseInt(args[0]), args[1], 0);
        else
            new Agent("localhost", Integer.parseInt(args[0]), args[1], args[2], 0);
    }

    private void log(String info) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        System.out.println("[" + dtf.format(now) + "]" + " Agent " + this.address + ": " + info);
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

    private Agent(String ip, int port, String monitor, int zegar) {
        this.address = new KekAddress(ip, port);
        this.zegar = zegar;
        this.agents.add(this.address.toString());
        try {
            this.serverSocket = new ServerSocket(this.address.port);
            setThreads();
            log("Server is running");
            notifyAgents(monitor);
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.agents.add(this.address.toString());
    }

    private Agent(String ip, int port, String parent, String monitor, int zegar) {
        this.address = new KekAddress(ip, port);
        this.zegar = zegar;
        this.agents.add(this.address.toString());
        try {
            this.serverSocket = new ServerSocket(this.address.port);
            setThreads();
            log("Server is running");
            getParentAgents(parent);
            notifyAgents(monitor);
            maybeSynchronize();
            synchronizeAll();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void synchronizeAll() throws IOException {
        for (String agent : this.agents) {
            if (!agent.equalsIgnoreCase(this.address.toString())) {
                KekAddress address = new KekAddress(agent);
                Socket socket = new Socket(address.ip, address.port);

                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                write(out, "syn");
                socket.close();
            }
        }
    }

    private void notifyAgents(String monitor) throws IOException {
        KekAddress monitorAddress = new KekAddress(monitor);
        Socket monitorSocket = new Socket(monitorAddress.ip, monitorAddress.port);
        BufferedWriter outMonitor = new BufferedWriter(new OutputStreamWriter(monitorSocket.getOutputStream()));
        write(outMonitor, "agn " + this.address);
        monitorSocket.close();

        for (String agent : this.agents) {
            if (!agent.equalsIgnoreCase(this.address.toString())) {
                KekAddress agentAddress = new KekAddress(agent);
                Socket agentSocket = new Socket(agentAddress.ip, agentAddress.port);
                BufferedWriter outAgent = new BufferedWriter(new OutputStreamWriter(agentSocket.getOutputStream()));
                write(outAgent, "agn " + this.address);
                agentSocket.close();
            }
        }
    }

    private void getParentAgents(String parent) throws IOException {
        KekAddress address = new KekAddress(parent);
        Socket parentSocket = new Socket(address.ip, address.port);
        BufferedWriter outParent = new BufferedWriter(new OutputStreamWriter(parentSocket.getOutputStream()));
        BufferedReader inParent = new BufferedReader(new InputStreamReader(parentSocket.getInputStream(), "UTF-8"));
        write(outParent, "net");
        String s;
        while (true) {
            s = inParent.readLine();
            if (s != null)
                this.agents.add(s);
            else
                break;
        }
        parentSocket.close();
    }

    private void setThreads() {
        Thread zegarThread = new Thread(this::timeIsMoney);
        zegarThread.start();

        Thread serverThread = new Thread(this::server404);
        serverThread.start();
    }

    private void timeIsMoney() {
        while (true) {
            this.zegar++;
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void server404() {
        try {
            while (true) {
                Socket socket = this.serverSocket.accept();
                KekAddress client = new KekAddress(socket.getInetAddress().toString(), socket.getPort());
                log("Client connected on " + client);

                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                String[] commWithArguments = in.readLine().split(" ", 3);
                String msg = commWithArguments[0];
                log("Request " + commWithArguments[0]);

                switch (msg) {
                    case "clk":
                        write(out, this.zegar + "");
                        log("clk request 200");
                        break;
                    case "agn":
                        this.agents.add(commWithArguments[1]);
                        log("Added agent " + commWithArguments[1]);
                        break;
                    case "net":
                        for (String agent : this.agents)
                            write(out, agent);
                        log("net request 200");
                        break;
                    case "syn":
                        maybeSynchronize();
                        log("syn request 200");
                        break;
                    case "del":
                        this.agents.remove(commWithArguments[1]);
                        maybeSynchronize();
                        log("del request 200");
                        break;
                    case "die":
                        log("initializing exit ...");
                        socket.close();
                        this.serverSocket.close();
                        System.exit(0);
                    default:
                        log(client + "bad request " + msg + ", connections suicide ...");
                }

                log("Closing connection & channels for " + client);

                socket.close();
            }
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            server404();
        }
    }

    private void maybeSynchronize() {
        Thread thread = new Thread(() -> {
            try {
                ArrayList<Integer> sync = new ArrayList<>();
                for (String agent : this.agents) {
                    if (!agent.equalsIgnoreCase(this.address.toString())) {
                        KekAddress address = new KekAddress(agent);
                        Socket socket = new Socket(address.ip, address.port);

                        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                        write(out, "clk");
                        sync.add(Integer.parseInt(in.readLine()));
                        socket.close();
                        Thread.sleep(10);
                    }
                }
                int time = 0;
                for (Integer tmp : sync)
                    time += tmp;
                if (time != 0 || sync.size() != 0)
                    this.zegar = time / sync.size();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });

        thread.start();
    }
}
