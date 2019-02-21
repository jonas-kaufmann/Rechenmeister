package application.UI;

import java.net.URL;
import java.util.ResourceBundle;

import application.Controller;
import application.Model.CommunicationClient;
import application.Model.DataModel;
import application.Model.StatisticExercise;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.Button;
import javafx.scene.transform.Scale;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

/**
 * Controller für die Wettbewerbsseite des Clients
 */
public class CompetitionStatisticsC extends Controller implements Initializable {

	// View
	@FXML
	private AnchorPane anchorPane;
	@FXML
	private Button quitBtn;
	@FXML
	private TableView<StatisticExercise> statisticsTV;
	@FXML
	private TableColumn<StatisticExercise, String> termColumn;
	@FXML
	private TableColumn<StatisticExercise, String> inputSolutionColumn;
	@FXML
	private Label sumUpLabel;
	@FXML
	private Label scoreTxt;

	// Attribute
	private DataModel data;
	private MainMenuC mainMenuC;
	private Stage mainStage;
	private double minPaneWidth;
	private double minPaneHeight;
	private Scale paneScale;
	private double aspectRatio;
	private ChangeListener<Number> resizeChangeListener;
	private ObservableList<StatisticExercise> statisticExercises;
	private Timeline timeline;
	private int secondsToWait; // Zeit die gewartet werden soll bis das Statistikenfenster geschlossen werden
								// kann
	private CommunicationClient client;
	private int score;
	private int correctlySolved;

	/**
	 * @param data DataModel
	 * @param mainMenuC Controller der Seite des Hauptmenüs
	 * @param statisticExercises Liste der anzuzeigenden Statistikaufgaben
	 * @param secondsToWait Zeit die gewartet werden soll bis Schließen des Fensters möglich
	 * @param client zu schließender Client
	 * @param score Erreichter Score
	 * @param correctlySolved Anzahl korrekt gelöster Aufgaben
	 */
	public CompetitionStatisticsC(DataModel data, MainMenuC mainMenuC,
			ObservableList<StatisticExercise> statisticExercises, int secondsToWait, CommunicationClient client,
			int score, int correctlySolved) {
		this.data = data;
		this.mainMenuC = mainMenuC;
		this.statisticExercises = statisticExercises;
		this.secondsToWait = secondsToWait;
		this.client = client;
		this.score = score;
		this.correctlySolved = correctlySolved;
	}

	/**
	 * Seite anzeigen
	 */
	public void show() {
		mainStage = data.mainStage;
		setScene(mainStage, "CompetitionStatisticsV.fxml");
		// OnClose Event hinzufügen
		mainStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			public void handle(WindowEvent we) {
				onCloseMainStage(we);
			}
		});
		// Tabellenfärbung vorbereiten
		termColumn.setCellFactory(column -> {
			return new TableCell<StatisticExercise, String>() {
				@Override
				protected void updateItem(String item, boolean empty) {
					super.updateItem(item, empty);
					if (item == null || empty)
						setStyle("");
					else {
						setText(item);
						StatisticExercise statisticExercise = getTableView().getItems().get(getIndex());
						if (statisticExercise.getCorrect())
							setStyle("-fx-background-color: rgba(0, 128, 0, 0.85)");
						else
							setStyle("-fx-background-color: rgba(255, 0, 0, 0.85)");
					}
				}
			};
		});
		inputSolutionColumn.setCellFactory(column -> {
			return new TableCell<StatisticExercise, String>() {
				@Override
				protected void updateItem(String item, boolean empty) {
					super.updateItem(item, empty);
					if (item == null || empty)
						setStyle("");
					else {
						setText(item);
						StatisticExercise statisticExercise = getTableView().getItems().get(getIndex());
						if (statisticExercise.getCorrect())
							setStyle("-fx-background-color: rgba(0, 128, 0, 0.85)");
						else
							setStyle("-fx-background-color: rgba(255, 0, 0, 0.85)");
					}
				}
			};
		});
		// Statistiken anzeigen
		sumUpLabel.setText(correctlySolved + " von " + statisticExercises.size() + " Aufgaben richtig beantwortet");
		statisticsTV.setItems(statisticExercises);
		scoreTxt.setText(scoreTxt.getText() + " " + score);
		// Timeline starten der nach vorgegebener Zeit das Schließen des Fensters
		// freigibt
		timeline = new Timeline(new KeyFrame(Duration.seconds(secondsToWait), e -> {
			timeline.stop();
			client.quit();
			client.close();
			quitBtn.setDisable(false);
		}));
		timeline.setCycleCount(1);
		timeline.play();
		readyUpScaling();
	}

	/**
	 * Bereitet die Skalierung des Fenster vor
	 */
	@SuppressWarnings("deprecation")
	public void readyUpScaling() {
		// Position des Fensters setzen
		if (data.windowSizes.competitionStatisticsStageX != 0) {
			mainStage.setX(data.windowSizes.competitionStatisticsStageX);
			mainStage.setY(data.windowSizes.competitionStatisticsStageY);
		}

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
		if (data.windowSizes.competitionStatisticsWidth > 0)
			mainStage.impl_getPeer().setBounds(0, 0, false, false, (float) data.windowSizes.competitionStatisticsWidth,
					0, -1, -1, 0, 0);

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
	private void quitBtn_onAction(ActionEvent event) {
		// MainMenu aufrufen
		mainStage.widthProperty().removeListener(resizeChangeListener);
		saveStageSizes();
		mainMenuC.show();
	}

	// OnClose Event des Hauptfensters
	private void onCloseMainStage(WindowEvent we) {
		saveStageSizes();
	}

	private void saveStageSizes() {
		if (!data.windowSizes.maximized && !mainStage.isIconified()) {
			// Stagemaße abspeichern
			data.windowSizes.competitionStatisticsWidth = mainStage.getWidth();
			data.windowSizes.competitionStatisticsStageX = mainStage.getX();
			data.windowSizes.competitionStatisticsStageY = mainStage.getY();
		}
	}
}
