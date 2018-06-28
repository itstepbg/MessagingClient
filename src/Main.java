import java.io.IOException;
import java.net.Socket;

public class Main {
	
	private static Socket clientSocket;
	private static int port = 3000;
	
	public static void main(String[] args) {
		
		try {
			clientSocket = new Socket("localhost", port);
			System.out.println("Connected localhost in port " + port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			clientSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
