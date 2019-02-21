package application;

import java.io.IOException;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Erbbare Basisklasse mit Methoden für alle Controller
 */
public abstract class Controller {

	private Scene scene;

	/**
	 * Lädt die übergebene View auf eine neu erzeugte Seite und weist diese dem
	 * übergebenen Fenster zu
	 * 
	 * @param stage    Fenster
	 * @param fxmlFile Pfad zur FXML Datei (View)
	 */
	protected void setScene(Stage stage, String fxmlFile) {
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlFile));
			fxmlLoader.setController(this);
			scene = new Scene(fxmlLoader.load());

			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			
			stage.setMinWidth(0);
			stage.setMinHeight(0);
			stage.setMaxHeight(4000);
			stage.setScene(scene);
			stage.sizeToScene();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Lädt die übergebene Seite auf das übergebene Fenster
	 * 
	 * @param stage Fenster
	 * @param scene Seite
	 */
	protected void setScene(Stage stage, Scene scene) {
		this.scene = scene;
		stage.setMinWidth(0);
		stage.setMinHeight(0);
		stage.setMaxHeight(4000);
		stage.setScene(scene);
		stage.sizeToScene();
	}

	/**
	 * Erzeugt ein neues Fenster mit dem übergebenen Titel
	 * 
	 * @param title Titel des Fensters
	 * @return erzeugtes Fenster
	 */
	protected Stage createStage(String title) {
		Stage stage = new Stage();
		stage.setTitle(title);
		return stage;
	}

	/**
	 * Erzeugung eines Dialogfensters, welches das Ownerfenster beim Anzeigen
	 * blockiert
	 * 
	 * @param owner Fenster, zu welchem der Dialog gehört
	 * @return Erzeugter Dialog
	 */
	protected Stage createDialogStage(Stage owner) {
		Stage stage = new Stage();
		stage.initOwner(owner);
		stage.initModality(Modality.WINDOW_MODAL);

		// Maximieren unterbinden
		stage.maximizedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
				stage.setMaximized(false);
			}
		});

		return stage;
	}

	/**
	 * @return aktuelle Szene
	 */
	public Scene getScene() {
		return scene;
	}
}
