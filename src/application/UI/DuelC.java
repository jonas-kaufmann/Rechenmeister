package application.UI;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import application.Controller;
import application.Model.DataModel;
import application.Model.Exercise;
import application.Model.Exercisegenerator;
import application.Model.HelpingClass;
import application.Model.ScalingTextField;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.transform.Scale;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class DuelC extends Controller {

	// View-Elements
	@FXML
	private Label player1Label;
	@FXML
	private Label player2Label;
	@FXML
	private TextField timeP1TF;
	@FXML
	private TextField timeP2TF;
	@FXML
	private TextField solvedExercisesP1TF;
	@FXML
	private TextField solvedExercisesP2TF;
	@FXML
	private ProgressBar timePB;
	@FXML
	private TextField exerciseTF;
	@FXML
	private TextField entryTF;
	@FXML
	private VBox exercisesVBox;
	@FXML
	private Label continuePromptLabel;
	@FXML
	private VBox startBtnVBox;
	@FXML
	private VBox resizingVBox;
	@FXML
	private Label playerWonLabel;
	@FXML
	private VBox playerWonVBox;

	// Scaling
	private double minPaneWidth;
	private double minPaneHeight;
	private Scale paneScale;
	private double aspectRatio;
	private ChangeListener<Number> resizeChangeListener;

	private Stage mainStage;
	private DataModel data;
	private MainMenuC mainMenuC;
	private ArrayList<Exercise> generatedExercises;
	private Exercise currentExercise;
	private int currentPlayer;
	private int round;
	private long resttimePlayer1;
	private long resttimePlayer2;
	private Timer animationTimer;
	private Timer timer;
	private long startTime;
	private long millsToRun;
	private int correctExercisesP1;
	private int correctExercisesP2;
	private int wrongExercisesP1;
	private int wrongExercisesP2;
	private Boolean wrongAnswerP1;
	private Boolean wrongAnswerP2;

	/**
	 * @param data      DataModel
	 * @param mainMenuC Controller der Seite des Hauptmenüs
	 */
	public DuelC(DataModel data, MainMenuC mainMenuC) {
		this.data = data;
		this.mainMenuC = mainMenuC;
	}

	/**
	 * Seite anzeigen
	 */
	public void show() {
		mainStage = data.mainStage;
		setScene(mainStage, "DuelV.fxml");

		mainStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			public void handle(WindowEvent we) {
				onCloseMainStage(we);
			}
		});

		readyUpScaling();

		// Seiteninhalt vorbereiten
		timeP1TF.setText("");
		timeP2TF.setText("");
		solvedExercisesP1TF.setText("");
		solvedExercisesP2TF.setText("");
		exercisesVBox.setVisible(false);
		playerWonVBox.setVisible(false);
		continuePromptLabel.setVisible(false);
		startBtnVBox.setVisible(true);

		resizingVBox.requestFocus();

		player1Label.setText(data.duelPlayer1Name);
		player2Label.setText(data.duelPlayer2Name);

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
		if (data.windowSizes.duelStageX != 0) {
			data.mainStage.setX(data.windowSizes.duelStageX);
			data.mainStage.setY(data.windowSizes.duelStageY);
		}

		// Skalierung der Textfelder
		new ScalingTextField(timeP1TF, 0.1);
		new ScalingTextField(timeP2TF, 0.1);
		new ScalingTextField(solvedExercisesP1TF, 0.1);
		new ScalingTextField(solvedExercisesP2TF, 0.1);
		new ScalingTextField(entryTF, 0.1);
		new ScalingTextField(exerciseTF, 0.1);

		// Skalierung des Fensters
		minPaneWidth = resizingVBox.getWidth();
		minPaneHeight = resizingVBox.getHeight();
		paneScale = new Scale(1, 1);
		resizingVBox.getTransforms().setAll(paneScale);

		resizingVBox.widthProperty().addListener((obs, oldVal, newVal) -> {
			paneScale.setX(newVal.doubleValue() / minPaneWidth);
			paneScale.setPivotX(newVal.doubleValue() / 2);
		});

		resizingVBox.heightProperty().addListener((obs, oldVal, newVal) -> {
			paneScale.setY(newVal.doubleValue() / minPaneHeight);
			paneScale.setPivotY(newVal.doubleValue() / 2);
		});

		// Größenbegrenzung setzen
		data.mainStage.setMinWidth(data.mainStage.getWidth());
		data.mainStage.setMinHeight(data.mainStage.getHeight());
		data.mainStage.setMaxHeight(data.mainStage.getHeight());

		// Resizen mit festgelegtem Seitenverhältnis
		aspectRatio = data.mainStage.getHeight() / data.mainStage.getWidth();
		resizeChangeListener = new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				data.mainStage.setMinHeight(newValue.doubleValue() * aspectRatio);
				data.mainStage.setMaxHeight(newValue.doubleValue() * aspectRatio);
			}
		};

		data.mainStage.widthProperty().addListener(resizeChangeListener);

		// Breite des Fensters setzen (Höhe wird automatisch gesetzt)
		if (data.windowSizes.duelStageWidth > 0)
			data.mainStage.impl_getPeer().setBounds(0, 0, false, false, (float) data.windowSizes.duelStageWidth, 0, -1,
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
	private void entryTF_onAction(ActionEvent event) {
		String input = entryTF.getText().replace(',', '.');

		if (HelpingClass.isFloat(input)) {
			timer.cancel();
			animationTimer.cancel();
			timePB.setProgress(0);

			// Spieler 1
			if (currentPlayer == 0) {
				player1Label.setStyle("-fx-background-color: white");
				player1Label.setTextFill(Paint.valueOf("black"));

				currentPlayer = 1;
				resttimePlayer1 = millsToRun - (System.currentTimeMillis() - startTime);

				// Richtige Lösung
				if (new BigDecimal(input).compareTo(currentExercise.Solution) == 0) {
					correctExercisesP1++;
				} else {
					wrongExercisesP1++;
					wrongAnswerP1 = true;
					resttimePlayer1 -= data.settings.duelTimeReduction * 2000f * wrongExercisesP1;
				}

				solvedExercisesP1TF.setText(correctExercisesP1 + " / " + (round + 1));

				// Zeit in der nächsten Runde von Spieler 1 prüfen
				if (wrongAnswerP1) {
					millsToRun = (long) Math
							.round(resttimePlayer1 - data.settings.duelTimeReduction * 1000f * (round + 1));
				} else {
					millsToRun = (long) Math.round(data.settings.duelBaseTime * 1000 + resttimePlayer1
							- data.settings.duelTimeReduction * 1000f * (round + 1));
				}
				timeP1TF.setText(HelpingClass.generateTimeString(millsToRun));

				// Spieler 1 hat verloren
				if (millsToRun <= 0) {
					exercisesVBox.setVisible(false);
					playerWonLabel.setText("Spieler 2 hat gewonnen!");
					playerWonLabel.setStyle("-fx-background-color: #d64351");
					playerWonVBox.setVisible(true);
					return;
				}

				player2Label.setStyle("-fx-background-color: #d64351");
				player2Label.setTextFill(Paint.valueOf("white"));
				continuePromptLabel.setText("Spieler 2: Leertaste zum Fortfahren drücken");
				if (wrongAnswerP2) {
					wrongAnswerP2 = false;
					millsToRun = (long) Math.round(resttimePlayer2 - data.settings.duelTimeReduction * 1000f * round);
				} else {
					millsToRun = (long) Math.round(data.settings.duelBaseTime * 1000 + resttimePlayer2
							- data.settings.duelTimeReduction * 1000f * round);
				}
			}
			// Spieler 2
			else {
				player2Label.setStyle("-fx-background-color: white");
				player2Label.setTextFill(Paint.valueOf("black"));

				currentPlayer = 0;
				resttimePlayer2 = millsToRun - (System.currentTimeMillis() - startTime);

				// Richtige Lösung
				if (new BigDecimal(input).compareTo(currentExercise.Solution) == 0) {
					correctExercisesP2++;
				} else {
					wrongExercisesP2++;
					wrongAnswerP2 = true;
					resttimePlayer2 -= data.settings.duelTimeReduction * 2000f * wrongExercisesP2;
				}

				solvedExercisesP2TF.setText(correctExercisesP2 + " / " + (round + 1));

				round++;

				// Zeit von Spieler 2 in der nächsten Runde prüfen
				if (wrongAnswerP2) {
					millsToRun = (long) Math.ceil(resttimePlayer2 - data.settings.duelTimeReduction * 1000f * round);
				} else {
					millsToRun = (long) Math.ceil(data.settings.duelBaseTime * 1000 + resttimePlayer2
							- data.settings.duelTimeReduction * 1000f * round);
				}
				timeP2TF.setText(HelpingClass.generateTimeString(millsToRun));

				// Spieler 2 hat verloren
				if (millsToRun <= 0) {
					exercisesVBox.setVisible(false);
					playerWonLabel.setText("Spieler 1 hat gewonnen!");
					playerWonLabel.setStyle("-fx-background-color: #02AAB0");
					playerWonVBox.setVisible(true);

					return;
				}

				player1Label.setStyle("-fx-background-color: #02AAB0");
				player1Label.setTextFill(Paint.valueOf("white"));
				continuePromptLabel.setText("Spieler 1: Leertaste zum Fortfahren drücken");

				if (wrongAnswerP1) {
					wrongAnswerP1 = false;
					millsToRun = (long) Math.ceil(resttimePlayer1 - data.settings.duelTimeReduction * 1000f * round);
				} else {
					millsToRun = (long) Math.ceil(data.settings.duelBaseTime * 1000 + resttimePlayer1
							- data.settings.duelTimeReduction * 1000f * round);
				}
			}

			exercisesVBox.setVisible(false);
			continuePromptLabel.setVisible(true);
			continuePromptLabel.requestFocus();
			entryTF.clear();

			currentExercise = new Exercisegenerator(data).generate();

			for (int i = 0; i < 4; i++) {
				Boolean noDoubles = true;

				for (int a = 0; a < generatedExercises.size(); a++) {
					if (generatedExercises.get(a).CompareExercise(currentExercise)) {
						currentExercise = new Exercisegenerator(data).generate();
						noDoubles = false;
						break;
					}
				}

				if (noDoubles) {
					generatedExercises.add(currentExercise);
					break;
				} else if (i == 3) {
					if (currentPlayer == 0) {
						player1Label.setStyle("-fx-background-color:  #02AAB0");
						player1Label.setTextFill(Paint.valueOf("white"));
					} else {
						player2Label.setStyle("-fx-background-color: white");
						player2Label.setTextFill(Paint.valueOf("black"));
					}
					continuePromptLabel.setVisible(false);
					startBtnVBox.setVisible(true);

					// Alert erstellen und Design bearbeiten
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle(data.applicationName);
					alert.setHeaderText(null);
					alert.setContentText(
							"Es können keine weiteren Aufgaben für die gewählten Einstellungen generiert werden.");
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

					timeP1TF.setText("");
					timeP2TF.setText("");
					solvedExercisesP1TF.setText("");
					solvedExercisesP2TF.setText("");
				}
			}

			exerciseTF.setText(currentExercise.GetTerm());

		}
	}

	@FXML
	private void quitBtn_onAction(ActionEvent event) {
		data.mainStage.widthProperty().removeListener(resizeChangeListener);

		saveStageSizes();

		mainMenuC.show();
	}

	@FXML
	private void settingsBtn_onAction(ActionEvent event) {
		if (!playerWonVBox.isVisible()) {
			if (currentPlayer == 0) {
				player1Label.setStyle("-fx-background-color: white");
				player1Label.setTextFill(Paint.valueOf("black"));
			} else {
				player2Label.setStyle("-fx-background-color: white");
				player2Label.setTextFill(Paint.valueOf("black"));
			}

			startBtnVBox.setVisible(true);
			timeP1TF.setText("");
			timeP2TF.setText("");
			solvedExercisesP1TF.setText("");
			solvedExercisesP2TF.setText("");
			exercisesVBox.setVisible(false);
			continuePromptLabel.setVisible(false);

			if (timer != null) {
				timer.cancel();
				animationTimer.cancel();
			}
		}

		// Einstellungs-Popup-Fenster generieren
		Stage popupStage = createDialogStage(data.mainStage);
		popupStage.getIcons().add(data.appIcon);
		popupStage.setTitle("Einstellungen");
		new SettingsC(data, popupStage).show();
	}

	@FXML
	private void startBtn_onAction(ActionEvent event) {
		startBtnVBox.setVisible(false);
		exercisesVBox.setVisible(false);
		playerWonVBox.setVisible(false);
		continuePromptLabel.setText("Spieler 1: Leertaste zum Fortfahren drücken");
		continuePromptLabel.setVisible(true);
		continuePromptLabel.requestFocus();
		player1Label.setStyle("-fx-background-color: #02AAB0");
		player1Label.setTextFill(Paint.valueOf("white"));
		timePB.setProgress(0);
		timeP1TF.setText(HelpingClass.generateTimeString(data.settings.duelBaseTime * 1000));
		timeP2TF.setText(HelpingClass.generateTimeString(data.settings.duelBaseTime * 1000));
		solvedExercisesP1TF.clear();
		solvedExercisesP2TF.clear();
		entryTF.clear();

		// Erste Aufgabe generieren
		currentPlayer = 0;
		round = 0;
		resttimePlayer1 = 0;
		resttimePlayer2 = 0;
		correctExercisesP1 = 0;
		correctExercisesP2 = 0;
		wrongExercisesP1 = 0;
		wrongExercisesP2 = 0;
		wrongAnswerP1 = false;
		wrongAnswerP2 = false;
		millsToRun = data.settings.duelBaseTime * 1000;
		generatedExercises = new ArrayList<Exercise>();

		currentExercise = new Exercisegenerator(data).generate();
		generatedExercises.add(currentExercise);

		exerciseTF.setText(currentExercise.GetTerm());
		entryTF.setDisable(false);
	}

	@FXML
	private void continuePromptLabel_onKeyTyped(KeyEvent event) {
		if (continuePromptLabel.isVisible() && event.getCharacter().equals(" ")) {
			event.consume();

			continuePromptLabel.setVisible(false);
			exercisesVBox.setVisible(true);

			if (currentPlayer == 0) {
				timeP1TF.clear();
			} else {
				timeP2TF.clear();
			}

			entryTF.requestFocus();

			animationTimer = new Timer();
			timer = new Timer();

			startTime = System.currentTimeMillis();

			timer.schedule(new TimerTask() {

				@Override
				public void run() {
					timer.cancel();
					animationTimer.cancel();

					timePB.setProgress(0);
					exercisesVBox.setVisible(false);

					if (currentPlayer == 0) {
						player1Label.setStyle("-fx-background-color: white");
						player1Label.setTextFill(Paint.valueOf("black"));
						playerWonLabel.setStyle("-fx-background-color: #d64351");

						Platform.runLater(new Runnable() {

							@Override
							public void run() {
								playerWonLabel.setText("Spieler 2 hat gewonnen!");
							}
						});
					} else {
						player2Label.setStyle("-fx-background-color: white");
						player2Label.setTextFill(Paint.valueOf("black"));
						playerWonLabel.setStyle("-fx-background-color: #02AAB0");

						Platform.runLater(new Runnable() {

							@Override
							public void run() {
								playerWonLabel.setText("Spieler 1 hat gewonnen!");
							}
						});
					}
					playerWonVBox.setVisible(true);
				}
			}, millsToRun);

			animationTimer.schedule(new TimerTask() {

				@Override
				public void run() {
					timePB.setProgress((float) (System.currentTimeMillis() - startTime) / (float) millsToRun);
				}
			}, (long) (1000f / 60f), (long) (1000f / 60f));
		}
	}

	// OnClose Event des Hauptfensters
	private void onCloseMainStage(WindowEvent we) {
		data.mainStage.widthProperty().removeListener(resizeChangeListener);
		saveStageSizes();
	}

	private void saveStageSizes() {
		if (!data.windowSizes.maximized && !mainStage.isIconified()) {
			// Stagemaße abspeichern
			data.windowSizes.duelStageWidth = data.mainStage.getWidth();
			data.windowSizes.duelStageX = data.mainStage.getX();
			data.windowSizes.duelStageY = data.mainStage.getY();
		}
	}

	@FXML
	private void player1Label_MouseClicked(MouseEvent e) {
		if (!exercisesVBox.isVisible()) {
			// Dialog zur Benutzernamen Eingabe erstellen
			TextInputDialog dialog = new TextInputDialog();
			dialog.setTitle(data.applicationName);
			dialog.setHeaderText(null);
			dialog.setContentText("Bezeichnung für Spieler 1:");

			dialog.getEditor().setText(data.duelPlayer1Name);
			dialog.getEditor().selectAll();

			// Icon dem Dialog zuweisen
			Stage dialogStage = (Stage) dialog.getDialogPane().getScene().getWindow();
			dialogStage.getIcons().add(data.appIcon);
			// Buttons Style setzen
			for (Node node : ((ButtonBar) dialog.getDialogPane().getChildren().get(2)).getButtons()) {
				((Button) node).getStylesheets().add(data.buttonCss);
			}
			// Dialog zentrieren
			dialogStage.setOnShown(new EventHandler<WindowEvent>() {
				@Override
				public void handle(WindowEvent event) {
					if (mainStage.getX() == -32000)
						dialogStage.setX(
								Screen.getPrimary().getVisualBounds().getWidth() / 2 - dialogStage.getWidth() / 2);
					else
						dialogStage.setX(mainStage.getX() + mainStage.getWidth() / 2 - dialogStage.getWidth() / 2);
					if (mainStage.getY() == -32000)
						dialogStage.setY(
								Screen.getPrimary().getVisualBounds().getHeight() / 2 - dialogStage.getHeight() / 2);
					else
						dialogStage.setY(mainStage.getY() + mainStage.getHeight() / 2 - dialogStage.getHeight() / 2);
				}
			});
			// Text deselektieren
			HelpingClass.deselect(
					((TextField) ((GridPane) dialog.getDialogPane().getChildren().get(3)).getChildren().get(1)));
			// Dialog aufrufen und eingegebenen Nutzernamen abspeichern
			try {
				String playerName = dialog.showAndWait().get();
				if (!HelpingClass.isNullOrBlank(playerName)) {
					player1Label.setText(playerName);
					data.duelPlayer1Name = playerName;
				} else
					return;
			} catch (Exception e1) {
			}
		}
	}

	@FXML
	private void player2Label_MouseClicked(MouseEvent e) {
		if (!exercisesVBox.isVisible()) {
			// Dialog zur Benutzernamen Eingabe erstellen
			TextInputDialog dialog = new TextInputDialog();
			dialog.setTitle(data.applicationName);
			dialog.setHeaderText(null);
			dialog.setContentText("Bezeichnung für Spieler 2:");

			dialog.getEditor().setText(data.duelPlayer2Name);
			dialog.getEditor().selectAll();

			// Icon dem Dialog zuweisen
			Stage dialogStage = (Stage) dialog.getDialogPane().getScene().getWindow();
			dialogStage.getIcons().add(data.appIcon);
			// Buttons Style setzen
			for (Node node : ((ButtonBar) dialog.getDialogPane().getChildren().get(2)).getButtons()) {
				((Button) node).getStylesheets().add(data.buttonCss);
			}
			// Dialog zentrieren
			dialogStage.setOnShown(new EventHandler<WindowEvent>() {
				@Override
				public void handle(WindowEvent event) {
					if (mainStage.getX() == -32000)
						dialogStage.setX(
								Screen.getPrimary().getVisualBounds().getWidth() / 2 - dialogStage.getWidth() / 2);
					else
						dialogStage.setX(mainStage.getX() + mainStage.getWidth() / 2 - dialogStage.getWidth() / 2);
					if (mainStage.getY() == -32000)
						dialogStage.setY(
								Screen.getPrimary().getVisualBounds().getHeight() / 2 - dialogStage.getHeight() / 2);
					else
						dialogStage.setY(mainStage.getY() + mainStage.getHeight() / 2 - dialogStage.getHeight() / 2);
				}
			});
			// Text deselektieren
			HelpingClass.deselect(
					((TextField) ((GridPane) dialog.getDialogPane().getChildren().get(3)).getChildren().get(1)));
			// Dialog aufrufen und eingegebenen Nutzernamen abspeichern
			try {
				String playerName = dialog.showAndWait().get();
				if (!HelpingClass.isNullOrBlank(playerName)) {
					player2Label.setText(playerName);
					data.duelPlayer2Name = playerName;
				} else
					return;
			} catch (Exception e1) {
			}
		}
	}

	@FXML
	private void infoBtn_onAction(ActionEvent event) {
		if (!exercisesVBox.isVisible()) {
			// Alert erstellen und Design bearbeiten
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle(data.applicationName);
			alert.setHeaderText(null);
			alert.setContentText(
					"Du rechnest gegen jemand Anderen gegen die Zeit. Falsch beantwortete Aufgaben kosten dich Zeit, richtige geben dir welche. Bei mehreren falsch beantworteten Aufgaben wird dir auch deutlich mehr Zeit abgezogen."
							+ "\n\nUm die Spannung zu erhöhen bekommt ihr beide auch bei richtiger Lösung ab einem bestimmten Punkt Zeit abgezogen, das Finale hat begonnen."
							+ "\n\nDie Schwierigkeit lässt sich in den Einstellungen anpassen.");
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
			alert.showAndWait();
		}
	}
}
