import java.io.*;
import java.net.*;

public class Klient {
	private static void log(String text){
		System.out.println("CLI: " + text);
	}
	
	public static void main(String[] args) throws IOException{
		final int serverPort = 10000;
		final String serverName = "172.21.224.12";
		
		Socket clientSocket = new Socket(serverName, serverPort);
		
		InputStream sis = clientSocket.getInputStream();
		OutputStream sos = clientSocket.getOutputStream();
		
		BufferedReader br = new BufferedReader(new InputStreamReader(sis, "UTF-8"));
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(sos));
		
		
		//protocol aplikacji
		bw.write("ABD");
		bw.newLine();
		bw.flush();
		
		bw.write("1234");
		bw.newLine();
		bw.flush();
		
		bw.write("Alo alo alo");
		bw.newLine();
		bw.flush();
		
		String res = br.readLine();
		
		log("Response " +res);
		
		clientSocket.close();
	}
}
