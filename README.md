# Rechenmeister
Kopfrechentrainer, gedacht für den Einsatz an Schulen. Bietet einen Trainings-, Duell- und Wettbewerbsmodus. Im Duellmodus spielen 2 Schüler an einem PC gegeneinander, beim Wettbewerbsmodus kann eine unbegrenzte Anzahl an Teilnehmern gegeneinander antreten. Jeder Schüler rechnet an einem eigenen PC und erhält die gleichen Aufgaben.

## Voraussetzungen
Benötigt Java 8 (weder aktueller, noch älter), nach Möglichkeit neuster Service-Patch
Bisher nur auf Windows 7 und Windows 10 getestet

## Fehler
Fehler können unter dem Tab Issues eingesendet werden.
Wenn die Anwendung abstürzt wird automatisch ein Log angelegt. Dieser befindet sich im Ordner %localappdata%/Rechenmeister. Diesen bei der Einsendung eines Fehlers bitte anhängen.

## Lizenz
Das Projekt läuft unter der Lizenz [CC BY-SA 4.0](https://creativecommons.org/licenses/by-sa/4.0/)

## Anmerkungen zum Quellcode
### Dokumentation
Die Dokumentation kann mit der Datei 'Generate Javadoc.cmd' im Stammverzeichnis generiert werden. Der Pfad für 'javadoc.exe' muss höchstwahrscheinlich angepasst werden.

### Erzeugung einer Exe mit Installer
Vorraussetzungen:
- Inno-Setup Version 5.* installieren und Installationspfad in Umgebungsvariable 'Path' eintragen
- Apache Ant herunterladen und entpacken, Pfad auf 'bin'-Ordner in 'build/build.cmd' eintragen
- Systemumgebungsvariable PATH oder Benutzervariable JAVA_HOME muss auf JDK zeigen

In folgenden Dateien Version erhöhen und bei Bedarf weiteres anpassen:
- 'build/build.xml'
- 'build/package/windows/Rechentrainer.iss'
- 'build.fxbuild'

'build.cmd' in CMD ausführen

## Bekannte Java-Versionsprobleme
- Exception beim Resizen mit Java 9 oder neuer (impl_getPeer Methode aus der Klasse Stage entfernt)
- Fehlerhafte Fensterdarstellung nach Minimieren des maximierten Fensters in die Taskleiste (Bug der IconifiedProperty; in Service-Patch 172 und höher behoben)
