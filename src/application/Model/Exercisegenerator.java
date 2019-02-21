package application.Model;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Random;

/**
 * Generiert Aufgaben
 */
public class Exercisegenerator {

	private DataModel data;
	private Random random;

	public Exercisegenerator(DataModel dataModel) {
		data = dataModel;
		random = new Random();
	}

	/**
	 * Generierung einer Aufgabe mit zufälligen Zahlen und Rechenzeichen
	 * 
	 * @return Generierte Aufgabe
	 */
	public Exercise generate() {

		// Variablen initialisieren
		BigDecimal number1 = new BigDecimal(0);
		BigDecimal number2 = new BigDecimal(0);
		BigDecimal solution = new BigDecimal(0);

		NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.GERMANY);
		numberFormat.setMaximumFractionDigits(2);

		// Zufällig einen Operator aus der Operatorenliste auswählen
		String operator = data.settings.operators[random.nextInt(data.settings.operators.length)];

		// Je nach Operator Lösung, Zahl1 und Zahl2 erstellen
		int minNumber1;
		int maxNumber1;
		// Addition
		if (operator.equals("+")) {
			if (data.settings.wholeNumbersNegative) {
				solution = new BigDecimal(randomInt(-data.settings.wholeNumbersMax, data.settings.wholeNumbersMax));
				minNumber1 = solution.intValue() - data.settings.wholeNumbersMax;
				maxNumber1 = solution.intValue() + data.settings.wholeNumbersMax;
				if (minNumber1 < -data.settings.wholeNumbersMax)
					minNumber1 = -data.settings.wholeNumbersMax;
				if (maxNumber1 > data.settings.wholeNumbersMax)
					maxNumber1 = data.settings.wholeNumbersMax;
			} else {
				solution = new BigDecimal(randomInt(0, data.settings.wholeNumbersMax));
				minNumber1 = 0;
				maxNumber1 = solution.intValue();
			}
			number1 = new BigDecimal(randomInt(minNumber1, maxNumber1));
			number2 = new BigDecimal(solution.intValue() - number1.intValue());
		}
		// Subtraktion
		else if (operator.equals("-")) {
			if (data.settings.wholeNumbersNegative) {
				solution = new BigDecimal(randomInt(-data.settings.wholeNumbersMax, data.settings.wholeNumbersMax));
				minNumber1 = solution.intValue() - data.settings.wholeNumbersMax;
				maxNumber1 = solution.intValue() + data.settings.wholeNumbersMax;
				if (minNumber1 < -data.settings.wholeNumbersMax)
					minNumber1 = -data.settings.wholeNumbersMax;
				if (maxNumber1 > data.settings.wholeNumbersMax)
					maxNumber1 = data.settings.wholeNumbersMax;
			} else {
				solution = new BigDecimal(randomInt(0, data.settings.wholeNumbersMax));
				minNumber1 = solution.intValue();
				maxNumber1 = data.settings.wholeNumbersMax;
			}
			number1 = new BigDecimal(randomInt(minNumber1, maxNumber1));
			number2 = new BigDecimal(number1.intValue() - solution.intValue());
		}
		// Multiplikation
		else if (operator.equals("*")) {
			number1 = new BigDecimal(random.nextInt((int) Math.sqrt(data.settings.wholeNumbersMax)) + 1);
			int max = (int) (data.settings.wholeNumbersMax / number1.intValue());
			number2 = new BigDecimal(random.nextInt(max + 1));
			if (data.settings.wholeNumbersNegative && random.nextBoolean()) {
				if (random.nextBoolean())
					number1 = number1.negate();
				else
					number2 = number2.negate();
			}
			solution = number1.multiply(number2);
		}
		// Division
		else if (operator.equals("/")) {
			number2 = new BigDecimal(random.nextInt((int) Math.sqrt(data.settings.wholeNumbersMax)) + 1);
			int max = (int) (data.settings.wholeNumbersMax / number2.intValue());
			solution = new BigDecimal(random.nextInt(max + 1));
			number1 = solution.multiply(number2);
			if (data.settings.wholeNumbersNegative && random.nextBoolean()) {
				if (random.nextBoolean())
					number1 = number1.negate();
				else
					number2 = number2.negate();
				solution = solution.negate();
			}
		}
		// Quadratzahlen
		else if (operator.equals("^2")) {
			int max = (int) Math.sqrt(data.settings.moreOperationsMax) + 1;
			number1 = new BigDecimal(random.nextInt(max));
			solution = number1.multiply(number1);
		}
		// Quadratwurzeln
		else if (operator.equals("2v")) {
			int max = (int) Math.sqrt(data.settings.moreOperationsMax) + 1;
			solution = new BigDecimal(random.nextInt(max));
			number1 = solution.multiply(solution);
		}
		// Dritte Wurzeln
		else if (operator.equals("3v")) {
			int max = (int) Math.cbrt(data.settings.moreOperationsMax) + 1;
			solution = new BigDecimal(random.nextInt(max));
			number1 = solution.pow(3);
		}
		// Zweierprotenzen
		else if (operator.equals("2^x")) {
			int max = (int) (Math.log10(data.settings.powerOfTwoMax) / Math.log10(2)) + 1;
			number1 = new BigDecimal(random.nextInt(max));
			solution = new BigDecimal(Math.pow(2, number1.intValue()));
		}
		// Dezimalbrüche
		else if (operator.equals("+d1")) {
			int max = data.settings.decimalsMax * 10;
			double n1;
			double n2;
			int counter = 0;
			do {
				counter++;
				if (data.settings.decimalsNegative) {
					solution = new BigDecimal(randomInt(-max, max));
					minNumber1 = solution.intValue() - max;
					maxNumber1 = solution.intValue() + max;
					if (minNumber1 < -max)
						minNumber1 = -max;
					if (maxNumber1 > max)
						maxNumber1 = max;
				} else {
					solution = new BigDecimal(randomInt(0, max));
					minNumber1 = 0;
					maxNumber1 = solution.intValue();
				}
				number1 = new BigDecimal(randomInt(minNumber1, maxNumber1));
				number2 = new BigDecimal(solution.intValue() - number1.intValue());
			} while (counter <= 5 && (n1 = number1.doubleValue() / 10) == Math.floor(n1)
					&& (n2 = number2.doubleValue() / 10) == Math.floor(n2));

			boolean divided1 = false;
			boolean divided2 = false;
			int exerciseType = random.nextInt(3);
			if (number1.doubleValue() == 0) {
				number2 = number2.divide(new BigDecimal(10));
				divided2 = true;
			} else if (number2.doubleValue() == 0) {
				number1 = number1.divide(new BigDecimal(10));
				divided1 = true;
			} else if (exerciseType == 0) {
				if ((n1 = number1.doubleValue() / 10) == Math.floor(n1)) {
					number2 = number2.divide(new BigDecimal(10));
					divided2 = true;
				} else {
					number1 = number1.divide(new BigDecimal(10));
					divided1 = true;
				}
			} else if (exerciseType == 1) {
				if ((n2 = number2.doubleValue() / 10) == Math.floor(n2)) {
					number1 = number1.divide(new BigDecimal(10));
					divided1 = true;
				} else {
					number2 = number2.divide(new BigDecimal(10));
					divided2 = true;
				}
			} else {
				number1 = number1.divide(new BigDecimal(10));
				divided1 = true;
				number2 = number2.divide(new BigDecimal(10));
				divided2 = true;
			}

			solution = number1.add(number2);
			max /= 10;

			boolean number1Negated;
			boolean number2Negated;
			boolean solutionNegated;
			if (number1Negated = number1.compareTo(new BigDecimal(0)) == -1)
				number1 = number1.negate();
			if (number2Negated = number2.compareTo(new BigDecimal(0)) == -1)
				number2 = number2.negate();
			if (solutionNegated = solution.compareTo(new BigDecimal(0)) == -1)
				solution = solution.negate();

			if (solution.compareTo(new BigDecimal(max)) == 1 || number1.compareTo(new BigDecimal(max)) == 1
					|| number2.compareTo(new BigDecimal(max)) == 1) {
				if (divided1) {
					number2 = number2.divide(new BigDecimal(10));
				} else if (divided2) {
					number1 = number1.divide(new BigDecimal(10));
				} else if (number1.compareTo(number2) == 1) {
					number1 = number1.divide(new BigDecimal(10));
				} else {
					number2 = number2.divide(new BigDecimal(10));
				}
			}

			if (number1Negated)
				number1 = number1.negate();
			if (number2Negated)
				number2 = number2.negate();
			if (solutionNegated)
				solution = solution.negate();

			solution = number1.add(number2);
		} else if (operator.equals("-d1")) {
			int max = data.settings.decimalsMax * 10;
			double n1;
			double n2;
			int counter = 0;
			do {
				counter++;
				if (data.settings.decimalsNegative) {
					solution = new BigDecimal(randomInt(-max, max));
					minNumber1 = solution.intValue() - max;
					maxNumber1 = solution.intValue() + max;
					if (minNumber1 < -max)
						minNumber1 = -max;
					if (maxNumber1 > max)
						maxNumber1 = max;
				} else {
					solution = new BigDecimal(randomInt(0, max));
					minNumber1 = solution.intValue();
					maxNumber1 = max;
				}
				number1 = new BigDecimal(randomInt(minNumber1, maxNumber1));
				number2 = new BigDecimal(number1.intValue() - solution.intValue());
			} while (counter <= 5 && (n1 = number1.doubleValue() / 10) == Math.floor(n1)
					&& (n2 = number2.doubleValue() / 10) == Math.floor(n2));

			boolean divided1 = false;
			boolean divided2 = false;
			int exerciseType = random.nextInt(3);
			if (number1.doubleValue() == 0) {
				number2 = number2.divide(new BigDecimal(10));
				divided2 = true;
			} else if (number2.doubleValue() == 0) {
				number1 = number1.divide(new BigDecimal(10));
				divided1 = true;
			} else if (exerciseType == 0) {
				if ((n1 = number1.doubleValue() / 10) == Math.floor(n1)) {
					number2 = number2.divide(new BigDecimal(10));
					divided2 = true;
				} else {
					number1 = number1.divide(new BigDecimal(10));
					divided1 = true;
				}
			} else if (exerciseType == 1) {
				if ((n2 = number2.doubleValue() / 10) == Math.floor(n2)) {
					number1 = number1.divide(new BigDecimal(10));
					divided1 = true;
				} else {
					number2 = number2.divide(new BigDecimal(10));
					divided2 = true;
				}
			} else {
				number1 = number1.divide(new BigDecimal(10));
				divided1 = true;
				number2 = number2.divide(new BigDecimal(10));
				divided2 = true;
			}

			solution = number1.subtract(number2);
			max /= 10;

			boolean number1Negated;
			boolean number2Negated;
			boolean solutionNegated;
			if (number1Negated = number1.compareTo(new BigDecimal(0)) == -1)
				number1 = number1.negate();
			if (number2Negated = number2.compareTo(new BigDecimal(0)) == -1)
				number2 = number2.negate();
			if (solutionNegated = solution.compareTo(new BigDecimal(0)) == -1)
				solution = solution.negate();

			if (solution.compareTo(new BigDecimal(max)) == 1 || number1.compareTo(new BigDecimal(max)) == 1
					|| number2.compareTo(new BigDecimal(max)) == 1) {
				if (divided1) {
					number2 = number2.divide(new BigDecimal(10));
				} else if (divided2) {
					number1 = number1.divide(new BigDecimal(10));
				} else if (number1.compareTo(number2) == 1) {
					number1 = number1.divide(new BigDecimal(10));
				} else {
					number2 = number2.divide(new BigDecimal(10));
				}
			}

			if (number1Negated)
				number1 = number1.negate();
			if (number2Negated)
				number2 = number2.negate();
			if (solutionNegated)
				solution = solution.negate();

			solution = number1.subtract(number2);
		} else if (operator.equals("*d1")) {
			int max = data.settings.decimalsMax;
			double n1;
			double n2;
			int counter = 0;
			do {
				counter++;
				number1 = new BigDecimal(random.nextInt((int) Math.sqrt(max)) + 1);
				number2 = new BigDecimal(random.nextInt((int) (max / number1.intValue()) + 1));
			} while (counter <= 5 && (n1 = number1.doubleValue() / 10) == Math.floor(n1)
					&& (n2 = number2.doubleValue() / 10) == Math.floor(n2));

			int exerciseType = random.nextInt(3);
			if (number1.doubleValue() == 0)
				number2 = number2.divide(new BigDecimal(10));
			else if (number2.doubleValue() == 0)
				number1 = number1.divide(new BigDecimal(10));
			else if (exerciseType == 0) {
				if ((n1 = number1.doubleValue() / 10) == Math.floor(n1))
					number2 = number2.divide(new BigDecimal(10));
				else
					number1 = number1.divide(new BigDecimal(10));
			} else if (exerciseType == 1) {
				if ((n2 = number2.doubleValue() / 10) == Math.floor(n2))
					number1 = number1.divide(new BigDecimal(10));
				else
					number2 = number2.divide(new BigDecimal(10));
			} else {
				number1 = number1.divide(new BigDecimal(10));
				number2 = number2.divide(new BigDecimal(10));
			}

			if (data.settings.decimalsNegative && random.nextBoolean()) {
				int negation = random.nextInt(3);
				if (negation == 0)
					number1 = number1.negate();
				else if (negation == 1)
					number2 = number2.negate();
				else {
					number1 = number1.negate();
					number2 = number2.negate();
				}
			}

			solution = number1.multiply(number2);
		} else if (operator.equals("/d1")) {
			int max = data.settings.decimalsMax;
			double n1;
			double n2;
			int counter = 0;
			do {
				counter++;
				number2 = new BigDecimal(random.nextInt((int) Math.sqrt(max)) + 1);
				solution = new BigDecimal(random.nextInt((int) (max / number2.intValue()) + 1));
				number1 = solution.multiply(number2);
			} while (counter <= 5 && (n1 = number1.doubleValue() / 10) == Math.floor(n1)
					&& (n2 = number2.doubleValue() / 10) == Math.floor(n2));

			int exerciseType = random.nextInt(3);
			if (solution.doubleValue() == 0)
				number2 = number2.divide(new BigDecimal(10));
			else if (number2.doubleValue() == 0)
				solution = solution.divide(new BigDecimal(10));
			else if (exerciseType == 0) {
				if ((n1 = solution.doubleValue() / 10) == Math.floor(n1))
					number2 = number2.divide(new BigDecimal(10));
				else
					solution = solution.divide(new BigDecimal(10));
			} else if (exerciseType == 1) {
				if ((n2 = number2.doubleValue() / 10) == Math.floor(n2))
					solution = solution.divide(new BigDecimal(10));
				else
					number2 = number2.divide(new BigDecimal(10));
			} else {
				solution = solution.divide(new BigDecimal(10));
				number2 = number2.divide(new BigDecimal(10));
			}

			if (data.settings.decimalsNegative && random.nextBoolean()) {
				int negation = random.nextInt(3);
				if (negation == 0)
					solution = solution.negate();
				else if (negation == 1)
					number2 = number2.negate();
				else {
					solution = solution.negate();
					number2 = number2.negate();
				}
			}

			number1 = solution.multiply(number2);
		}

		// Unterscheidet ob es sich um eine Wurzel- und Quadrierungsaufgabe oder normale
		// Aufgabe (Strich- und Punktrechnung) handelt, um je nach dem die 2. Nummer
		// (rechte Zahl) zu ignorieren
		if (operator.equals("2v") || operator.equals("^2") || operator.equals("3v") || operator.equals("2^x")) {
			return new Exercise(operator, number1, solution);
		} else {
			return new Exercise(operator, number1, number2, solution);
		}
	}

	public int randomInt(int min, int max) {
		if (min <= max) {
			if (min < 0) {
				if (max > 1073741823 && min < 1073741823)
					throw new IllegalArgumentException(
							"the value of max and min has to be smaller than or equal to half of the max Int32 value");
				return random.nextInt(max - min + 1) + min;
			} else
				return random.nextInt(max - min + 1) + min;
		} else
			throw new IllegalArgumentException("max has to be greater than or equal to min");

	}
}