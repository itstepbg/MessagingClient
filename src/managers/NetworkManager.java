package managers;

import java.io.IOException;
import java.net.Socket;

import library.util.MessagingLogger;

public class NetworkManager {

	public boolean initSocket(String ipAddress, int port) {
		try {
			Socket communicationSocket = new Socket(ipAddress, port);
			// communicationSocket.setSoTimeout(2000);
			MessagingManager.getInstance().initCommunication(communicationSocket);
			MessagingLogger.getLogger().info("Connected to " + ipAddress + " on port " + port);
			return true;
		} catch (IOException e) {
			return false;
		}
	}
}
