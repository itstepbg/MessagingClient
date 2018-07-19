package networking;

import java.io.IOException;
import java.net.Socket;
import java.util.logging.Logger;

import library.models.data.User;
import library.models.network.NetworkMessage;
import library.networking.CommonCommunication;
import library.networking.CommunicationInterface;
import library.networking.CommunicationThreadFactory;
import library.networking.InputThread;
import library.networking.OutputThread;
import library.util.MessagingLogger;
import managers.UserManager;

public class Communication extends CommonCommunication implements CommunicationInterface {
	private static Logger logger = MessagingLogger.getLogger();

	private Socket communicationSocket;
	private InputThread inputThread;
	private OutputThread outputThread;

	private Object uiLock = null;

	public Communication(Socket communicationSocket) {
		this.communicationSocket = communicationSocket;

		inputThread = CommunicationThreadFactory.createInputThread(communicationSocket);
		outputThread = CommunicationThreadFactory.createOutputThread(communicationSocket);

		inputThread.setCommunicationListener(this);
		outputThread.setCommunicationListener(this);

		inputThread.start();
		outputThread.start();
	}

	public void setUiLock(Object uiLock) {
		this.uiLock = uiLock;
	}

	@Override
	public void handleMessage(NetworkMessage networkMessage) {
		switch (networkMessage.getType()) {
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

		UserManager.getInstance().setUser(null);

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
}
