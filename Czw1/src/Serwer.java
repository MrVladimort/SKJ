import java.io.*;
import java.net.*;

public class Serwer {
	private static void log(String text){
		System.out.println("SER: " + text);
	}
	
	public static void main(String[]args) throws IOException{
		log("START");
		
		final int serverPort = 10000;
		log("Server socket creating");
		ServerSocket welcomeSocket = new ServerSocket(serverPort);
		log("Server socket created");
		
		log("Server listening");
		Socket clientSocket = welcomeSocket.accept();
		String clientIp = clientSocket.getInetAddress().toString();
		int clientPort = clientSocket.getPort();
		log("Client connected " + clientIp + ":" + clientPort);
		
		log("Streams collecting");
		InputStream sis = clientSocket.getInputStream();
		OutputStream sos = clientSocket.getOutputStream();
		
		BufferedReader br = new BufferedReader(new InputStreamReader(sis, "UTF-8"));
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(sos));
		
		//protocol aplikacji
		String loginS = "ABCD", passwordS = "1234", loginC = "", passwordC = "";
		boolean auth = true;

		loginC = br.readLine();
		if(loginS.equals(loginC)){
			log("Correct login");
			bw.write("Correct login");
			bw.newLine();
			bw.flush();
			passwordC = br.readLine();
			if(passwordS.equals(passwordC)){
				log("Correct pass");
				bw.write("Correct pass");
				bw.newLine();
				bw.flush();
			}else{
				log("Invalid login");
				bw.write("Invalid login");
				bw.newLine();
				bw.flush();
				auth = false;
			}
		}else{
			log("Invalid login");
			bw.write("Invalid login");
			bw.newLine();
			bw.flush();
			auth = false;
		}
		
		log("User: " + loginC + " " + passwordC);
		
		if(auth){
			String requestFromClient = br.readLine();
			log("Client request " + requestFromClient);
			log("Client reverse service");
			String responseFromServer = new StringBuilder(requestFromClient).reverse().toString();
			bw.write(responseFromServer);
			bw.newLine();
			bw.flush();
			log("Client response " + responseFromServer);
		}else{
			log("Invalid credenrials");
			bw.write("Invalid credentials");
			bw.newLine();
			bw.flush();
		}
		
		log("Server socket closing");
		welcomeSocket.close();
		log("Server socket closed");
		log("STOP");
	}
}
