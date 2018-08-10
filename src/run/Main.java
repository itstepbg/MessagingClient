package run;

import java.nio.file.Paths;
import java.util.Base64;
import java.util.Scanner;
import java.util.logging.Logger;
import java.util.regex.Matcher;

import library.exceptions.WrongMenuInputException;
import library.models.network.MessageType;
import library.models.network.NetworkMessage;
import library.util.Crypto;
import library.util.MessagingLogger;
import managers.MessagingManager;
import managers.NetworkManager;
import managers.UserManager;
import networking.ClientCommunication;

public class Main {
	private static Logger logger = MessagingLogger.getLogger();

	private static int port = 3000;
	private static Scanner sc = new Scanner(System.in);

	private static NetworkManager networkManager = new NetworkManager();
	private static MessagingManager messagingManager = MessagingManager.getInstance();

	private static volatile boolean processing = false;
	private static boolean running = true;

	private static Object uiLock = new Object();

	public static void main(String[] args) {
		running = networkManager.initSocket("localhost", port);
		// TODO The UI lock object should be passed in the Communication constructor
		// instead.
		messagingManager.getCommunication().setUiLock(uiLock);
		ClientCommunication.setProcessing(processing);
		while (running) {
			if(!processing) {
				chooseMenuOption();
			}
			else {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public static void chooseMenuOption() {
		System.out.println();

		if (UserManager.getInstance().getUser() == null) {
			System.out.println("1. Register plain");
			System.out.println("2. register TLS");
			System.out.println("3. Login");
		}else {
			// TODO Add missing file functionalities.
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
				registerPlain();
				break;
			case 2:
				registerTLS();
				break;
			case 3:
				login();
				break;
			case 4:
				quit();
				break;

			default:
				throw new WrongMenuInputException("Choose a valid menu option!");
			}
		}

		else {
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

	public static void getUserRegistrationParameter() {
		System.out.println("Please enter new user name:");
		String userName = sc.nextLine();
		System.out.println("Please enter your new password:");
		String password = sc.nextLine();

		System.out.println("Please enter your email:");
		String email = sc.nextLine();


		sendCreateAccountMessage(userName, password, email);
		waitForNetworking();
	}

	private static void registerTLS() {
		// TODO Auto-generated method stub

	}

	private static void registerPlain() {
		NetworkMessage networkMessage = new NetworkMessage();
		networkMessage.setType(MessageType.REGISTER_PLAIN);
		messagingManager.getCommunication().sendMessage(networkMessage);
		processing = true;
	}

	private static void createAccount() {
		System.out.println("Please enter new user name:");
		String userName = sc.nextLine();
		System.out.println("Please enter your new password:");
		String password = sc.nextLine();

		System.out.println("Please enter your email:");
		String email = sc.nextLine();

		sendCreateAccountMessage(userName, password, email);

		waitForNetworking();
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
		System.out.println("Select file to upload:");
		String filePath = sc.nextLine();

		// TODO Check whether the file exists.
		// TODO Choose remote folder.

		filePath.replaceAll("[/\\\\]+", Matcher.quoteReplacement(System.getProperty("file.separator")));

		NetworkMessage networkMessage = new NetworkMessage();
		networkMessage.setType(MessageType.UPLOAD_FILE);
		networkMessage.setFilePath(Paths.get(filePath).getFileName().toString());

		messagingManager.getCommunication().createFileUploadThread(filePath);
		messagingManager.getCommunication().sendMessage(networkMessage);

		waitForNetworking();
	}

	private static void sendCreateAccountMessage(String userName, String password, String email) {
		NetworkMessage networkMessage = new NetworkMessage();

		networkMessage.setType(MessageType.CREATE_USER);
		networkMessage.setActor(userName);











		// Maybe use salting here as well?
		String passwordHash = Crypto.generateHash(password);
		networkMessage.setPasswordHash(passwordHash);
		networkMessage.setEmail(email);

		messagingManager.getCommunication().sendMessage(networkMessage);
	}

	private static void sendLoginMessage(String userName, String password) {
		NetworkMessage networkMessage = new NetworkMessage();

		int randomIterations = Crypto.getRandomIterations();
		networkMessage.setType(MessageType.LOGIN);
		networkMessage.setActor(userName);
		networkMessage.setIterations(randomIterations);

		String saltedPass = Crypto.saltPassword(messagingManager.getCommunication().getSalt(),
				Crypto.generateHash(password), randomIterations) ;
		String encodedPassBase64 = Base64.getEncoder().encodeToString(saltedPass.getBytes());

		networkMessage.setPasswordHash(encodedPassBase64);

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
