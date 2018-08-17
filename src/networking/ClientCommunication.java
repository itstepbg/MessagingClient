package networking;

import java.net.Socket;

import java.util.logging.Logger;

import library.models.data.Directory;

import java.util.Base64;

import FTPLibrary.FTPConstants;
import library.models.data.User;
import library.models.network.MessageType;
import library.models.network.NetworkMessage;
import library.networking.Communication;

import library.util.MessagingLogger;

import library.util.ConstantsFTP;
import library.util.Crypto;
import library.util.Utils;
import managers.MessagingManager;

import managers.UserManager;
import run.Main;


public class ClientCommunication extends Communication {
	private static Logger logger = MessagingLogger.getLogger();

	private Object uiLock = null;
	private volatile boolean processing = false;

	public ClientCommunication(Socket communicationSocket) {
		super(communicationSocket);
	}

	public void setUiLock(Object uiLock) {
		this.uiLock = uiLock;
	}

	public void setProcessing(boolean processing) {
		this.processing = processing;
	}

	@Override
	public void handleMessage(NetworkMessage networkMessage) {
		super.handleMessage(networkMessage);

		NetworkMessage responseMessage;

		switch (networkMessage.getType()) {

		case SERVER_HELLO:
			if (networkMessage.getText().equals(FTPConstants.SERVER_HELLO_MESSAGE)) {
				logger.info("Hello message from server received with text: " + networkMessage.getText());
				responseMessage = new NetworkMessage();
				responseMessage.setType(MessageType.CLIENT_HELLO);
				responseMessage.setText(FTPConstants.CLIENT_HELLO_MESSAGE);
				responseMessage.setClientFQDN(Utils.getFQDN());
				sendMessage(responseMessage);
			} else {
				logger.info("Unsupported protocol version.");
				MessagingManager.getInstance().removeCommunication();
			}
			break;
		case WELCOME_MESSAGE:
			if (networkMessage.getClientFQDN().equals(Utils.getFQDN())) {
				System.out.println("Connection with server established successfully. Handshake done.");
				logger.info("Connection with server established successfully. Handshake done.");

			}
			break;

		case CONTINUE_WITH_PASS:
			//here we get the salting parameters from server and send registration password to authenticate

			String saltEncodedBase64 = networkMessage.getSalt();
			// here we decode the salt from the server
			salt = new String (Base64.getDecoder().decode(saltEncodedBase64.getBytes()));

			int iterations = Integer.valueOf(networkMessage.getIterations());
			registerPassword = Crypto.saltPassword(salt, ConstantsFTP.MASTER_PASS, iterations);

			responseMessage = new NetworkMessage();
			responseMessage.setType(MessageType.REGISTER_PASS);
			responseMessage.setText(registerPassword);

			logger.info("Registration password hash generated.");
			System.out.println("Registration password hash generated.");
			sendMessage(responseMessage);


			break;

		case REGISTRATION_ALLOWED:
			// awaiting client input from the console
			//TODO maybe we have to create class that will read commands from the console
			logger.info("The client is authenticated");

			System.out.println("Registration protocol completed.");

			Main.getUserRegistrationParameter();
			break;

		case AUTHENTICATION_FAILED:
				logger.info("Master password is not correct");

				closeCommunication();
		break;

		case STATUS_RESPONSE:
			handleStatusResponse(networkMessage);
			break;
		default:
			break;
		}
	}

	private void handleStatusResponse(NetworkMessage statusResponse) {
		NetworkMessage request = pendingRequests.get(statusResponse.getMessageId());

		switch (request.getType()) {

		case CREATE_USER:
			if (statusResponse.getStatus() != NetworkMessage.STATUS_OK) {
				User currentUser = new User();
				UserManager.getInstance().setUser(currentUser);
				processing = false;
			} else {
				// TODO Error in UI.
			}
			break;
		case REGISTRATION_FAILED:
			logger.info("Wrong username or password. Try again.");
			System.out.println("Password or username is not correct. Try again.");
			processing = false;
			break;

		case LOGIN:

			if (statusResponse.getStatus() == NetworkMessage.STATUS_OK) {
				User currentUser = new User();
				UserManager.getInstance().setUser(currentUser);
			} else {
				// TODO Error in UI.
			}

			notifyUiThread();
			break;
		case LOGOUT:
			UserManager.getInstance().setUser(null);

			notifyUiThread();
			break;
		case UPLOAD_FILE:
			if (statusResponse.getStatus() == NetworkMessage.STATUS_OK) {
				startFileUpload();
				logger.info("File successfully uploaded.");
			} else {
				clearFileUploadThread();
				logger.info("File failed uploading.");
			}

			notifyUiThread();
			break;
		case DOWNLOAD_FILE:
			if (statusResponse.getStatus() == NetworkMessage.STATUS_OK) {
				startFileUpload();
				logger.info("File successfully downloaded.");
			} else {
				clearFileUploadThread();
				logger.info("File failed downloading.");
			}

			notifyUiThread();
			break;
		case CREATE_DIRECTORY:
			if (statusResponse.getStatus() == NetworkMessage.STATUS_OK) {
				logger.info("Directory successfully created.");
			} else {
				logger.info("Directory already exists.");
			}

			notifyUiThread();
			break;
		case DELETE_FILE:
			if (statusResponse.getStatus() == NetworkMessage.STATUS_OK) {
				logger.info("The file was deleted successfully");
			} else {
				logger.info("The file that you're trying to delete doesn't exist!");
			}

			notifyUiThread();
			break;
		case COPY_FILE:
			if (statusResponse.getStatus() == NetworkMessage.STATUS_OK) {
				logger.info("The file was copied successfully.");
			} else {
				logger.info("The file that you're trying to copy already exists!");
			}

			notifyUiThread();
			break;
		case MOVE_FILE:
			if (statusResponse.getStatus() == NetworkMessage.STATUS_OK) {
				logger.info("The file was moved successfully.");
			} else {
				logger.info("The file that you're trying to move already exists in the target directory!");
			}

			notifyUiThread();
			break;
		case RENAME_FILE:
			if (statusResponse.getStatus() == NetworkMessage.STATUS_OK) {
				logger.info("The file was renamed successfully.");
			} else {
				logger.info("The directory already consists a file with this name!");
			}

			notifyUiThread();
			break;
		case LIST_FILES:

			listFiles = new Directory();
			if (statusResponse.getStatus() == NetworkMessage.STATUS_OK) {
				listFiles = statusResponse.getFileList();
				System.out.println("");
				System.out.println("These are all your directories and files: ");
			} else {
				logger.info("Could not print the information requested!");
			}

			listFiles.printDirectories();
			listFiles.printFiles();

			notifyUiThread();
			break;
		case LIST_FILES_SHARED_BY_YOU:
			listFiles = statusResponse.getFileList();
			listFiles.printFilesSharedByYou();
			notifyUiThread();
			break;
		case LIST_FILES_SHARED_WITH_YOU:
			listFiles = statusResponse.getFileList();
			listFiles.printFilesSharedWithYou();

			notifyUiThread();
			break;
		case SHARE_FILE:
			if (statusResponse.getStatus() == NetworkMessage.STATUS_OK) {
				logger.info("The file was shared successfully.");
			} else {
				logger.info("The file that you're trying to share already exists!");
			}

			notifyUiThread();
			break;
		default:
			break;
		}
	}

	@Override
	public void sendMessage(NetworkMessage networkMessage) {
		super.sendMessage(networkMessage);

		switch (networkMessage.getType()) {


		case CLIENT_HELLO:
		case REGISTER_PLAIN:
		case REGISTER_PASS:
		case CREATE_USER:
		case LOGIN:
		case LOGOUT:
		case CREATE_DIRECTORY:
		case DELETE_FILE:
		case COPY_FILE:
		case MOVE_FILE:
		case RENAME_FILE:
		case UPLOAD_FILE:
		case DOWNLOAD_FILE:
		case LIST_FILES:
		case LIST_FILES_SHARED_BY_YOU:
		case LIST_FILES_SHARED_WITH_YOU:
		case SHARE_FILE:
			addPendingRequest(networkMessage);
			break;
		default:
			break;
		}
	}

	private void notifyUiThread() {
		// Potential racing condition.
		synchronized (uiLock) {
			uiLock.notify();
		}
	}

	@Override
	public void unregisterCommunication() {
		super.unregisterCommunication();

		UserManager.getInstance().setUser(null);
	}
}
