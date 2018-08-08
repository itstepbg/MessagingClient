package run;

import java.nio.file.Paths;
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
			// TODO Add missing file functionalities.
			System.out.println("0. Create new directory");
			System.out.println("1. Upload File");
			System.out.println("2. Delete File");
			System.out.println("3. Copy File");
			System.out.println("4. Logout");
			System.out.println("6. Share File");
		}
		System.out.println("5. Quit");
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
			case 0:
				createNewDirectory();
				break;
			case 1:
				uploadFile();
				break;
			case 2:
				deleteFile();
				break;
			case 3:
				copyFile();
				break;
			case 4:
				logout();
				break;
			case 5:
				quit();
				break;
			case 6:
				shareFile();
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

		waitForNetworking();
	}

	private static void shareFile() {
		System.out.println("Please enter user name you want to share file with:");
		String userNameSharedTo = sc.nextLine();
		System.out.println("Please enter file path of the file you want to share:");
		String filePath = sc.nextLine();

		NetworkMessage networkMessage = new NetworkMessage();

		networkMessage.setType(MessageType.SHARE_FILE);
		networkMessage.setUser(userNameSharedTo);
		networkMessage.setFilePath(filePath);

		messagingManager.getCommunication().sendMessage(networkMessage);

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
		System.out.println("Select file to upload from local path:");
		String downloadFromfilePath = sc.nextLine();

		System.out.println("Select file to upload to remote path:");
		String uploadTofilePath = sc.nextLine();

		// TODO Check whether the file exists.
		// TODO Choose remote folder.

		downloadFromfilePath.replaceAll("[/\\\\]+", Matcher.quoteReplacement(System.getProperty("file.separator")));

		NetworkMessage networkMessage = new NetworkMessage();
		networkMessage.setType(MessageType.UPLOAD_FILE);
		networkMessage.setFilePath(Paths.get(uploadTofilePath).toString());

		messagingManager.getCommunication().createFileUploadThread(downloadFromfilePath);
		System.out.println(downloadFromfilePath);
		messagingManager.getCommunication().sendMessage(networkMessage);

		waitForNetworking();
	}

	private static void createNewDirectory() {
		System.out.println("Please enter directory name:");
		String folderName = sc.nextLine();

		NetworkMessage networkMessage = new NetworkMessage();
		networkMessage.setType(MessageType.CREATE_DIRECTORY);
		networkMessage.setFilePath(folderName);

		messagingManager.getCommunication().sendMessage(networkMessage);

		waitForNetworking();
	}

	private static void deleteFile() {
		System.out.println("Select file to delete:");
		String filePath = sc.nextLine();

		filePath.replaceAll("[/\\\\]+", Matcher.quoteReplacement(System.getProperty("file.separator")));

		NetworkMessage networkMessage = new NetworkMessage();
		networkMessage.setType(MessageType.DELETE_FILE);
		networkMessage.setFilePath(Paths.get(filePath).getFileName().toString());

		messagingManager.getCommunication().sendMessage(networkMessage);

		waitForNetworking();
	}

	private static void copyFile() {
		System.out.println("Select file to copy :");
		String sourcePath = sc.nextLine();

		sourcePath.replaceAll("[/\\\\]+", Matcher.quoteReplacement(System.getProperty("file.separator")));

		System.out.println("Select a directory in which you'd like to paste the file :");
		String targetPath = sc.nextLine();

		targetPath.replaceAll("[/\\\\]+", Matcher.quoteReplacement(System.getProperty("file.separator")));

		NetworkMessage networkMessage = new NetworkMessage();
		networkMessage.setType(MessageType.COPY_FILE);
		networkMessage.setFilePath(sourcePath);
		networkMessage.setNewFilePath(targetPath);

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

		networkMessage.setType(MessageType.LOGIN);
		networkMessage.setActor(userName);
		String saltedPassword = Crypto.saltPassword(messagingManager.getCommunication().getSalt(),
				Crypto.generateHash(password), 1024);
		networkMessage.setPasswordHash(saltedPassword);

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
