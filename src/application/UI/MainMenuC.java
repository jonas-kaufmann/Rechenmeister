package application.UI;

import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import application.Controller;
import application.Model.DataModel;
import application.Model.HelpingClass;
import application.Model.ScalingTextField;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.transform.Scale;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.control.ButtonBar;

/**
 * Controller für die Hauptmenüseite
 */
public class MainMenuC extends Controller implements Initializable {

	// View
	@FXML
	private AnchorPane anchorPane;
	@FXML
	private TextField usernameTF;

	// Attribute
	private DataModel data;
	private Stage mainStage;
	private double minPaneWidth;
	private double minPaneHeight;
	private Scale paneScale;
	private double aspectRatio;
	private ChangeListener<Number> resizeChangeListener;
	private TextInputDialog competitionDialog;

	/**
	 * @param data DataModel
	 */
	public MainMenuC(DataModel data) {
		this.data = data;
	}

	/**
	 * Seite anzeigen
	 */
	public void show() {
		mainStage = data.mainStage;
		if (getScene() == null) {
			setScene(mainStage, "MainMenuV.fxml");
			checkJavaVersion();
		} else {
			setScene(data.mainStage, getScene());
			readyUpScaling();
		}
		mainStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			public void handle(WindowEvent we) {
				onCloseMainStage(we);
			}
		});
		usernameTF.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {

				int caretPosition = usernameTF.getCaretPosition();
				if (newValue.length() > 20 || newValue.contains(";")) {
					usernameTF.setText(oldValue);
					usernameTF.positionCaret(caretPosition);
				}
			}
		});
	}

	/**
	 * Bereitet die Skalierung des Fenster vor
	 */
	@SuppressWarnings("deprecation")
	public void readyUpScaling() {
		// Nutzernamen setzen
		if (!data.username.isEmpty() && data.username.length() <= 20 && !data.username.contains(";"))
			usernameTF.setText(data.username);
		else
			data.username = "";

		// Position des Fensters setzen
		if (data.windowSizes.mainMenuStageX != 0) {
			mainStage.setX(data.windowSizes.mainMenuStageX);
			mainStage.setY(data.windowSizes.mainMenuStageY);
		}

		// Skalierung der Textfelder
		new ScalingTextField(usernameTF, 0.1);

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
		if (data.windowSizes.mainMenuStageWidth > 0)
			mainStage.impl_getPeer().setBounds(0, 0, false, false, (float) data.windowSizes.mainMenuStageWidth, 0, -1,
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

	/**
	 * Methode wird aufgerufen, sobald die View geladen wird
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
	}

	@FXML
	private void trainingBtn_onAction(ActionEvent event) {
		data.username = usernameTF.getText();
		mainStage.widthProperty().removeListener(resizeChangeListener);
		saveStageSizes();
		(new PracticeC(data, this)).show();
	}

	@FXML
	private void competitionBtn_onAction(ActionEvent event) {
		// Alert erstellen und Design bearbeiten
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle(data.applicationName);
		alert.setHeaderText(null);
		alert.setContentText("Willst Du einen Wettkampf erstellen oder einem vorhandenen beitreten?");
		// Buttons dem Alertfenster hinzufügen
		ButtonType buttonTypeHost = new ButtonType("Erstellen");
		ButtonType buttonTypeJoin = new ButtonType("Beitreten");
		ButtonType buttonTypeCancel = new ButtonType("Abbrechen");
		alert.getButtonTypes().setAll(buttonTypeHost, buttonTypeJoin, buttonTypeCancel);
		// Buttons Style setzen
		for (Node node : ((ButtonBar) alert.getDialogPane().getChildren().get(2)).getButtons()) {
			((Button) node).getStylesheets().add(data.buttonCss);
		}
		// Icon dem Alertfenster zuweisen
		Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
		alertStage.getIcons().add(data.appIcon);
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
		// Zeigen des Fensters -> Auswerten des gedrückten Buttons
		Optional<ButtonType> result = alert.showAndWait();
		data.username = usernameTF.getText();
		if (result.get() == buttonTypeHost) {
			saveStageSizes();
			(new HostCompetitionC(data, this, this::handleConnected)).show();
		} else if (result.get() == buttonTypeJoin) {
			if (HelpingClass.isNullOrBlank(data.username)) {
				// Dialog zur Benutzernamen Eingabe erstellen
				TextInputDialog dialog = new TextInputDialog();
				dialog.setTitle(data.applicationName);
				dialog.setHeaderText(null);
				dialog.setContentText("Benutzername:");
				// Icon dem Dialog zuweisen
				Stage dialogStage = (Stage) dialog.getDialogPane().getScene().getWindow();
				dialogStage.getIcons().add(data.appIcon);
				// Zeicheneingabe überprüfen
				TextField usernameDialogTF = ((TextField) ((GridPane) dialog.getDialogPane().getChildren().get(3))
						.getChildren().get(1));
				usernameDialogTF.textProperty().addListener(new ChangeListener<String>() {
					@Override
					public void changed(ObservableValue<? extends String> observable, String oldValue,
							String newValue) {

						int caretPosition = usernameDialogTF.getCaretPosition();
						if (newValue.length() > 20 || newValue.contains(";")) {
							usernameDialogTF.setText(oldValue);
							usernameDialogTF.positionCaret(caretPosition);
						}
					}
				});
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
							dialogStage.setY(Screen.getPrimary().getVisualBounds().getHeight() / 2
									- dialogStage.getHeight() / 2);
						else
							dialogStage
									.setY(mainStage.getY() + mainStage.getHeight() / 2 - dialogStage.getHeight() / 2);
					}
				});
				// Dialog aufrufen und eingegebenen Nutzernamen abspeichern
				try {
					String username = dialog.showAndWait().get();
					if (!HelpingClass.isNullOrBlank(username) && !username.contains(";") && username.length() <= 20) {
						usernameTF.setText(username);
						data.username = username;
					} else
						return;
				} catch (NoSuchElementException e) {
					return;
				}
			}
			// Dialog zur IP Eingabe erstellen
			try {
				try {
					String[] split = InetAddress.getLocalHost().getHostAddress().toString().split("[.]");
					competitionDialog = new TextInputDialog(split[0] + "." + split[1] + "." + split[2] + ".");
				} catch (UnknownHostException ex) {
					competitionDialog = new TextInputDialog();
				}
				competitionDialog.setTitle(data.applicationName);
				competitionDialog.setHeaderText(null);
				competitionDialog.setContentText("Wettkampfadresse:");
				// Icon dem Dialog zuweisen
				Stage dialogStage = (Stage) competitionDialog.getDialogPane().getScene().getWindow();
				dialogStage.getIcons().add(data.appIcon);
				// Buttons Style setzen
				for (Node node : ((ButtonBar) competitionDialog.getDialogPane().getChildren().get(2)).getButtons()) {
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
							dialogStage.setY(Screen.getPrimary().getVisualBounds().getHeight() / 2
									- dialogStage.getHeight() / 2);
						else
							dialogStage
									.setY(mainStage.getY() + mainStage.getHeight() / 2 - dialogStage.getHeight() / 2);
					}
				});
				// Ip Textfeld Text deselektieren
				HelpingClass.deselect(((TextField) ((GridPane) competitionDialog.getDialogPane().getChildren().get(3))
						.getChildren().get(1)));
				// Dialog aufrufen und eingegebene IP abspeichern
				String ipInput = competitionDialog.showAndWait().get();
				saveStageSizes();
				(new CompetitionC(data, ipInput, data.PORT, data.username, this, this::handleConnected,
						this::handleConnectionError)).show();
			} catch (NoSuchElementException e) {
			}
		}
	}

	private void handleConnected() {
		mainStage.widthProperty().removeListener(resizeChangeListener);
	}

	private void handleConnectionError() {
		// Dialog aufrufen und eingegebene IP abspeichern
		String ipInput = competitionDialog.showAndWait().get();
		(new CompetitionC(data, ipInput, data.PORT, data.username, this, this::handleConnected,
				this::handleConnectionError)).show();
	}

	@FXML
	private void duelBtn_onAction(ActionEvent event) {
		data.username = usernameTF.getText();
		mainStage.widthProperty().removeListener(resizeChangeListener);
		saveStageSizes();
		new DuelC(data, this).show();
	}

	// OnClose Event des Hauptfensters
	private void onCloseMainStage(WindowEvent we) {
		saveStageSizes();
	}

	@FXML
	private void settingsBtn_onAction(ActionEvent event) {
		data.username = usernameTF.getText();
		// Einstellungs-Popup-Fenster generieren
		Stage popupStage = createDialogStage(data.mainStage);
		popupStage.getIcons().add(data.appIcon);
		popupStage.setTitle("Einstellungen");
		(new SettingsC(data, popupStage)).show();
	}

	private void saveStageSizes() {
		if (!data.windowSizes.maximized && !mainStage.isIconified()) {
			// Stagemaße abspeichern
			data.windowSizes.mainMenuStageWidth = mainStage.getWidth();
			data.windowSizes.mainMenuStageX = mainStage.getX();
			data.windowSizes.mainMenuStageY = mainStage.getY();
		}
	}

	@FXML
	private void creditsHyperlink_onAction(ActionEvent e) {
		data.username = usernameTF.getText();
		// Information-Popup-Fenster generieren
		Stage popupStage = createDialogStage(data.mainStage);
		popupStage.getIcons().add(data.appIcon);
		popupStage.setTitle("Information");
		(new InformationC(data, popupStage)).show();
	}

	// Java-Version überprüfen
	private void checkJavaVersion() {
		try {
			String split;
			if (HelpingClass.isInteger(
					split = System.getProperty("java.vm.version").split(Pattern.quote("."))[1].split("-")[0])) {
				int version = Integer.parseInt(split);
				if (version != data.settings.lastCheckedJavaVersion) {
					data.settings.checkJavaVersion = true;
					data.settings.lastCheckedJavaVersion = version;
				}
				if (data.settings.checkJavaVersion) {
					if (version < data.minimalJavaVersion) {
						// Alert erstellen und Design bearbeiten
						Alert alert = new Alert(AlertType.CONFIRMATION);
						alert.setTitle(data.applicationName);
						alert.setHeaderText(null);
						alert.setContentText(
								"Java-Version ist veraltet.\nOhne Update können Fehler des Programms entstehen.\n\n");
						Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
						alertStage.getIcons().add(data.appIcon);
						// Buttons dem Alertfenster hinzufügen
						ButtonType buttonTypeOk = new ButtonType("Ok");
						ButtonType buttonTypeDoNotShowAgain = new ButtonType("Meldung nicht mehr anzeigen");
						alert.getButtonTypes().setAll(buttonTypeOk, buttonTypeDoNotShowAgain);
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
									alertStage.setX(
											mainStage.getX() + mainStage.getWidth() / 2 - alertStage.getWidth() / 2);
								if (mainStage.getY() == -32000)
									alertStage.setY(Screen.getPrimary().getVisualBounds().getHeight() / 2
											- alertStage.getHeight() / 2);
								else
									alertStage.setY(
											mainStage.getY() + mainStage.getHeight() / 2 - alertStage.getHeight() / 2);
							}
						});
						// Zeigen des Fensters -> Auswerten des gedrückten Buttons
						Optional<ButtonType> result = alert.showAndWait();
						if (result.get() == buttonTypeDoNotShowAgain)
							data.settings.checkJavaVersion = false;
					}
				}
			} else
				System.out.println("Java-Version konnte nicht überprüft werden. (Int-Casting fehlgeschlagen)");
		} catch (Exception e) {
			System.out.println("Java-Version konnte nicht überprüft werden. (Exception: " + e.toString() + ")");
		}
	}
}
