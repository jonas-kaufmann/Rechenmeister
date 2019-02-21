package application.Model;

import java.math.BigDecimal;

/**
 * Speichern von Statistikaufgaben
 */
public class StatisticExercise {

	// Attribute
	private String term;
	private BigDecimal solution;
	private BigDecimal inputSolution;
	private boolean correct;

	/**
	 * Konstruktor für StatistikAufgaben
	 *
	 * @param term Term
	 * @param solution Lösung
	 * @param inputSolution vom Nutzer eingegebene Lösung
	 * @param correct Aufgabe richtig gelöst = true
	 */
	public StatisticExercise(String term, BigDecimal solution, BigDecimal inputSolution, boolean correct) {
		this.term = term;
		this.solution = solution;
		this.inputSolution = inputSolution;
		this.correct = correct;
	}

	/**
	 * @return Term
	 */
	public String getTerm() {

		return term + solution.toString().replace('.', ',');
	}

	/**
	 * @return eingegebene Lösung
	 */
	public String getInputSolution() {

		return inputSolution.toString().replace('.', ',');
	}

	/**
	 * @return Aufgabe korrekt gelöst = true
	 */
	public boolean getCorrect() {
		return correct;
	}
}
