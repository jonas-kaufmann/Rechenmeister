package application.Model;

/**
 * Dient dem Speichern der Einstellungen
 */
public class Settings {

	// Attribute
	public String[] operators = { "+", "-", "*", "/", "^2", "2v", "3v", "+d1", "-d1", "*d1", "/d1", "2^x" };

	public boolean wholeNumbersNegative = false;
	public int wholeNumbersMax = 100;

	public boolean decimalsNegative = false;
	public int decimalsMax = 100;

	public int powerOfTwoMax = 100;

	public int moreOperationsMax = 100;

	public int duelBaseTime = 20;
	public float duelTimeReduction = 4;

	public boolean activateLogging = true;
	public boolean checkJavaVersion = true;
	public int lastCheckedJavaVersion = 0;
	
	public int competitionDuration = 90;
}
