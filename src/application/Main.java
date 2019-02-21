package application;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Optional;

import application.Model.DataModel;
import application.UI.MainMenuC;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Main extends Application {

	private DataModel Data;
	public ServerSocket appSocket;
	private ProgramRunningSocketThread programRunningSocketThread;

	/**
	 * Initialisierung der Anwendung
	 * 
	 * @param mainStage erstes Fenster, welches bei der Initialisierung erzeugt
	 *                  wurde
	 */
	@Override
	@Deprecated
	public void start(Stage mainStage) {
		Image icon = new Image(getClass().getResourceAsStream("Resources/icon.png"));
		String buttonCss = (getClass().getResource("UI/Buttons.css")).toExternalForm();
		Font.loadFont(getClass().getResourceAsStream("Resources/Teko.ttf"), 1);
		Font.loadFont(getClass().getResourceAsStream("Resources/Syncopate.ttf"), 1);
		Data = new DataModel(this, mainStage, icon, buttonCss); // DataModel initialisieren
		try {
			appSocket = new ServerSocket(556);
		} catch (IOException e) {
			// Alert erstellen und Design bearbeiten
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle(Data.applicationName);
			alert.setHeaderText(null);
			alert.setContentText(
					"Anwendung läuft bereits, das Starten einer weiteren Instanz kann zu Problemen führen. Fortfahren?");

			// Buttons dem Alertfenster hinzufügen
			alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

			// Buttons Style setzen
			for(Node node : ((ButtonBar) alert.getDialogPane().getChildren().get(2)).getButtons()) {
				((Button) node).getStylesheets().add(Data.buttonCss);
			}
			
			// Icon dem Alertfenster zuweisen
			Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
			alertStage.getIcons().add(icon);

			// Zeigen des Fensters -> Auswerten des gedrückten Buttons
			Optional<ButtonType> result = alert.showAndWait();

			if (result.get() != ButtonType.YES) {
				System.exit(0);
				return;
			}

			programRunningSocketThread = new ProgramRunningSocketThread(this);
			programRunningSocketThread.start();
		}
		
		mainStage.setTitle(Data.applicationName);
		mainStage.getIcons().add(icon);

		
		if (Data.windowSizes.maximized)
			mainStage.setMaximized(true);

		// Maximieren
		mainStage.maximizedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
				Data.windowSizes.maximized = !Data.windowSizes.maximized;
				if (Data.windowSizes.maximized) {
					Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
					double scalingFactorY = screenBounds.getHeight() / mainStage.getHeight();
					double scalingX = mainStage.getWidth() * scalingFactorY;
					if (scalingX <= screenBounds.getWidth())
						mainStage.impl_getPeer().setBounds(0, 0, false, false, (float) scalingX, 0, -1, -1, 0, 0);
					else
						mainStage.impl_getPeer().setBounds(0, 0, false, false, (float) screenBounds.getWidth(), 0, -1,
								-1, 0, 0);
					mainStage.setX(screenBounds.getWidth() / 2 - mainStage.getWidth() / 2);
					mainStage.setY(screenBounds.getHeight() / 2 - mainStage.getHeight() / 2);
				}
			}
		});

		// Kein Kommentar
		mainStage.iconifiedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (Data.windowSizes.maximized && !newValue) {
					Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
					double scalingFactorY = screenBounds.getHeight() / mainStage.getHeight();
					double scalingX = mainStage.getWidth() * scalingFactorY;
					if (scalingX <= screenBounds.getWidth())
						mainStage.impl_getPeer().setBounds(0, 0, false, false, (float) scalingX, 0, -1, -1, 0, 0);
					else
						mainStage.impl_getPeer().setBounds(0, 0, false, false, (float) screenBounds.getWidth(), 0, -1,
								-1, 0, 0);
					mainStage.setX(screenBounds.getWidth() / 2 - mainStage.getWidth() / 2);
					mainStage.setY(screenBounds.getHeight() / 2 - mainStage.getHeight() / 2);
				}
			}
		});

		MainMenuC mainMenuC;
		(mainMenuC = new MainMenuC(Data)).show(); // Controller laden

		mainStage.show(); // Fenster anzeigen
		mainMenuC.readyUpScaling();
	}

	/**
	 * Schließt beim Beenden des Programms den ProgrammRunningSocketThread.
	 */
	@Override
	@Deprecated
	public void stop() {
		Data.SaveToJSON();

		if (programRunningSocketThread != null && programRunningSocketThread.isAlive())
			programRunningSocketThread.stop();
		if (appSocket != null) {
			try {
				appSocket.close();
			} catch (IOException e) {
			}
		}

		System.exit(0);
	}

	/**
	 * Methode, die beim Programmstart ausgeführt wird
	 * 
	 * @param args Startparameter, welche beim Programmstart übergeben wurden
	 */
	public static void main(String[] args) {
		launch();
	}
}
