package application.UI;

import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

import application.Controller;
import application.Model.DataModel;
import application.Model.HelpingClass;
import application.Model.Settings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class SettingsC extends Controller implements Initializable {

	@FXML
	private VBox rootVBox;
	@FXML
	private HBox presetsHBox;
	@FXML
	private HBox wholeNumbersHBox;
	@FXML
	private HBox decimalsHBox;
	@FXML
	private HBox powerOfTwoHBox;
	@FXML
	private HBox moreOperationsHBox;
	@FXML
	private HBox duelTimeHBox;
	@FXML
	private Button applyBtn;

	@FXML
	private TextField wholeNumbersTF;
	@FXML
	private CheckBox wholeNumbersCB;
	@FXML
	private CheckBox wholeNumbersAdditionCbx;
	@FXML
	private CheckBox wholeNumbersMultiplicationCbx;
	@FXML
	private CheckBox wholeNumbersSubtractionCbx;
	@FXML
	private CheckBox wholeNumbersDivisionCbx;

	@FXML
	private TextField decimalsTF;
	@FXML
	private CheckBox decimalsCB;
	@FXML
	private CheckBox decimalsAdditionCbx;
	@FXML
	private CheckBox decimalsMultiplicationCbx;
	@FXML
	private CheckBox decimalsSubtractionCbx;
	@FXML
	private CheckBox decimalsDivisionCbx;

	@FXML
	private TextField powerOfTwoTF;
	@FXML
	private CheckBox powerOfTwoCbx;

	@FXML
	private TextField moreOperationsTF;
	@FXML
	private CheckBox squareNumbersCbx;
	@FXML
	private CheckBox squareRootsCbx;
	@FXML
	private CheckBox cubeRootsCbx;

	@FXML
	private TextField duelBaseTimeTF;
	@FXML
	private TextField duelTimeReductionTF;

	private Double minPaneWidth;
	private Double minPaneHeight;
	private Scale paneScale;
	private Double aspectRatio;
	private ChangeListener<Number> resizeChangeListener;
	private TextField[] textFieldArray;

	private DataModel data;
	private Stage stage;

	private Boolean editable;

	/**
	 * Einstellungen bearbeitbar
	 * 
	 * @param data DataModel
	 * @param stage Popupfenster
	 */
	public SettingsC(DataModel data, Stage stage) {
		this.data = data;
		this.stage = stage;
		editable = true;
	}

	/**
	 * Einstellungen nicht bearbeitbar
	 * 
	 * @param data DataModel
	 * @param settings Settings-Objekt
	 * @param stage Popupfenster
	 */
	public SettingsC(DataModel data, Settings settings, Stage stage) {
		this.data = data;
		this.stage = stage;
		editable = false;
	}

	/**
	 * Seite anzeigen
	 */
	public void show() {
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			public void handle(WindowEvent we) {
				onCloseStage(we);
			}
		});
		setScene(stage, "SettingsV.fxml");
		// Popupstage zentrieren falls nicht abgespeichert
		Stage mainStage = data.mainStage;
		if (data.windowSizes.settingsStageX == 0)
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
		// Eingabesperre der Textfelder
		duelBaseTimeTF.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				int caretPosition = duelBaseTimeTF.getCaretPosition();
				if (!(newValue.isEmpty() || (HelpingClass.isInteger(newValue) && Integer.parseInt(newValue) > 0))) {
					duelBaseTimeTF.setText(oldValue);
					duelBaseTimeTF.positionCaret(caretPosition);
				}
			}
		});
		decimalsTF.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				int caretPosition = decimalsTF.getCaretPosition();
				int parse;
				if (!(newValue.isEmpty() || (HelpingClass.isInteger(newValue)
						&& (parse = Integer.parseInt(newValue)) > 0 && parse <= 107374182))) {
					decimalsTF.setText(oldValue);
					decimalsTF.positionCaret(caretPosition);
				}
			}
		});
		textFieldArray = new TextField[] { moreOperationsTF, powerOfTwoTF, wholeNumbersTF };
		for (TextField tf : textFieldArray) {
			tf.textProperty().addListener(new ChangeListener<String>() {
				@Override
				public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
					int caretPosition = tf.getCaretPosition();
					int parse;
					if (!(newValue.isEmpty() || (HelpingClass.isInteger(newValue)
							&& (parse = Integer.parseInt(newValue)) > 0 && parse <= 1073741823))) {
						tf.setText(oldValue);
						tf.positionCaret(caretPosition);
					}
				}
			});
		}
		duelTimeReductionTF.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				int caretPosition = duelTimeReductionTF.getCaretPosition();
				if (!(newValue.isEmpty() || HelpingClass.isFloat(newValue.replace(',', '.')))) {
					duelTimeReductionTF.setText(oldValue);
					duelTimeReductionTF.positionCaret(caretPosition);
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
		if (data.windowSizes.settingsStageX != 0) {
			stage.setX(data.windowSizes.settingsStageX);
			stage.setY(data.windowSizes.settingsStageY);
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
		if (data.windowSizes.settingsStageWidth > 0)
			stage.impl_getPeer().setBounds(0, 0, false, false, (float) data.windowSizes.settingsStageWidth, 0, -1, -1,
					0, 0);
	}

	/**
	 * Methode wird aufgerufen, sobald die View geladen wird
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// Falls editable false Textfelder und Checkboxen deaktivieren
		if (!editable) {
			presetsHBox.setDisable(true);
			wholeNumbersHBox.setDisable(true);
			decimalsHBox.setDisable(true);
			powerOfTwoHBox.setDisable(true);
			moreOperationsHBox.setDisable(true);
			duelTimeHBox.setDisable(true);
			applyBtn.setDisable(true);
		}

		// Settings laden
		wholeNumbersCB.setSelected(data.settings.wholeNumbersNegative);
		wholeNumbersTF.setText("" + data.settings.wholeNumbersMax);
		decimalsCB.setSelected(data.settings.decimalsNegative);
		decimalsTF.setText("" + data.settings.decimalsMax);
		powerOfTwoTF.setText("" + data.settings.powerOfTwoMax);
		moreOperationsTF.setText("" + data.settings.moreOperationsMax);
		duelBaseTimeTF.setText("" + data.settings.duelBaseTime);
		duelTimeReductionTF.setText(("" + data.settings.duelTimeReduction).replace('.', ','));

		for (String operator : data.settings.operators) {
			if (operator.equals("+"))
				wholeNumbersAdditionCbx.setSelected(true);
			else if (operator.equals("-"))
				wholeNumbersSubtractionCbx.setSelected(true);
			else if (operator.equals("*"))
				wholeNumbersMultiplicationCbx.setSelected(true);
			else if (operator.equals("/"))
				wholeNumbersDivisionCbx.setSelected(true);
			else if (operator.equals("^2"))
				squareNumbersCbx.setSelected(true);
			else if (operator.equals("2v"))
				squareRootsCbx.setSelected(true);
			else if (operator.equals("3v"))
				cubeRootsCbx.setSelected(true);
			else if (operator.equals("+d1")) {
				decimalsAdditionCbx.setSelected(true);
			} else if (operator.equals("-d1")) {
				decimalsSubtractionCbx.setSelected(true);
			} else if (operator.equals("*d1")) {
				decimalsMultiplicationCbx.setSelected(true);
			} else if (operator.equals("/d1")) {
				decimalsDivisionCbx.setSelected(true);
			} else if (operator.equals("2^x"))
				powerOfTwoCbx.setSelected(true);
		}
	}

	// Einstellungen speichern
	public void SaveSettings() {
		data.settings.wholeNumbersNegative = wholeNumbersCB.isSelected();
		data.settings.wholeNumbersMax = Integer.parseInt(wholeNumbersTF.getText());
		data.settings.decimalsNegative = decimalsCB.isSelected();
		data.settings.decimalsMax = Integer.parseInt(decimalsTF.getText());
		data.settings.powerOfTwoMax = Integer.parseInt(powerOfTwoTF.getText());
		data.settings.moreOperationsMax = Integer.parseInt(moreOperationsTF.getText());
		data.settings.duelBaseTime = Integer.parseInt(duelBaseTimeTF.getText());
		data.settings.duelTimeReduction = Float.parseFloat(duelTimeReductionTF.getText().replace(',', '.'));

		ArrayList<String> operatorsList = new ArrayList<String>();

		if (wholeNumbersAdditionCbx.isSelected())
			operatorsList.add("+");
		if (wholeNumbersSubtractionCbx.isSelected())
			operatorsList.add("-");
		if (wholeNumbersMultiplicationCbx.isSelected())
			operatorsList.add("*");
		if (wholeNumbersDivisionCbx.isSelected())
			operatorsList.add("/");
		if (squareNumbersCbx.isSelected())
			operatorsList.add("^2");
		if (squareRootsCbx.isSelected())
			operatorsList.add("2v");
		if (cubeRootsCbx.isSelected())
			operatorsList.add("3v");
		// Dezimalbrüche
		if (decimalsAdditionCbx.isSelected())
			operatorsList.add("+d1");
		if (decimalsSubtractionCbx.isSelected())
			operatorsList.add("-d1");
		if (decimalsMultiplicationCbx.isSelected())
			operatorsList.add("*d1");
		if (decimalsDivisionCbx.isSelected())
			operatorsList.add("/d1");
		if (powerOfTwoCbx.isSelected())
			operatorsList.add("2^x");

		String[] operators = new String[0];
		operators = operatorsList.toArray(operators);

		data.settings.operators = operators;
	}

	protected void onCloseStage(WindowEvent we) {
		saveStageSizes();

		if (editable) {
			String duelTimeReductionString = duelTimeReductionTF.getText().replace(',', '.');

			if (!applyBtn.isDisabled() && HelpingClass.isInteger(wholeNumbersTF.getText())
					&& Integer.parseInt(wholeNumbersTF.getText()) > 0 && HelpingClass.isInteger(decimalsTF.getText())
					&& Integer.parseInt(decimalsTF.getText()) > 0 && HelpingClass.isInteger(powerOfTwoTF.getText())
					&& Integer.parseInt(powerOfTwoTF.getText()) > 1
					&& HelpingClass.isInteger(moreOperationsTF.getText())
					&& Integer.parseInt(moreOperationsTF.getText()) > 1
					&& HelpingClass.isInteger(duelBaseTimeTF.getText()) && HelpingClass.isFloat(duelTimeReductionString)
					&& Integer.parseInt(duelBaseTimeTF.getText()) >= Float.parseFloat(duelTimeReductionString)
					&& Integer.parseInt(duelBaseTimeTF.getText()) > 0
					&& Float.parseFloat(duelTimeReductionString) >= 0) {
				// Alert erstellen und Design bearbeiten
				Alert alert = new Alert(AlertType.CONFIRMATION);
				alert.setTitle(data.applicationName);
				alert.setHeaderText(null);
				alert.setContentText("Sollen die Einstellungen gespeichert werden?");

				// Buttons dem Alertfenster hinzufügen
				ButtonType buttonTypeYes = new ButtonType("Ja");
				ButtonType buttonTypeNo = new ButtonType("Nein");
				ButtonType buttonTypeCancel = new ButtonType("Abbrechen");
				alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo, buttonTypeCancel);

				// Icon dem Alertfenster zuweisen
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
						alertStage.setX(stage.getX() + stage.getWidth() / 2 - alertStage.getWidth() / 2);
						alertStage.setY(stage.getY() + stage.getHeight() / 2 - alertStage.getHeight() / 2);
					}
				});

				// Zeigen des Fensters -> Auswerten des gedrückten Buttons
				Optional<ButtonType> result = alert.showAndWait();
				if (result.get() == buttonTypeCancel) {
					we.consume();
				} else if (result.get() == buttonTypeYes) {
					SaveSettings();
				}
			} else
				we.consume();
		}
	}

	@FXML
	private void fifthGradeBtn_onAction(ActionEvent event) {
		wholeNumbersTF.setText("1000");
		moreOperationsTF.setText("400");
		powerOfTwoTF.setText("1024");

		wholeNumbersAdditionCbx.setSelected(true);
		wholeNumbersSubtractionCbx.setSelected(true);
		wholeNumbersMultiplicationCbx.setSelected(true);
		wholeNumbersDivisionCbx.setSelected(true);

		decimalsAdditionCbx.setSelected(false);
		decimalsSubtractionCbx.setSelected(false);
		decimalsMultiplicationCbx.setSelected(false);
		decimalsDivisionCbx.setSelected(false);

		powerOfTwoCbx.setSelected(true);

		squareNumbersCbx.setSelected(true);
		squareRootsCbx.setSelected(false);
		cubeRootsCbx.setSelected(false);
		
		wholeNumbersCB.setSelected(false);
		decimalsCB.setSelected(false);
	}

	@FXML
	private void sixthGradeBtn_onAction(ActionEvent event) {
		decimalsTF.setText("100");
		moreOperationsTF.setText("625");

		wholeNumbersAdditionCbx.setSelected(false);
		wholeNumbersSubtractionCbx.setSelected(false);
		wholeNumbersMultiplicationCbx.setSelected(false);
		wholeNumbersDivisionCbx.setSelected(false);

		decimalsAdditionCbx.setSelected(true);
		decimalsSubtractionCbx.setSelected(true);
		decimalsMultiplicationCbx.setSelected(true);
		decimalsDivisionCbx.setSelected(true);

		powerOfTwoCbx.setSelected(false);

		squareNumbersCbx.setSelected(false);
		squareRootsCbx.setSelected(false);
		cubeRootsCbx.setSelected(false);
		
		wholeNumbersCB.setSelected(false);
		decimalsCB.setSelected(false);
	}

	@FXML
	private void applyBtn_onAction(ActionEvent event) {
		saveStageSizes();

		String duelTimeReductionString = duelTimeReductionTF.getText().replace(',', '.');

		if (HelpingClass.isInteger(wholeNumbersTF.getText()) && Integer.parseInt(wholeNumbersTF.getText()) > 0
				&& HelpingClass.isInteger(decimalsTF.getText()) && Integer.parseInt(decimalsTF.getText()) > 0
				&& HelpingClass.isInteger(powerOfTwoTF.getText()) && Integer.parseInt(powerOfTwoTF.getText()) > 1
				&& HelpingClass.isInteger(moreOperationsTF.getText())
				&& Integer.parseInt(moreOperationsTF.getText()) > 1 && HelpingClass.isInteger(duelBaseTimeTF.getText())
				&& HelpingClass.isFloat(duelTimeReductionString)
				&& Integer.parseInt(duelBaseTimeTF.getText()) >= Float.parseFloat(duelTimeReductionString)
				&& Integer.parseInt(duelBaseTimeTF.getText()) > 0 && Float.parseFloat(duelTimeReductionString) >= 0) {
			SaveSettings();
			stage.close();
		}
	}

	@FXML
	private void cancelBtn_onAction(ActionEvent event) {
		saveStageSizes();
		stage.close();
	}

	@FXML
	private void checkBox_onAction(ActionEvent event) {
		CheckBox[] checkBoxes = new CheckBox[] { wholeNumbersAdditionCbx, wholeNumbersSubtractionCbx,
				wholeNumbersMultiplicationCbx, wholeNumbersDivisionCbx, decimalsAdditionCbx, decimalsSubtractionCbx,
				decimalsMultiplicationCbx, decimalsDivisionCbx, powerOfTwoCbx, squareNumbersCbx, squareRootsCbx,
				cubeRootsCbx };

		int selected = 0;

		for (int i = 0; i < checkBoxes.length; i++) {
			if (checkBoxes[i].isSelected())
				selected++;
		}

		if (selected == 0) {
			applyBtn.setDisable(true);
		} else {
			applyBtn.setDisable(false);
		}
	}

	private void saveStageSizes() {
		if (!data.windowSizes.maximized) {
			// Stagemaße abspeichern
			data.windowSizes.settingsStageWidth = stage.getWidth();
			data.windowSizes.settingsStageX = stage.getX();
			data.windowSizes.settingsStageY = stage.getY();
		}
	}

}