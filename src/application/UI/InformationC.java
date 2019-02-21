package application.UI;

import java.net.URL;
import java.util.ResourceBundle;

import application.Controller;
import application.Model.DataModel;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class InformationC extends Controller implements Initializable {

	@FXML
	private VBox rootVBox;

	private ChangeListener<Number> resizeChangeListener;
	private Double minPaneWidth;
	private Double minPaneHeight;
	private Scale paneScale;
	private Double aspectRatio;

	private DataModel data;
	private Stage stage;

	/**
	 * @param data DataModel
	 * @param stage Popupfenster
	 */
	public InformationC(DataModel data, Stage stage) {
		this.data = data;
		this.stage = stage;
	}

	/**
	 * Seite anzeigen
	 */
	public void show() {
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			public void handle(WindowEvent we) {
				saveStageSizes();
			}
		});
		setScene(stage, "InformationV.fxml");
		// Popupstage zentrieren falls nicht abgespeichert
		Stage mainStage = data.mainStage;
		if (data.windowSizes.informationStageX == 0)
			stage.setOnShown(new EventHandler<WindowEvent>() {
				@Override
				public void handle(WindowEvent event) {
					double y = mainStage.getY() + mainStage.getHeight() / 2 - stage.getHeight() / 2;

					if (y < 0) {
						y = 0;
					}

					stage.setX(mainStage.getX() + mainStage.getWidth() / 2 - stage.getWidth() / 2);
					stage.setY(y);
				}
			});
		// Popupfenster zeigt sich; Hauptfenster kann nicht mehr bedient werden während
		// Popupfenster offen ist
		stage.show();
		readyUpScaling();
	}

	/**
	 * Bereitet die Skalierung des Fenster vor
	 */
	@SuppressWarnings("deprecation")
	public void readyUpScaling() {
		// Position des Fensters setzen
		if (data.windowSizes.informationStageX != 0) {
			stage.setX(data.windowSizes.informationStageX);
			stage.setY(data.windowSizes.informationStageY);
		}

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
		stage.setMinWidth(stage.getWidth());
		stage.setMinHeight(stage.getHeight());
		stage.setMaxHeight(stage.getHeight());

		// Resizen mit festgelegtem Seitenverhältnis
		aspectRatio = stage.getHeight() / stage.getWidth();
		resizeChangeListener = new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				stage.setMinHeight(newValue.doubleValue() * aspectRatio);
				stage.setMaxHeight(newValue.doubleValue() * aspectRatio);
			}
		};
		stage.widthProperty().addListener(resizeChangeListener);

		// Breite des Fensters setzen (Höhe wird automatisch gesetzt)
		if (data.windowSizes.informationStageWidth > 0)
			stage.impl_getPeer().setBounds(0, 0, false, false, (float) data.windowSizes.informationStageWidth, 0, -1, -1,
					0, 0);
	}

	/**
	 * Methode wird aufgerufen, sobald die View geladen wird
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
	}

	@FXML
	private void closeBtn_onAction(ActionEvent event) {
		saveStageSizes();
		stage.close();
	}
	
	@FXML
	private void licenseHyperlink_onAction(ActionEvent e) {
		data.application.getHostServices().showDocument("https://creativecommons.org/licenses/by-sa/4.0/");
	}
	
	@FXML
	private void gsonHyperlink_onAction(ActionEvent e) {
		data.application.getHostServices().showDocument("https://github.com/google/gson");
	}

	private void saveStageSizes() {
		if (!data.windowSizes.maximized) {
			// Stagemaße abspeichern
			data.windowSizes.informationStageWidth = stage.getWidth();
			data.windowSizes.informationStageX = stage.getX();
			data.windowSizes.informationStageY = stage.getY();
		}
	}

}