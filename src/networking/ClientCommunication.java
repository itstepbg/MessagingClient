package networking;

import java.net.Socket;
import java.util.Base64;

import FTPLibrary.FTPConstants;
import library.exceptions.WrongMenuInputException;
import library.models.data.User;
import library.models.network.MessageType;
import library.models.network.NetworkMessage;
import library.networking.Communication;
import library.util.ConstantsFTP;
import library.util.Crypto;
import library.util.Utils;
import managers.MessagingManager;
import managers.UserManager;
import run.Main;


public class ClientCommunication extends Communication {

	private Object uiLock = null;
	private static volatile boolean processing = false;

	public ClientCommunication(Socket communicationSocket) {
		super(communicationSocket);
	}

	public void setUiLock(Object uiLock) {
		this.uiLock = uiLock;
	}

	public static void setProcessing(boolean processing) {
		ClientCommunication.processing = processing;
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
			} else {
				clearFileUploadThread();
				// TODO Error in UI.
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
		case UPLOAD_FILE:
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
