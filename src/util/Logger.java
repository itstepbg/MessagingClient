package util;

public class Logger {

	public static void logMessage(String message) {
		System.out.println("MESSAGE:" + message);
	}

	public static void logError(String message) {
		System.err.println("ERROR:" + message);
	}

	public static void logInfo(String message) {
		System.out.println("INFO:" + message);
	}
}