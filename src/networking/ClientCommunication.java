package networking;

import java.net.Socket;

import library.models.data.User;
import library.models.network.NetworkMessage;
import library.networking.Communication;
import managers.UserManager;

public class ClientCommunication extends Communication {

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
