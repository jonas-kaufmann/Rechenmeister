package application.Model;

import java.math.BigDecimal;

/**
 * Speichern von Aufgaben
 */
public class Exercise {

	// Attribute
	public String Operator;
	public BigDecimal Number1;
	public BigDecimal Number2;
	public BigDecimal Solution;

	/**
	 * Konstruktor für "normale" Aufgaben (Strich- und Punktrechnung)
	 *
	 * @param operator Rechenzeichen
	 * @param number1  linke Zahl
	 * @param number2  rechte Zahl
	 * @param solution Lösung
	 */
	public Exercise(String operator, BigDecimal number1, BigDecimal number2, BigDecimal solution) {
		Operator = operator;
		Number1 = number1;
		Number2 = number2;
		Solution = solution;
	}

	/**
	 * Konstruktor für Wurzel- und Quadrierungsaufgaben
	 * 
	 * @param operator Rechenzeichen
	 * @param number1  Basis für Quadrierung / Zahl für Wurzelziehung
	 * @param solution Lösung
	 */
	public Exercise(String operator, BigDecimal number1, BigDecimal solution) {
		Operator = operator;
		Number1 = number1;
		Solution = solution;
	}

	/**
	 * @return Term ohne Lösung als String
	 */
	public String GetTerm() {
		if(Number1 != null && Number1.compareTo(new BigDecimal(0)) == 0)
			Number1 = new BigDecimal(0);
		if(Number2 != null && Number2.compareTo(new BigDecimal(0)) == 0)
			Number2 = new BigDecimal(0);
		if (Operator.equals("^2"))
			return Number1 + "² = ";
		else if (Operator.equals("2v"))
			return "√" + Number1 + " = ";
		else if (Operator.equals("3v"))
			return "∛" + Number1 + " = ";
		else if (Operator.equals("*")) {
			if (Number2.intValue() < 0)
				return Number1 + " ⋅ (" + Number2 + ") = ";
			else
				return Number1 + " ⋅ " + Number2 + " = ";
		} else if (Operator.equals("/")) {
			if (Number2.intValue() < 0)
				return Number1 + " : (" + Number2 + ") = ";
			return Number1 + " : " + Number2 + " = ";
		} else if (Operator.equals("+d1")) {
			if (Number2.compareTo(new BigDecimal(0)) == -1)
				return Number1.toString().replace('.', ',') + " + (" + Number2.toString().replace('.', ',') + ") = ";
			else
				return Number1.toString().replace('.', ',') + " + " + Number2.toString().replace('.', ',') + " = ";
		} else if (Operator.equals("-d1")) {
			if (Number2.compareTo(new BigDecimal(0)) == -1)
				return Number1.toString().replace('.', ',') + " - (" + Number2.toString().replace('.', ',') + ") = ";
			else
				return Number1.toString().replace('.', ',') + " - " + Number2.toString().replace('.', ',') + " = ";
		} else if (Operator.equals("*d1")) {
			if (Number2.compareTo(new BigDecimal(0)) == -1)
				return Number1.toString().replace('.', ',') + " ⋅ (" + Number2.toString().replace('.', ',') + ") = ";
			else
				return Number1.toString().replace('.', ',') + " ⋅ " + Number2.toString().replace('.', ',') + " = ";
		} else if (Operator.equals("/d1")) {
			if (Number2.compareTo(new BigDecimal(0)) == -1)
				return Number1.toString().replace('.', ',') + " : (" + Number2.toString().replace('.', ',') + ") = ";
			else
				return Number1.toString().replace('.', ',') + " : " + Number2.toString().replace('.', ',') + " = ";
		} else if (Operator.equals("2^x"))
			return "2" + HelpingClass.raiseNumbers(Number1.toString()) + " = ";
		else {
			if (Number2.compareTo(new BigDecimal(0)) == -1)
				return Number1.toString().replace('.', ',') + " " + Operator + " ("
						+ Number2.toString().replace('.', ',') + ") = ";
			else
				return Number1.toString().replace('.', ',') + " " + Operator + " "
						+ Number2.toString().replace('.', ',') + " = ";
		}
	}

	/**
	 * Vergleich, ob zwei Exercise-objekte den selben Term repräsentieren
	 * 
	 * @param exercise zu vergleichendes Exercise-Objekt
	 * @return boolean true = gleich
	 */
	public boolean CompareExercise(Exercise exercise) {
		if (exercise.Operator.equals(Operator) && exercise.Number1.compareTo(Number1) == 0) {
			if (Operator.equals("^2") || Operator.equals("2v") || Operator.equals("3v") || Operator.equals("2^x")) {
				return true;
			} else if (exercise.Number2.compareTo(Number2) == 0) {
				return true;
			}
		}
		return false;
	}
}
