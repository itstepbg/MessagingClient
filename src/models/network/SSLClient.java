package models.network;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author user
 */
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.Security;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import com.sun.net.ssl.internal.ssl.Provider;

public class SSLClient {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		String strServerName = "localhost"; // SSL Server Name
		int intSSLport = 4443; // Port where the SSL Server is listening
		PrintWriter out = null;
		BufferedReader in = null;

		{
			// Registering the JSSE provider
			Security.addProvider(new Provider());
			System.setProperty("javax.net.ssl.trustStore",
					"C:\\Users\\doom\\Documents\\GitHub\\MessagingClient\\store1\\server.ks");
			System.setProperty("javax.net.ssl.keyStorePassword", "cisco.123");
		}

		try {
			System.setProperty("javax.net.debug", "all");
			// Creating Client Sockets
			SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
			// Specifying the Keystore details

			// Initializing the streams for Communication with the Server
			try (SSLSocket sslSocket = (SSLSocket) sslsocketfactory.createSocket(strServerName, intSSLport)) {
				// Initializing the streams for Communication with the Server
				out = new PrintWriter(sslSocket.getOutputStream(), true);
				in = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));

				BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
				String userInput = "Hello Testing ";
				out.println(userInput);

				while ((userInput = stdIn.readLine()) != null) {
					out.println(userInput);
					System.out.println("echo: " + in.readLine());
				}

				out.println(userInput);

				// Closing the Streams and the Socket
				out.close();
				in.close();
				stdIn.close();
			}
		}

		catch (Exception exp) {
			System.out.println(" Exception occurred .... " + exp);
			exp.printStackTrace();
		}

	}

}
