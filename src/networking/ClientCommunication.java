package networking;

import java.net.Socket;
import java.util.logging.Logger;

import library.models.data.User;
import library.models.network.NetworkMessage;
import library.networking.Communication;
import library.util.MessagingLogger;
import managers.UserManager;

public class ClientCommunication extends Communication {
	private static Logger logger = MessagingLogger.getLogger();

	private Object uiLock = null;

	public ClientCommunication(Socket communicationSocket) {
		super(communicationSocket);
	}

	public void setUiLock(Object uiLock) {
		this.uiLock = uiLock;
	}

	@Override
	public void handleMessage(NetworkMessage networkMessage) {
		super.handleMessage(networkMessage);

		switch (networkMessage.getType()) {
		case SALT:
			salt = networkMessage.getText();
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
			if (statusResponse.getStatus() == NetworkMessage.STATUS_OK) {
				// TODO
			} else {
				// TODO Error in UI.
			}

			notifyUiThread();
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
				logger.info("The file was deleted successfully”");
			} else {
				logger.info("The file that you're trying to delete already exists!");
			}

			notifyUiThread();
			break;
		case COPY_FILE:
			if (statusResponse.getStatus() == NetworkMessage.STATUS_OK) {
				logger.info("The file was copied successfully.”");
			} else {
				logger.info("The file that you're trying to copy already exists!");
			}

			notifyUiThread();
			break;
		case SHARE_FILE:
			if (statusResponse.getStatus() == NetworkMessage.STATUS_OK) {
				logger.info("The file was shared successfully.”");
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
		case CREATE_USER:
		case LOGIN:
		case LOGOUT:
		case CREATE_DIRECTORY:
		case DELETE_FILE:
		case COPY_FILE:
		case UPLOAD_FILE:
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
