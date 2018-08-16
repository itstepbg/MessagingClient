package networking;

import java.net.Socket;
import java.util.logging.Logger;

import library.models.data.Directory;
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
