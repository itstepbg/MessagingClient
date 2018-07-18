package networking;

import java.io.IOException;
import java.net.Socket;
import java.util.logging.Logger;

import library.models.network.NetworkMessage;
import library.networking.CommunicationInterface;
import library.networking.CommunicationThreadFactory;
import library.networking.InputThread;
import library.networking.OutputThread;
import library.util.MessagingLogger;

public class Communication implements CommunicationInterface {
	private static Logger logger = MessagingLogger.getLogger();

	private Socket communicationSocket;
	private InputThread inputThread;
	private OutputThread outputThread;

	public Communication(Socket communicationSocket) {
		this.communicationSocket = communicationSocket;

		inputThread = CommunicationThreadFactory.createInputThread(communicationSocket);
		outputThread = CommunicationThreadFactory.createOutputThread(communicationSocket);

		inputThread.setCommunicationListener(this);
		outputThread.setCommunicationListener(this);

		inputThread.start();
		outputThread.start();
	}

	@Override
	public void handleMessage(NetworkMessage networkMessage) {
		// TODO Auto-generated method stub
	}

	@Override
	public void sendMessage(NetworkMessage networkMessage) {
		outputThread.addMessage(networkMessage);
	}

	@Override
	public void closeCommunication() {
		logger.info("Closing communication for " + communicationSocket.getInetAddress().getHostAddress());

		if (!communicationSocket.isClosed()) {
			try {
				communicationSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
