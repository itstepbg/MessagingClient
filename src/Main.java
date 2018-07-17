import java.io.DataOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import exepctions.WrongMenuInputException;
import library.models.network.MessageType;
import library.models.network.NetworkMessage;
import library.util.MessagingLogger;
import library.util.Sha1Hash;

public class Main {

	private static Logger logger = MessagingLogger.getLogger();

	private static Socket communicationSocket;
	private static int port = 3000;
	private static Scanner sc = new Scanner(System.in);
	private static DataOutputStream outToServer = null;
	private static boolean running = true;

	public static void main(String[] args) {
		initSocket();
		chooseMenuOption();

		// login();

		// logout();

		closeSocket();
		while (running) {
			chooseMenuOption();
		}
	}

	public static void chooseMenuOption() {
		System.out.println();
		System.out.println("0. Create Account");
		System.out.println("1. Login");
		System.out.println("2. Logout");
		System.out.println();

		int inputOption = Integer.parseInt(sc.nextLine());

		try {
			manageUserInput(inputOption);
		} catch (WrongMenuInputException e) {
			System.out.println(e.getMessage());
		}
	}

	public static void manageUserInput(int inputOption) throws WrongMenuInputException {
		switch (inputOption) {
		case 0:
			createAccount();
			break;
		case 1:
			login();
			break;
		case 2:
			logout();
			break;
		default:
			throw new WrongMenuInputException("Choose a valid menu option!");
		}
	}

	private static void logout() {
		NetworkMessage logoutMessage = new NetworkMessage();
		logoutMessage.setType(MessageType.LOGOUT);

		String logoutMessageXml = serializeMessage(logoutMessage);

		logger.info(logoutMessageXml);

		try {
			outToServer.writeBytes(logoutMessageXml + "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void createAccount() {
		System.out.println("Please enter new user name:");
		String userName = sc.nextLine();
		System.out.println("Please enter your new password:");
		String password = sc.nextLine();

		System.out.println("Please enter your email:");
		String email = sc.nextLine();

		String createAccountMsg = generateCreateAccountMessageXml(userName, password, email);

		logger.info(createAccountMsg);
		try {
			outToServer.writeBytes(createAccountMsg + "\n");

			// clientCommunication.sendMessage(new NetworkMessage(...))

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	private static void login() {

		System.out.println("Please enter user name:");
		String userName = sc.nextLine();
		System.out.println("Please enter your password:");
		String password = sc.nextLine();

		String loginMessageXml = generateLoginMessageXml(userName, password);

		logger.info(loginMessageXml);

		try {
			outToServer.writeBytes(loginMessageXml + "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static String generateCreateAccountMessageXml(String userName, String password, String email) {
		NetworkMessage networkMessage = new NetworkMessage();

		networkMessage.setType(MessageType.CREATE_USER);
		networkMessage.setActor(userName);
		String hashPassword = Sha1Hash.generateHash(password);
		networkMessage.setPasswordHash(hashPassword);
		networkMessage.setEmail(email);

		return serializeMessage(networkMessage);
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
			communicationSocket = new Socket("localhost", port);
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
