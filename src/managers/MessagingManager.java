package managers;

import java.net.Socket;

import networking.ClientCommunication;

public class MessagingManager {

	private ClientCommunication communication = null;
	private final static MessagingManager instance = new MessagingManager();

	private MessagingManager() {
	}

	public static MessagingManager getInstance() {
		return instance;
	}

	private void setCommunication(ClientCommunication communication) {
		this.communication = communication;
	}

	public void removeCommunication() {
		this.communication = null;
	}

	public void initCommunication(Socket communicationSocket) {
		setCommunication(new ClientCommunication(communicationSocket));
	}

	public ClientCommunication getCommunication() {
		return communication;
	}
}
