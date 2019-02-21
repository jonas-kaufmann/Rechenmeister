# Call of Mathe: Rechenmeister
Kopfrechentrainer-Projekt des Informatikkurses im S-Jahrgang 2019 des Nikolaus-von-Kues Gymnasiums

## Lizenz
Das Projekt läuft unter der Lizenz [CC BY-SA 4.0](https://creativecommons.org/licenses/by-sa/4.0/)

## Dokumentation
Die Dokumentation kann mit der Datei 'Generate Javadoc.cmd' im Stammverzeichnis generiert werden. Der Pfad für 'javadoc.exe' muss höchstwahrscheinlich angepasst werden.

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
