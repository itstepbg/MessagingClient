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
import managers.UserManager;

public class Main {
	private static Logger logger = MessagingLogger.getLogger();

	private static int port = 3000;
	private static Scanner sc = new Scanner(System.in);

	private static NetworkManager networkManager = new NetworkManager();
	private static MessagingManager messagingManager = MessagingManager.getInstance();

	private static boolean running = true;

	private static Object uiLock = new Object();

	public static void main(String[] args) {
		running = networkManager.initSocket("localhost", port);
		// TODO The UI lock object should be passed in the Communication constructor
		// instead.
		messagingManager.getCommunication().setUiLock(uiLock);
		while (running) {
			chooseMenuOption();
		}
	}

	public static void chooseMenuOption() {
		System.out.println();
		if (UserManager.getInstance().getUser() == null) {
			System.out.println("1. Create Account");
			System.out.println("2. Login");
		} else {
			System.out.println("1. Upload File");
			System.out.println("2. Logout");
		}
		System.out.println("3. Quit");
		System.out.println();

		int inputOption = Integer.parseInt(sc.nextLine());

		try {
			manageUserInput(inputOption);
		} catch (WrongMenuInputException e) {
			System.out.println(e.getMessage());
		}
	}

	public static void manageUserInput(int inputOption) throws WrongMenuInputException {
		if (UserManager.getInstance().getUser() == null) {
			switch (inputOption) {
			case 1:
				createAccount();
				break;
			case 2:
				login();
				break;
			case 3:
				quit();
				break;
			default:
				throw new WrongMenuInputException("Choose a valid menu option!");
			}
		} else {
			switch (inputOption) {
			case 1:
				uploadFile();
				break;
			case 2:
				logout();
				break;
			case 3:
				quit();
				break;
			default:
				throw new WrongMenuInputException("Choose a valid menu option!");
			}
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

		waitForNetworking();
	}

	private static void logout() {
		NetworkMessage logoutMessage = new NetworkMessage();
		logoutMessage.setType(MessageType.LOGOUT);

		messagingManager.getCommunication().sendMessage(logoutMessage);

		waitForNetworking();
	}

	private static void quit() {
		logout();
		messagingManager.getCommunication().closeCommunication();
		running = false;
	}

	private static void uploadFile() {
		// TODO Auto-generated method stub
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

	private static void waitForNetworking() {
		synchronized (uiLock) {
			try {
				uiLock.wait();
			} catch (InterruptedException e) {
			}
		}
	}
}
