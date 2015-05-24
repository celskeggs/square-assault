package codeday.squareassault.server;

public class Logger {
	public static void info(String text) {
		System.err.println("[INFO] " + text);
	}

	public static void warning(String text) {
		System.err.println("[WARNING] " + text);
	}

	public static void warning(String text, Throwable thr) {
		System.err.println("[WARNING] " + text);
		thr.printStackTrace();
	}
}
