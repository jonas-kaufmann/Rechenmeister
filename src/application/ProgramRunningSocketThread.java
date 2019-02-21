package application;

import java.net.ServerSocket;

public class ProgramRunningSocketThread extends Thread {
	private Main main;

	/**
	 * @param mainClass Objekt der Main Klasse
	 */
	public ProgramRunningSocketThread(Main mainClass) {
		main = mainClass;
	}

	/**
	 * Startet einen neuen ProgramRunningSocket wenn der vorhandene geschlossen
	 * wird.
	 */
	@Deprecated
	public void run() {
		while (true) {
			try {
				Thread.sleep(1000);
				main.appSocket = new ServerSocket(556);
				stop();
			} catch (Exception e) {
			}
		}
	}
}