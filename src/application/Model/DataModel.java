package application.Model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Dient dem Speichern von Daten in der gesamten Anwendung (DataModel)
 */
public class DataModel {

	// Attribute
	public Stage mainStage;
	public Image appIcon;
	public Settings settings;
	public WindowSizeSettings windowSizes;
	public String username = "";
	public double IconifiedStageWidth;
	public double IconifiedStageX;
	public double IconifiedStageY;
	public final int PORT = 555;
	public String duelPlayer1Name = "Player 1";
	public String duelPlayer2Name = "Player 2";
	private String folderPath = System.getenv("LOCALAPPDATA") + "\\Rechenmeister";
	public int minimalJavaVersion = 172;
	public Application application;
	public String buttonCss;
	public String applicationName = "CoM: Rechenmeister";

	/**
	 * @param mainStage Hauptfenster
	 * @param appIcon Icon der Anwendung
	 * @param application Application-Objekt aus Main.java, welche Schnittstelle zu Windows bildet
	 * @param buttonCss CSS für Buttons
	 */
	public DataModel(Application application, Stage mainStage, Image appIcon, String buttonCss) {
		this.application = application;
		this.mainStage = mainStage;
		this.appIcon = appIcon;
		this.buttonCss = buttonCss;

		// Aus JSON laden
		Gson json = new Gson();
		String folderPath = System.getenv("LOCALAPPDATA") + "\\Rechenmeister";

		if (new File(folderPath + "\\Settings.json").exists()) {
			try {
				BufferedReader bufferedReader = new BufferedReader(
						new InputStreamReader(new FileInputStream(folderPath + "\\Settings.json"), "UTF-8"));
				settings = json.fromJson(bufferedReader, Settings.class);
				bufferedReader.close();
			} catch (Exception e) {
			}
		} else
			settings = new Settings();
		if (new File(folderPath + "\\WindowSizes.json").exists()) {
			try {
				BufferedReader bufferedReader = new BufferedReader(
						new InputStreamReader(new FileInputStream(folderPath + "\\WindowSizes.json"), "UTF-8"));
				windowSizes = json.fromJson(bufferedReader, WindowSizeSettings.class);
				bufferedReader.close();
			} catch (Exception e) {
			}
		} else
			windowSizes = new WindowSizeSettings();

		if (settings == null)
			settings = new Settings();
		if (windowSizes == null)
			windowSizes = new WindowSizeSettings();

		// Konsolen Logging
		if (settings.activateLogging) {
			PrintStream out;
			try {
				new File(folderPath).mkdirs();
				out = new PrintStream(
						new FileOutputStream(folderPath+ "\\log.txt", false), true);
				System.setOut(out);
				Thread.currentThread().setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
					public void uncaughtException(Thread t, Throwable e) {
						StringWriter sw = new StringWriter();
						e.printStackTrace(new PrintWriter(sw));
						String stacktrace = sw.toString();
						System.out.println(stacktrace);
					}
				});
			} catch (FileNotFoundException e) {
				System.out.println("Konsolen-Logging konnte nicht aktiviert werden.");
			}
		}
	}

	/**
	 * Speichert die Einstellungen in JSON Dateien.
	 */
	public void SaveToJSON() {
		new File(folderPath).mkdirs();
		Gson json = new GsonBuilder().setPrettyPrinting().create();

		try {
			BufferedWriter bufferedWriter = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(folderPath + "\\Settings.json"), "UTF-8"));
			json.toJson(settings, Settings.class, bufferedWriter);
			bufferedWriter.close();
		} catch (Exception e) {
		}
		try {
			BufferedWriter bufferedWriter = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(folderPath + "\\WindowSizes.json"), "UTF-8"));
			json.toJson(windowSizes, WindowSizeSettings.class, bufferedWriter);
			bufferedWriter.close();
		} catch (Exception e) {
		}
	}
}