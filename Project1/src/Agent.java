import java.net.*;
import java.util.*;
import java.io.*;
import java.lang.Thread.*;

public class Agent {
	String ip;
	int port;
	int licznik;
	ArrayList <Agent> dataAgent;
	Thread zegarThread;
	Thread serverThread;
	
	public Agent(ArrayList<Agent> dataAgent){
		int lastIndex = dataAgent.size();
		this.dataAgent = dataAgent;
		this.ip = dataAgent.get(lastIndex).ip;
		this.port = dataAgent.get(lastIndex).port;
		this.licznik = dataAgent.get(lastIndex).licznik;
		this.dataAgent.add(this);
		//this.ip = InetAddress.getLocalHostAddress(); тільки два конструктора
	}
	
	public Agent(String ip, int port, int licznik, ArrayList<Agent> dataAgent) {
		this.ip = ip;
		this.port = port;
		this.licznik = licznik;
		this.dataAgent = dataAgent;
		this.dataAgent.add(this);
	}
	

	private static void log(String text) {
		System.out.println("Agent: "  + text);	
	}
	
	public void increment() {
		try {
			licznik++;
			System.out.println(licznik);
			zegarThread.sleep(1000);
			increment();
		} catch(InterruptedException e) {
			e.printStackTrace();
		}		
	}
	
	private void setThreads() {
		this.serverThread = new Thread(() -> {
			try {
				ServerSocket serverSocket = new ServerSocket(this.port);
				Socket clientSocket = serverSocket.accept();			
				log("Agent ip, port :" 
						+ this.ip + "\n" 
						+ this.port);
					
				InputStream sis = clientSocket.getInputStream();
				OutputStream sos = clientSocket.getOutputStream();
					
				InputStreamReader isr = new InputStreamReader(sis);
				OutputStreamWriter csw = new OutputStreamWriter(sos);
					
				BufferedReader br = new BufferedReader(isr);
				BufferedWriter bw = new BufferedWriter(csw);
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		);
		this.zegarThread = new Thread(() -> increment());
	}
	
	public void getLicznik() {
		
	}
	
	public void sendInfo() {
		
	}
	
	public void synchronize() {
		
	}

}
