package application.Model;

import javafx.application.Platform;
import javafx.scene.control.TextField;

/**
 * Stellt Hilfsmethoden bereit
 */
public class HelpingClass {
	/**
	 * Prüft, ob der übergebene String eine Zahl darstellt
	 * 
	 * @param text Der zu überprüfende String
	 * @return true = ist Zahl
	 */
	public static boolean isInteger(String text) {
		try {
			Integer.parseInt(text);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean isFloat(String text) {
		try {
			Float.parseFloat(text);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Prüft, ob der übergebene String zugewiesen und nicht leer ist, bzw. nicht nur
	 * aus Leerzeichen entsteht
	 * 
	 * @param text Der zu überprüfende String
	 * @return true = String leer
	 */
	public static boolean isNullOrBlank(String text) {
		return text == null || text.trim().length() == 0;
	}

	/**
	 * Ersetzt Zahlen durch zugehörige Potenzen.
	 * 
	 * @param text zu bearbeitender Text
	 * @return bearbeiteter Text
	 */
	public static String raiseNumbers(String text) {
		text = text.replace('0', '⁰');
		text = text.replace('1', '¹');
		text = text.replace('2', '²');
		text = text.replace('3', '³');
		text = text.replace('4', '⁴');
		text = text.replace('5', '⁵');
		text = text.replace('6', '⁶');
		text = text.replace('7', '⁷');
		text = text.replace('8', '⁸');
		text = text.replace('9', '⁹');

		return text;
	}

	/**
	 * Teilt die Zeit in ms in Sekunden und Millisekunden auf.
	 * 
	 * @param timeInMilliseconds Zeit in ms
	 * @return Zeit als String in ms und s
	 */
	public static String generateTimeString(long timeInMilliseconds) {
		if (timeInMilliseconds == 0) {
			return "0s";
		}

		long seconds = Math.abs(timeInMilliseconds) / 1000;

		if (timeInMilliseconds % 1000 == 0) {
			if (timeInMilliseconds < 0) {
				return "- " + seconds + "s";
			} else {
				return seconds + "s";
			}
		} else {
			long millieseconds = Math.abs(timeInMilliseconds) - (seconds * 1000);

			if (timeInMilliseconds < 0)
				return "- " + seconds + "s " + millieseconds + "ms";
			else
				return seconds + "s " + millieseconds + "ms";
		}
	}
	
	/**
	 * Deselektiert den Text des Textfeldes
	 * @param textField Textfeld
	 */
	public static void deselect(TextField textField) {
		Platform.runLater(() -> {
			if (textField.getText().length() > 0 && textField.selectionProperty().get().getEnd() == 0) {
				deselect(textField);
			} else {
				textField.selectEnd();
				textField.deselect();
			}
		});
	}
}
