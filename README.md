# Rechenmeister
Kopfrechentrainer, gedacht für den Einsatz an Schulen. Bietet einen Trainings-, Duell- und Wettbewerbsmodus. Im Duellmodus spielen 2 Schüler an einem PC gegeneinander, beim Wettbewerbsmodus kann eine unbegrenzte Anzahl an Teilnehmern gegeneinander antreten. Jeder Schüler rechnet an einem eigenen PC und erhält die gleichen Aufgaben.

## Voraussetzungen
Benötigt Java 8, nach Möglichkeit neuster Service-Patch
Bisher nur auf Windows 7 und Windows 10 getestet

## Lizenz
Das Projekt läuft unter der Lizenz [CC BY-SA 4.0](https://creativecommons.org/licenses/by-sa/4.0/)

## Dokumentation
Die Dokumentation kann mit der Datei 'Generate Javadoc.cmd' im Stammverzeichnis generiert werden. Der Pfad für 'javadoc.exe' muss höchstwahrscheinlich angepasst werden.

## Fehler
Fehler können im Abschnitt Issues eingesendet werden.
Wenn die Anwendung abstürzt wird automatisch ein Log angelegt. Dieser befindet sich im Ordner %localappdata%/Rechenmeister. Diesen bei der Einsendung eines Fehlers bitte anhängen.

## Erzeugung einer Exe mit Installer
Vorraussetzungen:
- Inno-Setup installiert und in Umgebungsvariable 'Path' eingerichtet
- Apache Ant
- Umgebungsvariable PATH zeigt auf JDK

Folgende Dateien anpassen:
- 'build/build.xml'
- 'build/package/windows/Rechentrainer.iss'
- 'build.fxbuild'

Pfad zu Ant in 'build/build.cmd' eintragen und ausführen.

## Versionsprobleme
- Exception beim Resizen mit Java 9 oder neuer (impl_getPeer Methode aus der Klasse Stage entfernt)
- Fehlerhafte Fensterdarstellung nach Minimieren des maximierten Fensters in die Taskleiste (Bug der IconifiedProperty; in Service-Patch 172 und höher behoben)

