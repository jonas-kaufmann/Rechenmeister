package application.UI;

import java.math.BigDecimal;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import application.Controller;
import application.Model.CommunicationClient;
import application.Model.DataModel;
import application.Model.Exercise;
import application.Model.HelpingClass;
import application.Model.ScalingTextField;
import application.Model.StatisticExercise;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.transform.Scale;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

/**
 * Controller für die Wettbewerbsseite des Clients
 */
public class CompetitionC extends Controller implements Initializable {

	// View
	@FXML
	private VBox rootVBox;
	@FXML
	private TextField exerciseTF;
	@FXML
	private TextField entryTF;
	@FXML
	private TextField resultTF;
	@FXML
	private TextField timeTF;
	@FXML
	private Label usernameLabel;

	// Attribute
	private DataModel data;
	private CommunicationClient client;
	private MainMenuC mainMenuC;
	private String serverIp;
	private int port;
	private String username;
	private Exercise[] exercises;
	private int lastExerciseId;
	private int competitionTime;
	private int currentExercise = 0;
	private Timeline timeline;
	private Integer timeSeconds;
	private int score = 0;
	private Stage mainStage;
	private double minPaneWidth;
	private double minPaneHeight;
	private Scale paneScale;
	private double aspectRatio;
	private ChangeListener<Number> resizeChangeListener;
	private ObservableList<StatisticExercise> statisticExercises = FXCollections.observableArrayList();
	private int tasksCount = 0;
	private int correctlySolved = 0;
	private Runnable handleConnected;
	private Runnable handleConnectionError;
	private Alert quitAlert;

	/**
	 * @param data      DataModel
	 * @param serverIp  Serveradresse
	 * @param port      Port
	 * @param username  Benutzername
	 * @param mainMenuC Controller der Seite des Hauptmenüs
	 * @param handleConnected Methode, welche bei Ereignis Connected ausgeführt werden soll
	 * @param handleConnectionError Methode, welche bei Ereignis ConnectionError ausgeführt werden soll
	 */
	public CompetitionC(DataModel data, String serverIp, int port, String username, MainMenuC mainMenuC,
			Runnable handleConnected, Runnable handleConnectionError) {
		this.data = data;
		this.serverIp = serverIp;
		this.port = port;
		this.username = username;
		this.mainMenuC = mainMenuC;
		this.handleConnected = handleConnected;
		this.handleConnectionError = handleConnectionError;
	}

	/**
	 * Seite anzeigen
	 */
	public void show() {
		mainStage = data.mainStage;
		// Client erstellen
		client = new CommunicationClient(serverIp, port, username, "RechenmeisterClientThread", this::handleMessage,
				this::handleKicked, this::handleConnectionError, this::handleConnected);
		client.connect();

		// Eingabe im Eingabe-Textfeld beschränken
		entryTF.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {

				int caretPosition = entryTF.getCaretPosition();

				if (newValue.length() > 20) {
					entryTF.setText(oldValue);
					entryTF.positionCaret(caretPosition);
				} else {
					for (int i = 0; i < newValue.length(); i++) {
						if (!Character.isDigit(newValue.charAt(i)) && newValue.charAt(i) != '.'
								&& newValue.charAt(i) != ',' && newValue.charAt(i) != '-') {
							entryTF.setText(oldValue);
							entryTF.positionCaret(caretPosition);
							break;
						}
					}
				}
			}
		});
	}

	/**
	 * Bereitet die Skalierung des Fenster vor
	 */
	@SuppressWarnings("deprecation")
	public void readyUpScaling() {
		// Position des Fensters setzen
		if (data.windowSizes.competitionStageX != 0) {
			mainStage.setX(data.windowSizes.competitionStageX);
			mainStage.setY(data.windowSizes.competitionStageY);
		}

		// Skalierung der Textfelder
		new ScalingTextField(resultTF, 0.1);
		new ScalingTextField(entryTF, 0.1);
		new ScalingTextField(exerciseTF, 0.1);
		new ScalingTextField(timeTF, 0.1);

		// Skalierung des Fensters
		minPaneWidth = rootVBox.getWidth();
		minPaneHeight = rootVBox.getHeight();
		paneScale = new Scale(1, 1);
		rootVBox.getTransforms().setAll(paneScale);

		rootVBox.widthProperty().addListener((obs, oldVal, newVal) -> {
			paneScale.setX(newVal.doubleValue() / minPaneWidth);
			paneScale.setPivotX(newVal.doubleValue() / 2);
		});

		rootVBox.heightProperty().addListener((obs, oldVal, newVal) -> {
			paneScale.setY(newVal.doubleValue() / minPaneHeight);
			paneScale.setPivotY(newVal.doubleValue() / 2);
		});

		// Größenbegrenzung setzen
		mainStage.setMinWidth(mainStage.getWidth());
		mainStage.setMinHeight(mainStage.getHeight());
		mainStage.setMaxHeight(mainStage.getHeight());

		// Resizen mit festgelegtem Seitenverhältnis
		aspectRatio = mainStage.getHeight() / mainStage.getWidth();
		resizeChangeListener = new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				mainStage.setMinHeight(newValue.doubleValue() * aspectRatio);
				mainStage.setMaxHeight(newValue.doubleValue() * aspectRatio);
			}
		};
		mainStage.widthProperty().addListener(resizeChangeListener);

		// Breite des Fensters setzen (Höhe wird automatisch gesetzt)
		if (data.windowSizes.competitionStageWidth > 0)
			mainStage.impl_getPeer().setBounds(0, 0, false, false, (float) data.windowSizes.competitionStageWidth, 0,
					-1, -1, 0, 0);

		if (data.windowSizes.maximized) {
			Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
			double scalingFactorY = screenBounds.getHeight() / mainStage.getHeight();
			double scalingX = mainStage.getWidth() * scalingFactorY;
			if (scalingX <= screenBounds.getWidth())
				mainStage.impl_getPeer().setBounds(0, 0, false, false, (float) scalingX, 0, -1, -1, 0, 0);
			else
				mainStage.impl_getPeer().setBounds(0, 0, false, false, (float) screenBounds.getWidth(), 0, -1, -1, 0,
						0);
			mainStage.setX(screenBounds.getWidth() / 2 - mainStage.getWidth() / 2);
			mainStage.setY(screenBounds.getHeight() / 2 - mainStage.getHeight() / 2);
		}
	}

	/**
	 * Methode wird aufgerufen, sobald die View geladen wird
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		usernameLabel.setText(usernameLabel.getText() + username);
	}

	@FXML
	private void entryTF_onAction(ActionEvent event) {
		String entry = entryTF.getText();
		entry = entry.replace(',', '.');
		if (HelpingClass.isFloat(entry)) {
			BigDecimal solution = exercises[currentExercise].Solution;
			// Antwort richtig
			if (new BigDecimal(entry).compareTo(solution) == 0) {
				client.sendMessage("POINT " + currentExercise + " " + 1);
				score += 3;
				correctlySolved++;
				statisticExercises.add(new StatisticExercise(exercises[currentExercise].GetTerm(),
						exercises[currentExercise].Solution, new BigDecimal(entry), true));
			}
			// Antwort falsch
			else {
				score -= 2;
				client.sendMessage("POINT " + currentExercise + " " + 0);
				statisticExercises.add(new StatisticExercise(exercises[currentExercise].GetTerm(),
						exercises[currentExercise].Solution, new BigDecimal(entry), false));
			}
			tasksCount += 1;
			resultTF.setText("" + tasksCount);
			entryTF.setText("");
			currentExercise++;
			// Warten bis benötigte Aufgabe angekommen ist (verhindert bei großer
			// Verzörgerung bei der Aufgabenübermittlung Exceptions)
			while (exercises[currentExercise] == null) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
			}
			// Neue Aufgabe eintragen
			exerciseTF.setText(exercises[currentExercise].GetTerm());
			// Neue Aufgaben anfordern wenn nur noch 2 verbleiben
			if (lastExerciseId - 2 == currentExercise) {
				// Afugabenarray vergrößern falls nötig
				if (lastExerciseId + 5 >= exercises.length) {
					// Aufgaben und Pointarray erweitern
					Exercise[] enlargedExercises = new Exercise[exercises.length + competitionTime];
					for (int i = 0; i < exercises.length; i++) {
						enlargedExercises[i] = exercises[i];
					}
					exercises = enlargedExercises;
				}
				client.sendMessage("EXERCISES " + (lastExerciseId + 1));
			}
		}
	}

	@FXML
	private void quitBtn_onAction(ActionEvent event) {
		if (quitConfirmation()) {
			if (timeline != null)
				timeline.stop();
			client.quit();
			client.close();
			// MainMenu aufrufen
			mainStage.widthProperty().removeListener(resizeChangeListener);
			saveStageSizes();
			mainMenuC.show();
		}
	}

	private synchronized void handleConnectionError() {
		// Alert erstellen und Design bearbeiten
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle(data.applicationName);
		alert.setHeaderText(null);
		alert.setContentText("Verbindung zum Wettkampf konnte nicht hergestellt werden.");
		Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
		alertStage.getIcons().add(data.appIcon);
		// Buttons Style setzen
		for (Node node : ((ButtonBar) alert.getDialogPane().getChildren().get(2)).getButtons()) {
			((Button) node).getStylesheets().add(data.buttonCss);
		}
		// Alert zentrieren
		alertStage.setOnShown(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				if (mainStage.getX() == -32000)
					alertStage.setX(Screen.getPrimary().getVisualBounds().getWidth() / 2 - alertStage.getWidth() / 2);
				else
					alertStage.setX(mainStage.getX() + mainStage.getWidth() / 2 - alertStage.getWidth() / 2);
				if (mainStage.getY() == -32000)
					alertStage.setY(Screen.getPrimary().getVisualBounds().getHeight() / 2 - alertStage.getHeight() / 2);
				else
					alertStage.setY(mainStage.getY() + mainStage.getHeight() / 2 - alertStage.getHeight() / 2);
			}
		});
		alert.showAndWait();
		handleConnectionError.run();
	}

	private synchronized void handleConnected() {
		handleConnected.run();
		client.start();
		// Wettkampfseite aufrufen
		Stage mainStage = data.mainStage;
		setScene(mainStage, "CompetitionV.fxml");
		mainStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			public void handle(WindowEvent we) {
				onCloseMainStage(we);
			}
		});
		readyUpScaling();
	}

	private synchronized void handleMessage(CommunicationClient client, String msg) {
		if (msg.indexOf("START") == 0) {
			String[] split = msg.split(" ");
			if (split.length == 2 && HelpingClass.isInteger(split[1])) {
				competitionTime = Integer.parseInt(split[1]);
				exercises = new Exercise[competitionTime];
				// Die ersten Aufgaben anfragen
				client.sendMessage("EXERCISES 0");
				// Countdown vorbereiten
				timeSeconds = competitionTime + 5;
				timeTF.setEditable(false);
				timeTF.setStyle("-fx-background-color: red;");
				timeTF.setText("5");
				timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
					handleTimeline();
				}));
				timeline.setCycleCount(competitionTime + 5);
				// Countdown starten
				timeline.play();
			}
		} else if (msg.indexOf("EXERCISE") == 0) {
			String[] split = msg.split(" ");
			String operator = split[2];
			int index = 0;
			try {
				if (split.length == 6 && HelpingClass.isInteger(split[1])
						&& !(operator.equals("2v") || operator.equals("^2") || operator.equals("3v")
								|| operator.equals("2^x"))
						&& HelpingClass.isFloat(split[3]) && HelpingClass.isFloat(split[4])
						&& HelpingClass.isFloat(split[5])) {
					index = Integer.parseInt(split[1]);
					exercises[index] = new Exercise(operator, new BigDecimal(split[3]), new BigDecimal(split[4]),
							new BigDecimal(split[5]));
				} else if (split.length == 5 && HelpingClass.isInteger(split[1])
						&& (operator.equals("2v") || operator.equals("^2") || operator.equals("3v")
								|| operator.equals("2^x"))
						&& HelpingClass.isFloat(split[3]) && HelpingClass.isFloat(split[4])) {
					index = Integer.parseInt(split[1]);
					exercises[index] = new Exercise(operator, new BigDecimal(split[3]), new BigDecimal(split[4]));
				}
			} catch (IndexOutOfBoundsException e) {
				System.out.println(
						"Aufgabenliste: IndexOutOfBounds; vermutlich da 2 Clients am gleichen Pc/Socket eingetragen sind");
			}
			if (lastExerciseId < index)
				lastExerciseId = index;
		}
	}

	private synchronized void handleKicked(CommunicationClient client) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				if (quitAlert != null)
					quitAlert.close();
				if (timeSeconds == null || timeSeconds != -1) {
					if (timeline != null)
						timeline.stop();
					// Alert erstellen und Design bearbeiten
					Alert alert = new Alert(AlertType.INFORMATION);
					alert.setTitle(data.applicationName);
					alert.setHeaderText(null);
					alert.setContentText("Du wurdest aus dem Wettkampf entfernt.");
					Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
					alertStage.getIcons().add(data.appIcon);
					// Buttons Style setzen
					for (Node node : ((ButtonBar) alert.getDialogPane().getChildren().get(2)).getButtons()) {
						((Button) node).getStylesheets().add(data.buttonCss);
					}
					// Alert zentrieren
					alertStage.setOnShown(new EventHandler<WindowEvent>() {
						@Override
						public void handle(WindowEvent event) {
							if (mainStage.getX() == -32000)
								alertStage.setX(Screen.getPrimary().getVisualBounds().getWidth() / 2
										- alertStage.getWidth() / 2);
							else
								alertStage
										.setX(mainStage.getX() + mainStage.getWidth() / 2 - alertStage.getWidth() / 2);
							if (mainStage.getY() == -32000)
								alertStage.setY(Screen.getPrimary().getVisualBounds().getHeight() / 2
										- alertStage.getHeight() / 2);
							else
								alertStage.setY(
										mainStage.getY() + mainStage.getHeight() / 2 - alertStage.getHeight() / 2);
						}
					});
					alert.showAndWait();
					// MainMenu aufrufen
					mainStage.widthProperty().removeListener(resizeChangeListener);
					saveStageSizes();
					mainMenuC.show();
				}
			}
		});
	}

	// OnClose Event des Hauptfensters
	private void onCloseMainStage(WindowEvent we) {
		if (quitConfirmation()) {
			if (timeline != null)
				timeline.stop();
			client.quit();
			client.close();
			saveStageSizes();
		} else
			we.consume();
	}

	// true = quit; false = cancel
	private boolean quitConfirmation() {
		// Bestätigungsabfrage
		quitAlert = new Alert(AlertType.CONFIRMATION);
		quitAlert.setTitle(data.applicationName);
		quitAlert.setHeaderText(null);
		quitAlert.setContentText("Willst du den Wettkampf wirklich verlassen?");
		ButtonType buttonTypeQuit = new ButtonType("Verlassen");
		ButtonType buttonTypeCancel = new ButtonType("Abbrechen");
		quitAlert.getButtonTypes().setAll(buttonTypeQuit, buttonTypeCancel);
		Stage alertStage = (Stage) quitAlert.getDialogPane().getScene().getWindow();
		alertStage.getIcons().add(data.appIcon);
		// Buttons Style setzen
		for (Node node : ((ButtonBar) quitAlert.getDialogPane().getChildren().get(2)).getButtons()) {
			((Button) node).getStylesheets().add(data.buttonCss);
		}
		// Alert zentrieren
		alertStage.setOnShown(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				if (mainStage.getX() == -32000)
					alertStage.setX(Screen.getPrimary().getVisualBounds().getWidth() / 2 - alertStage.getWidth() / 2);
				else
					alertStage.setX(mainStage.getX() + mainStage.getWidth() / 2 - alertStage.getWidth() / 2);
				if (mainStage.getY() == -32000)
					alertStage.setY(Screen.getPrimary().getVisualBounds().getHeight() / 2 - alertStage.getHeight() / 2);
				else
					alertStage.setY(mainStage.getY() + mainStage.getHeight() / 2 - alertStage.getHeight() / 2);
			}
		});
		Optional<ButtonType> result = quitAlert.showAndWait();
		return result.get() == buttonTypeQuit;
	}

	private void handleTimeline() {
		timeSeconds--;

		if (timeSeconds > competitionTime) {
			timeTF.setText((timeSeconds - competitionTime) + "");
		} else {
			timeTF.setText(timeSeconds.toString());
		}

		if (timeSeconds == 0) {
			timeSeconds = -1;
			entryTF.setDisable(true);
			timeTF.setStyle("-fx-background-color: red;");
			timeline.stop();
			// Statistikseite aufrufen
			mainStage.widthProperty().removeListener(resizeChangeListener);
			saveStageSizes();
			(new CompetitionStatisticsC(data, mainMenuC, statisticExercises, 5, client, score, correctlySolved)).show();
		} else if (timeSeconds == competitionTime) {
			// Warten bis benötigte Aufgabe angekommen ist (verhindert bei großer
			// Verzögerung bei der Aufgabenübermittlung Exceptions)
			while (exercises[currentExercise] == null) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException ex) {
				}
			}
			// Neue Aufgabe eintragen
			exerciseTF.setText(exercises[currentExercise].GetTerm());
			entryTF.setDisable(false);
			entryTF.requestFocus();
			timeTF.setStyle("-fx-background-color: green;");
		}
	}

	private void saveStageSizes() {
		if (!data.windowSizes.maximized && !mainStage.isIconified()) {
			// Stagemaße abspeichern
			data.windowSizes.competitionStageWidth = mainStage.getWidth();
			data.windowSizes.competitionStageX = mainStage.getX();
			data.windowSizes.competitionStageY = mainStage.getY();
		}
	}
}
