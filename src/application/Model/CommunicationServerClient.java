package application.Model;

import java.net.*;
import java.util.function.BiConsumer;

/**
 * CommunicationClient als Client des Servers initialisieren, Austausch von Nachrichten
 */
public class CommunicationServerClient implements Cloneable {

	private int id;
	private String username;
	private CommunicationClient client;
	private BiConsumer<CommunicationServerClient, String> handleInput;
	private boolean[] pointArray;
	private int score = 0;
	private int editedExercises = 0;

	/**
	 * Serverseitige Verbindung zu einem Client initialisieren. Es werden noch keine Nachrichten empfangen.
	 * @param socket Verbindung
	 * @param handleInput Methode, welche eingehende Nachrichten verarbeitet
	 * @param clientIndex Index des Clients in der Liste aller Clients
	 * @param id ID des Clients
	 */
	public CommunicationServerClient(Socket socket, BiConsumer<CommunicationServerClient, String> handleInput,
			int clientIndex, int id) {
		this.handleInput = handleInput;
		this.id = id;
		client = new CommunicationClient(socket, "RechenmeisterServerClientThread" + id, this::handleInput, this::handleKicked);
	}

	/**
	 * @param client Client
	 */
	public CommunicationServerClient(CommunicationServerClient client) {
		this.id = client.id;
		this.username = client.username;
		this.pointArray = client.pointArray;
		this.score = client.score;
		this.editedExercises = client.editedExercises;
	}

	/**
	 * @param client Client
	 * @param input Nachricht
	 */
	private synchronized void handleInput(CommunicationClient client, String input) {
		handleInput.accept(this, input);
	}
	
	/**
	 * @param client Client
	 * @param input Nachricht
	 */
	private synchronized void handleKicked(CommunicationClient client) {
		handleInput.accept(this, "LOST CONNECTION");
	}

	/**
	 * @param exerciseIndex Aufgabenindex
	 * @param point true = Punkt, false = kein Punkt
	 */
	public void setPoint(int exerciseIndex, boolean point) {
		pointArray[exerciseIndex] = point;
		editedExercises++;
		if (point)
			score += 3;
		else score -= 2;
	}

	/**
	 * @return Punktearray
	 */
	public boolean[] getPointArray() {
		return pointArray;
	}

	/**
	 * @param pointArray PunkteArray
	 */
	public void setPointArray(boolean[] pointArray) {
		this.pointArray = pointArray;
	}

	/**
	 * @return Punkte
	 */
	public int getScore() {
		return score;
	}
	
	/**
	 * @return Anzahl bearbeitete Aufgaben
	 */
	public int getEditedExercises() {
		return editedExercises;
	}
	
	/**
	 * @return Client-Id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return Nutzername
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username Nutzername
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return Client
	 */
	public CommunicationClient getClient() {
		return client;
	}
}