import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.lang.Thread;

class Agent {
    ArrayList<String> agents = new ArrayList<>();
    Address address;
    int zegar = 0;
    Thread serverThread, zegarThread;

    private void log(String info) {
        System.out.println("Agent " + this.address + ": " + info);
    }

    Agent(String ip, int port, int zegar) {
        this.address = new Address(ip, port);
        this.zegar = zegar;
        this.agents = agents;
        this.agents.add(this.address.toString());

        setThreads();
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
        try (ServerSocket serverSocket = new ServerSocket(this.address.port)) {
            Socket socket = serverSocket.accept();
            Address client = new Address(socket.getInetAddress().toString(), socket.getPort());
            log("Client connected on " + client);

            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());

            String msg = in.readLine();

            if (msg.equalsIgnoreCase("clk")) {
                out.writeUTF(this.zegar + "");
                out.flush();
                Thread.sleep(3000);
            } else if (msg.equalsIgnoreCase("net")) {
                for (String agent : this.agents)
                    out.writeUTF(agent);
                out.flush();
                Thread.sleep(3000);
            } else if (msg.equalsIgnoreCase("syn")) {
                maybeSynchronize();
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
            server404();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void maybeSynchronize() throws IOException {
        ArrayList<Integer> sync = new ArrayList<>();

        for (String agent : this.agents) {
            if (!agent.equalsIgnoreCase(this.address.toString())) {
                Address address = new Address(agent);
                Socket socket = new Socket(address.ip, address.port);

                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                DataInputStream in = new DataInputStream(socket.getInputStream());

                out.writeUTF("clk");
                sync.add(Integer.parseInt(in.readUTF()));
            }
        }
        int time = 0;
        for (Integer tmp : sync)
            time += tmp;
        this.zegar = time / sync.size();
    }
}
