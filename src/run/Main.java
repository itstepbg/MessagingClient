package run;

import java.util.Scanner;
import java.util.logging.Logger;

import library.exceptions.WrongMenuInputException;
import library.models.network.MessageType;
import library.models.network.NetworkMessage;
import library.util.MessagingLogger;
import library.util.Sha1Hash;
import managers.MessagingManager;
import managers.NetworkManager;

public class Main {
	private static Logger logger = MessagingLogger.getLogger();

	private static int port = 3000;
	private static Scanner sc = new Scanner(System.in);

	private static NetworkManager networkManager = new NetworkManager();
	private static MessagingManager messagingManager = MessagingManager.getInstance();

	private static boolean running = true;

	public static void main(String[] args) {
		networkManager.initSocket("localhost", port);
		while (running) {
			chooseMenuOption();
		}
	}

	public static void chooseMenuOption() {
		// TODO The menu options should be contextual.
		// TODO Create an UserManager class that holds a reference
		// to the local User object (null if not logged in yet).
		// TODO Logout should not stop the application,
		// there should be a dedicated 'Exit' command.
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

	private static void createAccount() {
		System.out.println("Please enter new user name:");
		String userName = sc.nextLine();
		System.out.println("Please enter your new password:");
		String password = sc.nextLine();

		System.out.println("Please enter your email:");
		String email = sc.nextLine();

		sendCreateAccountMessage(userName, password, email);
	}

	private static void login() {
		System.out.println("Please enter user name:");
		String userName = sc.nextLine();
		System.out.println("Please enter your password:");
		String password = sc.nextLine();

		sendLoginMessage(userName, password);
	}

	private static void logout() {
		NetworkMessage logoutMessage = new NetworkMessage();
		logoutMessage.setType(MessageType.LOGOUT);

		messagingManager.getCommunication().sendMessage(logoutMessage);
	}

	private static void sendCreateAccountMessage(String userName, String password, String email) {
		NetworkMessage networkMessage = new NetworkMessage();

		networkMessage.setType(MessageType.CREATE_USER);
		networkMessage.setActor(userName);
		String hashPassword = Sha1Hash.generateHash(password);
		networkMessage.setPasswordHash(hashPassword);
		networkMessage.setEmail(email);

		messagingManager.getCommunication().sendMessage(networkMessage);
	}

	private static void sendLoginMessage(String userName, String password) {
		NetworkMessage networkMessage = new NetworkMessage();

		networkMessage.setType(MessageType.LOGIN);
		networkMessage.setActor(userName);
		String hashPassword = Sha1Hash.generateHash(password);
		networkMessage.setPasswordHash(hashPassword);

		messagingManager.getCommunication().sendMessage(networkMessage);
	}
}
