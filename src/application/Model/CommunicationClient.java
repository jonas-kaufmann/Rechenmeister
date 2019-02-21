package application.Model;

import java.net.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import sun.net.ConnectionResetException;

import java.io.*;

/**
 * Herstellen und Trennen einer Verbindung zu einem Server, Austausch von
 * Nachrichten
 */
public class CommunicationClient implements Runnable {

	private Socket socket;
	private DataInputStream inputStream;
	private DataOutputStream outputStream;
	private BiConsumer<CommunicationClient, String> handleInput;
	private Consumer<CommunicationClient> handleKicked;
	private Runnable handleConnectionError;
	private Runnable handleConnected;
	private boolean isServerClient;
	private String username;
	private String serverIp;
	private int port;

	private Thread thread;
	private String threadName;
	private boolean running;

	/**
	 * Clientseitige Verbindung konfigurieren und initialisieren. Es werden noch
	 * keine Nachrichten empfangen.
	 * 
	 * @param serverIp              Serveradresse
	 * @param port                  Serverport
	 * @param username              Benutzername
	 * @param handleInput           Methode, welche Nachrichten verarbeitet
	 * @param handleKicked          Methode, welche die Kick-Nachricht verarbeitet
	 * @param handleConnectionError Methode, welche bei Verbindungsfehlern
	 *                              aufgerufen wird
	 * @param handleConnected       Methode, welche bei erfolgreicher
	 *                              Verbindungsherstellung aufgerufen wird
	 * @param threadName            Name des Threads
	 */
	public CommunicationClient(String serverIp, int port, String username, String threadName,
			BiConsumer<CommunicationClient, String> handleInput, Consumer<CommunicationClient> handleKicked,
			Runnable handleConnectionError, Runnable handleConnected) {
		this.threadName = threadName;
		this.handleInput = handleInput;
		this.handleKicked = handleKicked;
		this.handleConnected = handleConnected;
		this.handleConnectionError = handleConnectionError;
		this.username = username;
		this.port = port;
		this.serverIp = serverIp;
		isServerClient = false;
	}

	/**
	 * Clientseitige Verbindung initialisieren. Es werden noch keine Nachrichten
	 * empfangen.
	 * 
	 * @param socket Socket (Verbindung)
	 * @param handleInput Methode, welche eingehende Nachrichten verarbeitet
	 * @param threadName Name des Threads
	 * @param handleKicked Methode, die bei Ereignis Kicked ausgeführt werden soll
	 */
	public CommunicationClient(Socket socket, String threadName, BiConsumer<CommunicationClient, String> handleInput,
			Consumer<CommunicationClient> handleKicked) {
		this.socket = socket;
		this.threadName = threadName;
		this.handleInput = handleInput;
		this.handleKicked = handleKicked;
		isServerClient = true;
		// Streams zur Nachrichtenübertragung erzeugen
		openStreams();
	}

	/**
	 * Erstellt und verbindet den Socket mit dem Server.
	 */
	public void connect() {
		try {
			// Socket erstellen welcher den Nachrichtenaustausch auf dem übergebenen Port
			// mit dem Server gewährleistet
			socket = new Socket();
			socket.connect(new InetSocketAddress(serverIp, port), 2000);
			// Streams zur Nachrichtenübertragung erzeugen
			openStreams();
			handleConnected.run();
		} catch (ConnectException e) {
			handleConnectionError.run();
		} catch (IOException e) {
			handleConnectionError.run();
		}
	}

	/**
	 * Nachricht an den Server senden
	 * 
	 * @param msg Nachricht
	 * @return Nachricht versendet = true
	 */
	public boolean sendMessage(String msg) {
		try {
			outputStream.writeUTF(msg);
			outputStream.flush();
			return true;
		} catch (SocketException ex) {
			running = false;
			handleKicked.accept(this);
			close();
		} catch (IOException e) {
			running = false;
			handleKicked.accept(this);
			close();
		}
		return false;
	}

	/**
	 * Methode für Empfangsthread. Liest eingehende Nachrichten und übergibt diese
	 * an die Methode, welche den Input verarbeitet.
	 */
	public void run() {
		Thread.currentThread().setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			public void uncaughtException(Thread t, Throwable e) {
				StringWriter sw = new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				String stacktrace = sw.toString();
				System.out.println(stacktrace);
			}
		});
		while (running) {
			try {
				handleInput(inputStream.readUTF());
			} catch (ConnectionResetException ex) {
				running = false;
				handleKicked.accept(this);
				close();
			} catch (IOException e) {
				running = false;
			}
		}
	}

	/**
	 * Erstellen und Starten des Empfangsthreads.
	 */
	public void start() {
		if (thread == null) {
			thread = new Thread(this, threadName);
			thread.setDaemon(true);
			running = true;
			thread.start();
		}
	}

	/**
	 * Methode welche alle Nachrichten des Servers verarbeitet. Nachrichten zur
	 * Kommunikation zwischen Server und Client werden verarbeitet und Nachrichten
	 * an den Nutzer (Bsp.: Aufgaben) werden an den Controller weitergegeben.
	 */
	private void handleInput(String input) {
		if (!isServerClient) {
			if (input.equals("OK username")) {
				sendMessage("USER " + username.replace(' ', ';')); // Leerzeichen mit Semikolon ersetzen
			} else if (input.equals("OK bye")) {
				close();
			} else if (input.equals("KICK")) {
				close();
				handleKicked.accept(this);
			} else
				handleInput.accept(this, input);
		} else
			handleInput.accept(this, input);
	}

	/**
	 * Streams zur Nachrichtenübertragung erzeugen
	 */
	public void openStreams() {
		try {
			inputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
			outputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Benachrichtigt den Server über das geplante Verlassen
	 */
	public void quit() {
		sendMessage("QUIT");
	}

	/**
	 * Schließt Empfangsthread, Input- und Outputstream und Verbindung
	 */
	public void close() {
		try {
			socket.close();
			inputStream.close();
			outputStream.close();
		} catch (SocketException ex) {
		} catch (IOException e) {
		}
	}

	/**
	 * @return Status des Empfangsthreads
	 */
	public boolean isRunning() {
		return running;
	}
}