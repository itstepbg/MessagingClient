import java.io.DataOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.Socket;
import java.util.Scanner;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import models.network.MessageType;
import models.network.NetworkMessage;
import util.Logger;
import util.Sha1Hash;

public class Main {
	
	private static Socket communicationSocket;
	private static int port = 3000;
	private static Scanner sc = new Scanner(System.in);
	private static DataOutputStream outToServer = null;
	
	public static void main(String[] args) {
		initSocket();

		login();
		
		logout();
		
		closeSocket();		
	}

	private static void logout() {
		NetworkMessage logoutMessage = new NetworkMessage();
		logoutMessage.setType(MessageType.LOGOUT);
		
		String logoutMessageXml = serializeMessage(logoutMessage);
		
		Logger.logMessage(logoutMessageXml);
		
		try {
			outToServer.writeBytes(logoutMessageXml + "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private static void login() {
		
		System.out.println("Please enter user name:");
		String userName = sc.nextLine();
		System.out.println("Please enter your password:");
		String password = sc.nextLine();
		
		String loginMessageXml = generateLoginMessageXml(userName, password);
		
		Logger.logMessage(loginMessageXml);
		
		try {
			outToServer.writeBytes(loginMessageXml + "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	private static String generateLoginMessageXml(String userName, String password) {
		
		NetworkMessage networkMessage = new NetworkMessage();
		
		networkMessage.setType(MessageType.LOGIN);
		networkMessage.setActor(userName);
		String hashPassword = Sha1Hash.generateHash(password);
		networkMessage.setPasswordHash(hashPassword);
		
		return serializeMessage(networkMessage);		
	}

	private static String serializeMessage(NetworkMessage networkMessage) {
		
		String serializedMessage = null;
		
		try {
			JAXBContext ctx = JAXBContext.newInstance(NetworkMessage.class);

	        Marshaller m = ctx.createMarshaller();
	        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);

	        StringWriter sw = new StringWriter();
	        m.marshal(networkMessage, sw);
	        sw.close();
	        
	        serializedMessage = sw.toString();
	        
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		return serializedMessage;
	}

	private static void initSocket() {
		try {
			communicationSocket = new Socket("192.168.100.12", port);
			System.out.println("Connected localhost in port " + port);
			
			outToServer = new DataOutputStream(communicationSocket.getOutputStream());
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
		
	}
	
	private static void closeSocket() {
		try {
			outToServer.close();
			communicationSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
