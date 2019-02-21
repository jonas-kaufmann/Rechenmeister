package application.UI;

import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

import application.Controller;
import application.Model.CommunicationServer;
import application.Model.CommunicationServerClient;
import application.Model.DataModel;
import application.Model.Exercise;
import application.Model.Exercisegenerator;
import application.Model.HelpingClass;
import application.Model.ScalingTextField;
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
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;
import javafx.scene.transform.Scale;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

/**
 * Controller für die Wettbewerbsseite des Servers
 */
public class HostCompetitionC extends Controller implements Initializable {

	// View
	@FXML
	private AnchorPane anchorPane;
	@FXML
	private TextField timeTF;
	@FXML
	private Button startBtn;
	@FXML
	private Button settingsBtn;
	@FXML
	private Button kickBtn;
	@FXML
	private Label ipLabel;
	@FXML
	private TableView<CommunicationServerClient> clientsTV;
	@FXML
	private Label signedInCountLabel;

	// Konstanten
	private static final int MIN_COMPETITION_TIME = 5;

	// Attribute
	private DataModel data;
	private CommunicationServer Server;
	private MainMenuC mainMenuC;
	private int competitionTime;
	private Exercisegenerator generator;
	private Exercise[] exercises;
	private Timeline timeline;
	private int timeSeconds = -1;
	private ArrayList<CommunicationServerClient> savedServerClients;
	private Stage mainStage;
	private double minPaneWidth;
	private double minPaneHeight;
	private Scale paneScale;
	private double aspectRatio;
	private ChangeListener<Number> resizeChangeListener;
	private ObservableList<CommunicationServerClient> clientData;
	private Runnable handleConnected;
	private ChangeListener<String> timeTF_changeListener;

	/**
	 * @param data DataModel
	 * @param handleConnected Methode, die bei Ereignis Connected ausgeführt werden soll
	 * @param mainMenuC  Controller der Hauptmenüseite
	 */
	public HostCompetitionC(DataModel data, MainMenuC mainMenuC, Runnable handleConnected) {
		this.data = data;
		this.mainMenuC = mainMenuC;
		this.handleConnected = handleConnected;
		generator = new Exercisegenerator(data);
	}

	/**
	 * Seite anzeigen
	 */
	public void show() {
		mainStage = data.mainStage;
		// Server erstellen
		Server = new CommunicationServer(data.PORT, "RechenmeisterServerThread", this::handleServerMessage,
				this::handleClientsChanged, this::handleServerCreationError, this::handleServerCreated);
		Server.create();
		if (timeTF != null) {
			timeTF.setText("" + data.settings.competitionDuration);
			timeTF_changeListener = new ChangeListener<String>() {
				@Override
				public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
					int caretPosition = timeTF.getCaretPosition();
					if (!(newValue.isEmpty() || (HelpingClass.isInteger(newValue) && Integer.parseInt(newValue) > 0))) {
						timeTF.setText(oldValue);
						timeTF.positionCaret(caretPosition);
					}
				}
			};
			timeTF.textProperty().addListener(timeTF_changeListener);
		}
	}

	/**
	 * Methode wird aufgerufen, sobald die View geladen wird
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		try {
			ipLabel.setText(ipLabel.getText() + InetAddress.getLocalHost().getHostAddress().toString());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		clientsTV.setItems(Server.getServerClients());
		clientsTV.getSortOrder().clear();
		clientsTV.getSortOrder().add(clientsTV.getColumns().get(1));
	}

	/**
	 * Bereitet die Skalierung des Fenster vor
	 */
	@SuppressWarnings("deprecation")
	public void readyUpScaling() {
		// Position des Fensters setzen
		if (data.windowSizes.hostCompetitionStageX != 0) {
			mainStage.setX(data.windowSizes.hostCompetitionStageX);
			mainStage.setY(data.windowSizes.hostCompetitionStageY);
		}

		// Skalierung der Textfelder
		new ScalingTextField(timeTF, 0.1);

		// Skalierung des Fensters
		minPaneWidth = anchorPane.getWidth();
		minPaneHeight = anchorPane.getHeight();
		paneScale = new Scale(1, 1);
		anchorPane.getTransforms().setAll(paneScale);

		anchorPane.widthProperty().addListener((obs, oldVal, newVal) -> {
			paneScale.setX(newVal.doubleValue() / minPaneWidth);
			paneScale.setPivotX(newVal.doubleValue() / 2);
		});

		anchorPane.heightProperty().addListener((obs, oldVal, newVal) -> {
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
		if (data.windowSizes.hostCompetitionWidth > 0)
			mainStage.impl_getPeer().setBounds(0, 0, false, false, (float) data.windowSizes.hostCompetitionWidth, 0, -1,
					-1, 0, 0);

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

	@FXML
	private void startBtn_onAction(ActionEvent event) {
		if (HelpingClass.isInteger(timeTF.getText())) {
			competitionTime = Integer.parseInt(timeTF.getText());
			if (competitionTime >= MIN_COMPETITION_TIME) {
				data.settings.competitionDuration = competitionTime;
				// Testen ob Clients noch verbunden sind
				int index = 0;
				while (index < Server.getServerClients().size()) {
					if (Server.getServerClients().get(index).getClient().sendMessage("Connectiontest")) {
						index++;
					}
				}
				if (Server.getServerClients().size() > 0) {
					Server.close();
					startBtn.setDisable(true);
					settingsBtn.setDisable(true);
					kickBtn.setDisable(true);
					timeTF.textProperty().removeListener(timeTF_changeListener);
					// Settings laden
					// Neue Aufgaben erzeugen
					exercises = createNewExercises();
					// Countdown vorbereiten
					timeSeconds = competitionTime + 10;
					timeTF.setEditable(false);
					timeTF.setStyle("-fx-background-color: red;");
					timeTF.setText("5");
					ipLabel.requestFocus();
					timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
						handleTimeline();
					}));
					timeline.setCycleCount(competitionTime + 10); // 5 Sekunden im voraus für Aufgabenverteilung, 5
																	// Sekunden
																	// im Nachhinein für Verzögerungen während des
																	// Wettkampfes
					clientsTV.getSortOrder().clear();
					clientsTV.getSortOrder().add(clientsTV.getColumns().get(3));

					// Clients mitteilen dass der Wettkampf starten
					Server.sendMessageToAll("START " + competitionTime);

					// Countdown starten
					timeline.play();
				}
			}
		}
	}

	@FXML
	private void closeBtn_onAction(ActionEvent event) {
		if (quitConfirmation()) {
			// Timeline stoppen
			if (timeline != null)
				timeline.stop();
			// Serversocket schließen
			Server.close();
			// Clients kicken
			for (int i = Server.getServerClients().size() - 1; i >= 0; i--) {
				Server.kickClient(Server.getServerClients().get(i));
			}
			saveStageSizes();
			if (HelpingClass.isInteger(timeTF.getText())) {
				competitionTime = Integer.parseInt(timeTF.getText());
				if (competitionTime >= MIN_COMPETITION_TIME) {
					data.settings.competitionDuration = competitionTime;
				}
			}
			// MainMenu aufrufen
			mainStage.widthProperty().removeListener(resizeChangeListener);
			mainMenuC.show();
		}
	}

	@FXML
	private void kickBtn_onAction(ActionEvent event) {
		if (clientsTV.getSelectionModel().getSelectedItem() != null)
			Server.kickClient(clientsTV.getSelectionModel().getSelectedItem());
	}

	@FXML
	private void settingsBtn_onAction(ActionEvent event) {
		// Einstellungs-Popup-Fenster generieren
		Stage popupStage = createDialogStage(data.mainStage);
		popupStage.getIcons().add(data.appIcon);
		popupStage.setTitle("Einstellungen");
		new SettingsC(data, popupStage).show();
	}

	private synchronized void handleServerCreationError() {
		// Alert erstellen und Design bearbeiten
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle(data.applicationName);
		alert.setHeaderText(null);
		alert.setContentText("Es wird bereits ein anderer Wettkampf gehostet.");
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
	}

	private synchronized void handleServerCreated() {
		handleConnected.run();
		Server.start();
		// HostWettkampf Seite aufrufen
		Stage mainStage = data.mainStage;
		setScene(mainStage, "HostCompetitionV.fxml");
		mainStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			public void handle(WindowEvent we) {
				onCloseMainStage(we);
			}
		});
		readyUpScaling();
	}

	private synchronized void handleServerMessage(CommunicationServerClient serverClient, String msg) {
		// Aufgaben an Clients versenden wenn diese sie anfragen
		if (msg.indexOf("EXERCISES") == 0) {
			String[] split;
			if ((split = msg.split(" ")).length == 2 && HelpingClass.isInteger(split[1])) {
				int requestedId = Integer.parseInt(split[1]);
				// Falls nicht mehr genug Aufgaben gespeichert sind neue erzeugen
				if (requestedId + 5 >= exercises.length) {
					exercises = createNewExercises();
				}
				// 5 Aufgaben versenden
				String operator;
				for (int i = requestedId; i < requestedId + 5; i++) {
					operator = exercises[i].Operator;
					if (operator.equals("2v") || operator.equals("^2") || operator.equals("3v")
							|| operator.equals("2^x"))
						serverClient.getClient().sendMessage("EXERCISE " + i + " " + operator + " "
								+ exercises[i].Number1 + " " + exercises[i].Solution);
					else
						serverClient.getClient().sendMessage("EXERCISE " + i + " " + operator + " "
								+ exercises[i].Number1 + " " + exercises[i].Number2 + " " + exercises[i].Solution);
				}
			}
		} else if (msg.indexOf("POINT") == 0 && timeSeconds > 0 && timeSeconds <= competitionTime + 5) {
			String[] split;
			if ((split = msg.split(" ")).length == 3 && HelpingClass.isInteger(split[1])
					&& HelpingClass.isInteger(split[2])) {
				int exerciseId = Integer.parseInt(split[1]);
				if (exerciseId < serverClient.getPointArray().length) {
					int point = Integer.parseInt(split[2]);
					if (point == 1) {
						serverClient.setPoint(exerciseId, true);
					} else if (point == 0)
						serverClient.setPoint(exerciseId, false);
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							// Tabelle aktuallisieren
							clientsTV.refresh();
							clientsTV.sort();
						}
					});
				}
			}
		}
	}

	private synchronized void handleClientsChanged() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				signedInCountLabel.setText("Angemeldet: " + Server.getServerClients().size());
				clientsTV.sort();
			}
		});
	}

	// OnClose Event des Hauptfensters
	private void onCloseMainStage(WindowEvent we) {
		if (quitConfirmation()) {
			// Timeline stoppen
			if (timeline != null)
				timeline.stop();
			// Serversocket schließen
			Server.close();
			// Clients kicken
			for (int i = Server.getServerClients().size() - 1; i >= 0; i--) {
				Server.kickClient(Server.getServerClients().get(i));
			}
			saveStageSizes();
			if (HelpingClass.isInteger(timeTF.getText())) {
				competitionTime = Integer.parseInt(timeTF.getText());
				if (competitionTime >= MIN_COMPETITION_TIME) {
					data.settings.competitionDuration = competitionTime;
				}
			}
		} else
			we.consume();
	}

	// true = quit; false = cancel
	private boolean quitConfirmation() {
		if (timeSeconds != -1) {
			// Bestätigungsabfrage
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle(data.applicationName);
			alert.setHeaderText(null);
			alert.setContentText("Wollen sie den Wettkampf wirklich schließen?");
			ButtonType buttonTypeClose = new ButtonType("Schließen");
			ButtonType buttonTypeCancel = new ButtonType("Abbrechen");
			alert.getButtonTypes().setAll(buttonTypeClose, buttonTypeCancel);
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
						alertStage
								.setX(Screen.getPrimary().getVisualBounds().getWidth() / 2 - alertStage.getWidth() / 2);
					else
						alertStage.setX(mainStage.getX() + mainStage.getWidth() / 2 - alertStage.getWidth() / 2);
					if (mainStage.getY() == -32000)
						alertStage.setY(
								Screen.getPrimary().getVisualBounds().getHeight() / 2 - alertStage.getHeight() / 2);
					else
						alertStage.setY(mainStage.getY() + mainStage.getHeight() / 2 - alertStage.getHeight() / 2);
				}
			});
			Optional<ButtonType> result = alert.showAndWait();
			return result.get() == buttonTypeClose;
		} else
			return true;
	}

	private void handleTimeline() {
		timeSeconds--;
		if (timeSeconds > competitionTime + 5)
			timeTF.setText("" + (timeSeconds - competitionTime - 5));
		else if (timeSeconds > 5)
			timeTF.setText("" + (timeSeconds - 5));
		else
		timeTF.setText("" + timeSeconds);
		if (timeSeconds == 0) {
			timeline.stop();
			// ServerClients abspeichern und in die Tabelle setzen
			savedServerClients = new ArrayList<CommunicationServerClient>(Server.getServerClients().size());
			for (CommunicationServerClient client : Server.getServerClients())
				savedServerClients.add(new CommunicationServerClient(client));
			clientData = FXCollections.observableList(savedServerClients);
			clientsTV.setItems(clientData);
			clientsTV.getSortOrder().clear();
			clientsTV.getSortOrder().add(clientsTV.getColumns().get(3));
			timeSeconds = -1;
		} else if (timeSeconds == competitionTime + 5)
			timeTF.setStyle("-fx-background-color: green;");
		else if (timeSeconds == 5)
			timeTF.setStyle("-fx-background-color: red;");
	}

	private Exercise[] createNewExercises() {
		Exercise[] _exercises;
		int index;
		if (exercises == null) {
			// Aufgabenarray neu erzeugen
			_exercises = new Exercise[competitionTime];
			index = 0;
			// Pointarrays der Clients erzeugen
			for (CommunicationServerClient client : Server.getServerClients())
				client.setPointArray(new boolean[competitionTime]);
		} else {
			// Neues erweitertes Aufgabenarray erzeugen und alte Aufgaben kopieren
			_exercises = new Exercise[exercises.length + competitionTime];
			for (int i = 0; i < exercises.length; i++) {
				_exercises[i] = exercises[i];
			}
			index = exercises.length;
			// Pointarrays der Clients erweitern
			boolean[] pointArray;
			boolean[] enlargedPointArray;
			for (CommunicationServerClient client : Server.getServerClients()) {
				pointArray = client.getPointArray();
				enlargedPointArray = new boolean[exercises.length + competitionTime];
				for (int i = 0; i < exercises.length; i++) {
					enlargedPointArray[i] = pointArray[i];
				}
				client.setPointArray(enlargedPointArray);
			}
		}
		// Neue Aufgaben generieren und im lokalen Array abspeichern
		for (int i = index; i < _exercises.length; i++) {
			if (i > 0) {
				// Solange neue Aufgaben generieren bis der neue Term nicht gleich dem der alten
				// Aufgabe ist oder bis fünf Mal die selben Aufgaben nacheinander generiert
				// wurden
				int counter = 0;
				boolean successfull = false;

				while (!successfull && counter <= 5) {
					_exercises[i] = generator.generate();
					counter++;
					successfull = true;
					for (int a = 0; a < i; a++) {
						if (_exercises[i].CompareExercise(_exercises[a])) {
							successfull = false;
							break;
						}
					}
				}
				if (counter > 5) {
					// 2 gleiche Aufgaben hintereinander ausschließen falls alle einmal gestellt
					// wurden
					counter = 0;
					while (_exercises[i].CompareExercise(_exercises[i - 1]) && counter <= 5) {
						_exercises[i] = generator.generate();
						counter++;
					}
				}
			} else
				_exercises[i] = generator.generate();
		}
		return _exercises;
	}

	private void saveStageSizes() {
		if (!data.windowSizes.maximized && !mainStage.isIconified()) {
			// Stagemaße abspeichern
			data.windowSizes.hostCompetitionWidth = mainStage.getWidth();
			data.windowSizes.hostCompetitionStageX = mainStage.getX();
			data.windowSizes.hostCompetitionStageY = mainStage.getY();
		}
	}
}
