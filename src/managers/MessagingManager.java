package managers;

import java.net.Socket;

import networking.Communication;

public class MessagingManager {

	private Communication communication = null;
	private final static MessagingManager instance = new MessagingManager();

	private MessagingManager() {
	}

	public static MessagingManager getInstance() {
		return instance;
	}

	private void setCommunication(Communication communication) {
		this.communication = communication;
	}

	public void removeCommunication() {
		this.communication = null;
	}

	public void initCommunication(Socket communicationSocket) {
		setCommunication(new Communication(communicationSocket));
	}

	public Communication getCommunication() {
		return communication;
	}
}
