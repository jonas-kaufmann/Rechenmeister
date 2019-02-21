package application.UI;

import java.math.BigDecimal;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.ResourceBundle;

import application.Controller;
import application.Model.DataModel;
import application.Model.Exercise;
import application.Model.Exercisegenerator;
import application.Model.HelpingClass;
import application.Model.ScalingTextField;
import application.Model.UnsolvedExercise;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.transform.Scale;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * Controller für die Übungsseite
 */
public class PracticeC extends Controller implements Initializable {

	// View
	@FXML
	private AnchorPane anchorPane;
	@FXML
	private TextField exerciseTF;
	@FXML
	private TextField entryTF;
	@FXML
	private TextField resultTF;
	@FXML
	private TextField doneTasksTF;

	// Attribute
	private DataModel data;
	private Exercisegenerator generator;
	private Exercise currentExercise;
	private ArrayList<Exercise> exercises = new ArrayList<Exercise>();
	private MainMenuC mainMenuC;
	private Stage mainStage;
	private double minPaneWidth;
	private double minPaneHeight;
	private Scale paneScale;
	private double aspectRatio;
	private ChangeListener<Number> resizeChangeListener;
	private int solvedTasks;
	private int tasksCount;
	private ArrayList<UnsolvedExercise> unsolvedExercises; // Liste für falsch
															// beantwortete Aufgaben
	private int unsolvedExerciseIndex = -1; // Index der aus der Liste der falschen Aufgaben gerade angezeigte Aufgabe

	/**
	 * @param data      DataModel
	 * @param mainMenuC Controller der Seite des Hauptmenüs
	 */
	public PracticeC(DataModel data, MainMenuC mainMenuC) {
		this.data = data;
		this.mainMenuC = mainMenuC;
		generator = new Exercisegenerator(data);
	}

	/**
	 * Seite anzeigen
	 */
	public void show() {
		mainStage = data.mainStage;
		setScene(mainStage, "PracticeV.fxml");
		readyUpScaling();
		
		mainStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			public void handle(WindowEvent we) {
				onCloseMainStage(we);
			}
		});
		
		//Eingabe im Eingabe-Textfeld beschränken
		entryTF.textProperty().addListener(new ChangeListener<String>() {
		    @Override
		    public void changed(ObservableValue<? extends String> observable,
		            String oldValue, String newValue) {

		        int caretPosition = entryTF.getCaretPosition();
		        
		        if (newValue.length() > 20) {
	        		entryTF.setText(oldValue);
	        		entryTF.positionCaret(caretPosition);
		        } else {
			        for (int i = 0; i < newValue.length(); i++) {
			        	if (!Character.isDigit(newValue.charAt(i)) && newValue.charAt(i) != '.' && newValue.charAt(i) != ',' && newValue.charAt(i) != '-') {
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
		if (data.windowSizes.practiseStageX != 0) {
			mainStage.setX(data.windowSizes.practiseStageX);
			mainStage.setY(data.windowSizes.practiseStageY);
		}

		// Skalierung der Textfelder
		new ScalingTextField(resultTF, 0.1);
		new ScalingTextField(entryTF, 0.1);
		new ScalingTextField(exerciseTF, 0.1);

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
		if (data.windowSizes.practiseStageWidth > 0)
			mainStage.impl_getPeer().setBounds(0, 0, false, false, (float) data.windowSizes.practiseStageWidth, 0, -1,
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
	private void entryTF_onAction(ActionEvent event) {
		String entry = entryTF.getText().replace(',', '.');
		if (HelpingClass.isFloat(entry)) {
			BigDecimal solution = currentExercise.Solution;
			DecimalFormat twoDecimals = new DecimalFormat("0.##");
			resultTF.setText(currentExercise.GetTerm() + twoDecimals.format(solution));
			// Antwort richtig
			if (new BigDecimal(entry).compareTo(solution) == 0) {
				resultTF.setStyle("-fx-background-color: green;");
				solvedTasks += 1;

				// Aufgabe in Liste der Falschen enthalten
				if (unsolvedExerciseIndex != -1) {
					UnsolvedExercise unsolvedExercise = unsolvedExercises.get(unsolvedExerciseIndex);
					unsolvedExercise.training -= 1;

					if (unsolvedExercise.training == 0) {
						unsolvedExercises.remove(unsolvedExerciseIndex);
						unsolvedExerciseIndex = -1;
					}
				}
			}
			// Antwort falsch
			else {
				resultTF.setStyle("-fx-background-color: red;");

				// Aufgabe nicht in der Liste der Falschen enthalten
				if (unsolvedExerciseIndex == -1) {
					// Prüfen, ob Aufgabe schon in Liste der ungelösten vorliegt
					boolean notContained = true;
					for (int i = 0; i < unsolvedExercises.size(); i++) {
						if (unsolvedExercises.get(i).exercise.CompareExercise(currentExercise)) {
							notContained = false;
							break;
						}
					}
					if (notContained) {
						UnsolvedExercise unsolvedExercise = new UnsolvedExercise();
						unsolvedExercise.exercise = currentExercise;
						unsolvedExercise.training = 1;
						unsolvedExercises.add(unsolvedExercise);
					}
				} else {
					unsolvedExercises.get(unsolvedExerciseIndex).training += 1;
					unsolvedExerciseIndex = -1;
				}
			}
			tasksCount += 1;
			doneTasksTF.setText(solvedTasks + " / " + tasksCount);
			entryTF.setText("");
			// Aufgabe aus der Liste der unbeantworteten Aufgaben
			if (Math.random() <= unsolvedExercises.size() / 100d) {
				unsolvedExerciseIndex = (int) Math.round(Math.random() * (unsolvedExercises.size() - 1));
				currentExercise = unsolvedExercises.get(unsolvedExerciseIndex).exercise;
			}
			// neue Aufgabe generieren
			else
				CreateNewExercise();

			exerciseTF.setText(currentExercise.GetTerm());
		}

	}

	@FXML
	private void startBtn_onAction(ActionEvent event) {
		// Textfelder zurücksetzen
		entryTF.setText("");
		entryTF.setDisable(true);
		exerciseTF.setText("");
		resultTF.setText("");
		resultTF.setStyle(null);
		doneTasksTF.setText("");
		solvedTasks = 0;
		tasksCount = 0;
		unsolvedExerciseIndex = -1;
		exercises = new ArrayList<Exercise>();

		CreateNewExercise();
		unsolvedExercises = new ArrayList<UnsolvedExercise>();
		exerciseTF.setText(currentExercise.GetTerm());
		entryTF.setDisable(false);
		entryTF.requestFocus();
	}

	@FXML
	private void settingsBtn_onAction(ActionEvent event) {
		// Textfelder zurücksetzen
		entryTF.setText("");
		entryTF.setDisable(true);
		exerciseTF.setText("");
		resultTF.setText("");
		resultTF.setStyle(null);
		doneTasksTF.setText("");
		unsolvedExercises = new ArrayList<UnsolvedExercise>();
		unsolvedExerciseIndex = -1;
		// Einstellungs-Popup-Fenster generieren
		Stage popupStage = createDialogStage(data.mainStage);
		popupStage.getIcons().add(data.appIcon);
		popupStage.setTitle("Einstellungen");
		(new SettingsC(data, popupStage)).show();
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

	private void CreateNewExercise() {
		// Solange neue Aufgaben generieren bis der neue Term nicht gleich dem der alten
		// Aufgabe ist oder bis fünf Mal die selben Aufgaben nacheinander generiert
		// wurden
		int counter = 0;
		boolean successfull = false;

		while (!successfull && counter <= 5) {
			currentExercise = generator.generate();
			counter++;
			successfull = true;
			for (int i = 0; i < exercises.size(); i++) {
				if (exercises.get(i).CompareExercise(currentExercise)) {			
					successfull = false;
					break;
				}
			}
		}
		if(counter > 5) {
			// 2 gleiche Aufgaben hintereinander ausschließen falls alle einmal gestellt wurden
			counter = 0;
			while(currentExercise.CompareExercise(exercises.get(exercises.size()-1)) && counter <= 5) {
				currentExercise = generator.generate();
				counter++;
			}
		}
		exercises.add(currentExercise);
	}
	
	private void saveStageSizes() {
		if (!data.windowSizes.maximized && !mainStage.isIconified()) {
			// Stagemaße abspeichern
			data.windowSizes.practiseStageWidth = mainStage.getWidth();
			data.windowSizes.practiseStageX = mainStage.getX();
			data.windowSizes.practiseStageY = mainStage.getY();
		}
	}
}
