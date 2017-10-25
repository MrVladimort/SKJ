import java.io.*;
import java.net.*;

public class Klient {
	private static void log(String text){
		System.out.println("CLI: " + text);
	}
	
	private static void say(BufferedWriter bw,String msg) throws IOException{
		log(msg);
		bw.write(msg);
		bw.newLine();
		bw.flush();
	}
	
	private static boolean checkStatusCode(BufferedReader br) throws NumberFormatException, IOException{
		String status = br.readLine();
		log(status);
		int code = Integer.parseInt(status.split(" ")[0]);
		return code >= 200 && code <= 354;
	}
	
	public static void main(String[] args) throws IOException{
		final int serverPort = 25;
		final String serverName = "172.21.114.136";
		
		Socket clientSocket = new Socket(serverName, serverPort);
		
		InputStream sis = clientSocket.getInputStream();
		OutputStream sos = clientSocket.getOutputStream();
		
		BufferedReader br = new BufferedReader(new InputStreamReader(sis, "UTF-8"));
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(sos));
	
		
		//protocol aplikacji
		String data = ""; 
		boolean status = true;
		String [] querys = {"HELO 1.1.1.1", "MAIL FROM: s2@asm.pl", "RCPT TO: s3@asm.pl", "DATA"};
		
		for(String query : querys){
			if(!checkStatusCode(br)){
				clientSocket.close();
				return;
			}
			say(bw, query);
		}
		
		checkStatusCode(br);
		say(bw, "From: Vladik");
		say(bw, "To: Nastya");
		say(bw, "Subject: aha");
		say(bw, "\n");
		say(bw, "Ura");
		say(bw, "\n");
		say(bw, ".");
		checkStatusCode(br);
		
		clientSocket.close();
	}
}
