package managers;

import java.net.Socket;

import library.networking.CommunicationInterface;
import networking.Communication;

public class MessagingManager {

	private CommunicationInterface communication = null;
	private final static MessagingManager instance = new MessagingManager();

	private MessagingManager() {
	}

	public static MessagingManager getInstance() {
		return instance;
	}

	private void setCommunication(CommunicationInterface communication) {
		this.communication = communication;
	}

	public void removeCommunication() {
		this.communication = null;
	}

	public void initCommunication(Socket communicationSocket) {
		setCommunication(new Communication(communicationSocket));
	}

	public CommunicationInterface getCommunication() {
		return communication;
	}
}
