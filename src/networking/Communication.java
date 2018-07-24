package networking;

import java.io.IOException;
import java.net.Socket;

import library.models.data.User;
import library.models.network.NetworkMessage;
import library.networking.CommonCommunication;
import library.networking.CommunicationInterface;
import managers.UserManager;

public class Communication extends CommonCommunication implements CommunicationInterface {

	private Object uiLock = null;

	public Communication(Socket communicationSocket) {
		super(communicationSocket);
		startCommunicationThreads(this);
	}

	public void setUiLock(Object uiLock) {
		this.uiLock = uiLock;
	}

	@Override
	public void handleMessage(NetworkMessage networkMessage) {
		switch (networkMessage.getType()) {
		case HEARTBEAT:
			heartbeatThread.resetTimeoutBuffer();
			break;
		case STATUS_RESPONSE:
			NetworkMessage request = pendingRequests.get(networkMessage.getMessageId());

			switch (request.getType()) {
			case LOGIN:
				if (networkMessage.getStatus() == NetworkMessage.STATUS_OK) {
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
			default:
				break;
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void sendMessage(NetworkMessage networkMessage) {
		updateMessageCounter(networkMessage);
		outputThread.addMessage(networkMessage);

		switch (networkMessage.getType()) {
		case LOGIN:
		case LOGOUT:
			addPendingRequest(networkMessage);
			break;
		default:
			break;
		}
	}

	@Override
	public void closeCommunication() {
		logger.info("Closing communication for " + communicationSocket.getInetAddress().getHostAddress());

		// TODO Temporary logic for marking the user as logged out when the socket is
		// closed.
		// This should be replaced when a session persistence mechanic is implemented.

		heartbeatThread.interrupt();

		if (!communicationSocket.isClosed()) {
			try {
				communicationSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// TODO Notify UI to stop the application execution.
	}

	private void notifyUiThread() {
		// Potential racing condition.
		synchronized (uiLock) {
			uiLock.notify();
		}
	}

	@Override
	public void unregisterCommunication() {
		closeCommunication();
		UserManager.getInstance().setUser(null);
	}
}
