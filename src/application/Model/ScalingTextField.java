package application.Model;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class ScalingTextField {

	private Pane pane;
	private Text text;
	private final String fontFamily;
	private double fontSize;
	private double defaultFontSize;
	public boolean scalingActive;

	/**
	 * Erzeugt ein sich nach Text automatisch skalierendes Textfeld. Die Skalierung
	 * wird direkt aktiviert.
	 * 
	 * @param textField     Textfeld
	 * @param scalingFactor Genauigkeitsfaktor um den skaliert werden soll
	 */
	public ScalingTextField(TextField textField, double scalingFactor) {
		Font font = textField.getFont();
		fontFamily = font.getFamily();
		fontSize = defaultFontSize = font.getSize();
		pane = (Pane) textField.getChildrenUnmodifiable().get(0);
		String preDefinedText = textField.getText();

		// Text Objekt finden und abspeichern
		ObservableList<Node> nodes = pane.getChildrenUnmodifiable();
		Text _text;
		double textWidth1;
		double textWidth2;
		for (Node node : nodes) {
			try {
				_text = (Text) node;
				textField.setText("");
				textWidth1 = _text.getLayoutBounds().getWidth();
				textField.setText(".");
				textWidth2 = _text.getLayoutBounds().getWidth();
				if (textWidth1 < textWidth2) {
					text = _text;
					break;
				}
			} catch (ClassCastException e) {
			}
		}

		// Pane Größe fixieren; -> Textfeld hat keine Auswirkungen mehr
		pane.setMinWidth(pane.getWidth());
		pane.setMaxWidth(pane.getWidth());
		pane.setMinHeight(pane.getHeight());
		pane.setMaxHeight(pane.getHeight());

		// Listener setzen
		if (text != null) {
			textField.textProperty().addListener(new ChangeListener<String>() {
				@Override
				public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
					if (scalingActive) {
						fontSize = textField.getFont().getSize();
						// Verkleinern
						while (getPaneWidth() / text.getLayoutBounds().getWidth() < 1 && fontSize > 1) {
							textField.setFont(new Font(fontFamily, fontSize -= scalingFactor));
						}
						// Vergrößern
						if (getPaneWidth() / text.getLayoutBounds().getWidth() > 1
								&& defaultFontSize > textField.getFont().getSize()) {
							do {
								textField.setFont(new Font(fontFamily, fontSize += scalingFactor));
							} while (getPaneWidth() / text.getLayoutBounds().getWidth() > 1
									&& defaultFontSize > textField.getFont().getSize());
							textField.setFont(new Font(fontFamily, fontSize -= scalingFactor));
						}
					}
				}
			});
		}

		// Skalierung aktivieren
		activateScaling();
		textField.setText(preDefinedText);
	}

	private double getPaneWidth() {
		return pane.getWidth() - pane.getWidth() / 100;
	}

	/**
	 * Aktiviert die Skalierung
	 */
	public void activateScaling() {
		scalingActive = true;
	}

	/**
	 * Deaktiviert die Skalierung
	 */
	public void deactivateScaling() {
		scalingActive = false;
	}
}
