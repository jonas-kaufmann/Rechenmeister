package application.Model;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.*;
import java.util.function.BiConsumer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Erstellen eines Servers, Verwatlen der einzelnen Clients, Austausch von
 * Nachrichten
 */
public class CommunicationServer implements Runnable {

	private ServerSocket server;
	private ObservableList<CommunicationServerClient> serverClients = FXCollections.observableArrayList();
	private int nextId = 0;
	private BiConsumer<CommunicationServerClient, String> handleMessage;
	private Runnable handleClientsChanged;
	private Runnable handleServerCreationError;
	private Runnable handleServerCreated;
	private int port;

	private Thread thread;
	private String threadName;
	private boolean running;

	/**
	 * 
	 * @param port                      Serverport
	 * @param threadName                Name des Serverthreads
	 * @param handleMessage             Methode, welche Nachrichten verarbeitet
	 * @param handleClientsChanged      Methode, welche Änderungen an den Clients
	 *                                  verwaltet
	 * @param handleServerCreationError Methode, welche bei Erstellungsfehlern
	 *                                  aufgerufen wird
	 * @param handleServerCreated       Methode, welche bei erfolgreicher
	 *                                  Servererstellung aufgerufen wird
	 */
	public CommunicationServer(int port, String threadName, BiConsumer<CommunicationServerClient, String> handleMessage,
			Runnable handleClientsChanged, Runnable handleServerCreationError, Runnable handleServerCreated) {
		this.threadName = threadName;
		this.handleMessage = handleMessage;
		this.handleClientsChanged = handleClientsChanged;
		this.handleServerCreated = handleServerCreated;
		this.handleServerCreationError = handleServerCreationError;
		this.port = port;
	}

	/**
	 * Erstellt den ServerSocket und öffnet ihn auf dem übergebenen Port
	 */
	public void create() {
		try {
			// ServerSocket erstellen welcher den Nachrichtenaustausch auf dem übergebenen
			// Port gewährleistet
			server = new ServerSocket(port);
			handleServerCreated.run();
		} catch (IOException e) {
			handleServerCreationError.run();
		}
	}

	/**
	 * Methode für Clientempfangsthread. Nimmt Clients, welche versuchen sich zu
	 * verbinden, an und registriert diese.
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
				addClient(server.accept());
			} catch (IOException e) {
				running = false;
			}
		}
	}

	/**
	 * Erstellen und Starten des Clientempfangsthread. (kann nur einmal pro Server
	 * ausgeführt werden)
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
	 * Schließt den Server und beendigt somit den Clientempfangsthread.
	 */
	public void close() {
		try {
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Client der Liste hinzufügen und Nachrichtenempfang starten.
	 * 
	 * @param socket Socket (Verbindung)
	 */
	private void addClient(Socket socket) {
		CommunicationServerClient serverClient = new CommunicationServerClient(socket, this::handleInput,
				serverClients.size(), nextId);
		nextId++;
		// Empfangen der Nachrichten starten
		serverClient.getClient().start();
		serverClients.add(serverClient);
		handleClientsChanged.run();
		// Client bestätigen und Username anfragen
		serverClient.getClient().sendMessage("OK username");
	}

	/**
	 * Schließt Schnittstellen des Clients und entfernt ihn aus der Liste
	 * 
	 * @param serverClient Client
	 */
	public void removeClient(CommunicationServerClient serverClient) {
		serverClients.removeIf(c -> c.getId() == serverClient.getId());
		serverClient.getClient().close();
		handleClientsChanged.run();
	}

	/**
	 * Kickt den übergebenen Client
	 * 
	 * @param serverClient Client
	 */
	public void kickClient(CommunicationServerClient serverClient) {
		serverClient.getClient().sendMessage("KICK");
		removeClient(serverClient);
	}

	/**
	 * Sendet eine Nachricht an alle Clients (Broadcast)
	 * 
	 * @param msg Nachricht
	 */
	public void sendMessageToAll(String msg) {
		int index = 0;
		while (index < serverClients.size()) {
			if (serverClients.get(index).getClient().sendMessage(msg)) {
				index++;
			}
		}
	}

	/**
	 * Methode welche alle Nachrichten der Clients verarbeitet. Nachrichten zur
	 * Kommunikation zwischen Server und Client werden verarbeitet und Nachrichten
	 * an den Nutzer (Bsp.: Punkte) werden an den Controller weitergegeben.
	 * 
	 * @param serverClient Client, welcher die Nachricht gesendet hat
	 * @param input        Nachricht
	 */
	public void handleInput(CommunicationServerClient serverClient, String input) {
		if (input.indexOf("USER") == 0) {
			String[] split;
			if ((split = input.split(" ")).length > 1) {
				serverClient.setUsername(split[1].replace(';', ' '));
				handleClientsChanged.run();
				handleMessage.accept(serverClient, "SETTINGS");
			} else
				kickClient(serverClient);
		} else if (input.equals("QUIT")) {
			serverClient.getClient().sendMessage("OK bye");
			removeClient(serverClient);
		} else if (input.equals("LOST CONNECTION")) {
			removeClient(serverClient);
		} else {
			handleMessage.accept(serverClient, input);
		}
	}

	/**
	 * @param clientIndex Index des Clients
	 * @return Client
	 */
	public CommunicationClient getClient(int clientIndex) {
		return serverClients.get(clientIndex).getClient();
	}

	/**
	 * @return Liste aller Clients
	 */
	public ObservableList<CommunicationServerClient> getServerClients() {
		return serverClients;
	}
}