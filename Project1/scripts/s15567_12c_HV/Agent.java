import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.lang.Thread;
import java.util.Scanner;

class Agent {
    ArrayList<String> agents = new ArrayList<>();
    ServerSocket serverSocket;
    Address address;
    int zegar = 0;
    Thread serverThread, zegarThread;

    private void log(String info) {
        System.out.println("Agent " + this.address + ": " + info);
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

    public static void main(String[]args){
        Agent agent;
        if (args.length == 1)
            agent = new Agent("localhost", Integer.parseInt(args[0]), 0);
        else
            agent = new Agent("localhost", Integer.parseInt(args[0]), Integer.parseInt(args[1]), 0);
    }

    Agent(String ip, int port, int zegar) {
        this.address = new Address(ip, port);
        this.zegar = zegar;

        try {
            this.serverSocket = new ServerSocket(this.address.port);
            log("Server is running");
            notifyAgents();
            this.agents.add(this.address.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        setThreads();
    }

    Agent(String ip, int port, int parentPort, int zegar) {
        this.address = new Address(ip, port);
        this.zegar = zegar;

        try {
            this.serverSocket = new ServerSocket(this.address.port);
            log("Server is running");
            getParentAgents(parentPort);
            notifyAgents();
            maybeSynchronize();
            synchronizeAll();
        } catch (IOException e) {
            e.printStackTrace();
        }

        setThreads();
    }

    private void synchronizeAll() throws IOException {
        for (String agent : this.agents) {
            if (!agent.equalsIgnoreCase(this.address.toString())) {
                Address address = new Address(agent);
                Socket socket = new Socket(address.ip, address.port);

                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                write(out, "syn");
                socket.close();
            }
        }
    }

    private void notifyAgents() throws IOException {
        Socket monitorSocket = new Socket("localhost", 8080);
        BufferedWriter outMonitor = new BufferedWriter(new OutputStreamWriter(monitorSocket.getOutputStream()));
        write(outMonitor,"agn " + this.address);
        monitorSocket.close();

        for (String agent : this.agents){
            Address agentAddress = new Address(agent);
            Socket agentSocket = new Socket(agentAddress.ip, agentAddress.port);
            BufferedWriter outAgent = new BufferedWriter(new OutputStreamWriter(agentSocket.getOutputStream()));

            write(outAgent,"agn " + this.address);
            agentSocket.close();
        }
    }

    private void getParentAgents(int parentPort) throws IOException {
        Socket parentSocket = new Socket("localhost", parentPort);
        BufferedWriter outParent = new BufferedWriter(new OutputStreamWriter(parentSocket.getOutputStream()));
        BufferedReader inParent = new BufferedReader(new InputStreamReader(parentSocket.getInputStream(), "UTF-8"));
        write(outParent, "net");
        String s;
        while (true){
            s = inParent.readLine();
            if (s != null)
                this.agents.add(s);
            else
                break;
        }
        parentSocket.close();
    }

    private void setThreads() {
        this.zegarThread = new Thread(this::timeIsMoney);
        this.zegarThread.start();

        this.serverThread = new Thread(this::server404);
        this.serverThread.start();
    }

    private void timeIsMoney() {
//        log("milliseconds " + this.zegar);
        this.zegar += 1000;
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        timeIsMoney();
    }

    private void server404() {
        try {
            Socket socket = this.serverSocket.accept();
            Address client = new Address(socket.getInetAddress().toString(), socket.getPort());
            log("Client connected on " + client);

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            String [] commWithArguments = in.readLine().split(" ", 3);
            String msg = commWithArguments[0];
            log("Request " + commWithArguments[0]);

            switch (msg){
                case "clk":
                    write(out, this.zegar + "");
                    log("clk request 200");
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
                case "syn":
                    maybeSynchronize();
                    log("syn request 200");
                    break;
                default:
                    log(client + "bad request " + msg + ", connections suicide ...");
            }

            log("Closing connection & channels for " + client);

            socket.close();
            server404();
        } catch (IOException | NullPointerException e) {
            server404();
        }
    }

    private void maybeSynchronize() throws IOException {
        ArrayList<Integer> sync = new ArrayList<>();

        for (String agent : this.agents) {
            if (!agent.equalsIgnoreCase(this.address.toString())) {
                Address address = new Address(agent);
                Socket socket = new Socket(address.ip, address.port);

                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                write(out, "clk");
                sync.add(Integer.parseInt(in.readLine()));
                socket.close();
            }
        }
        int time = 0;
        for (Integer tmp : sync)
            time += tmp;
        if (time != 0 || sync.size() != 0)
            this.zegar = time / sync.size();
    }
}
