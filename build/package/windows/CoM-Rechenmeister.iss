;This file will be executed next to the application bundle image
;I.e. current directory will contain folder CoM-Rechenmeister with application files
[Setup]
AppId={{fxApplication}}
AppName=Call of Mathe: Rechenmeister
AppVersion=4.0
AppVerName=CoM-Rechenmeister 4.0
AppPublisher=NvKG-Inf-S-2019
AppComments=Call of Mathe: Rechenmeister
AppCopyright=Copyright (C) 2019
;AppPublisherURL=http://java.com/
;AppSupportURL=http://java.com/
;AppUpdatesURL=http://java.com/
DefaultDirName={localappdata}\Rechenmeister
DisableStartupPrompt=Yes
DisableDirPage=No
DisableProgramGroupPage=Yes
DisableReadyPage=Yes
DisableFinishedPage=Yes
DisableWelcomePage=Yes
DefaultGroupName=NvKG-Inf-S-2019
;Optional License
LicenseFile=
;WinXP or above
MinVersion=0,5.1 
OutputBaseFilename=CoM-Rechenmeister
Compression=lzma
SolidCompression=yes
PrivilegesRequired=lowest
SetupIconFile=CoM-Rechenmeister\CoM-Rechenmeister.ico
UninstallDisplayIcon={app}\CoM-Rechenmeister.ico
UninstallDisplayName=Call of Mathe: Rechenmeister
WizardImageStretch=No
WizardSmallImageFile=CoM-Rechenmeister-setup-icon.bmp   
ArchitecturesInstallIn64BitMode=x64

[Languages]
Name: "Deutsch"; MessagesFile: "compiler:Languages/German.isl"

[UninstallDelete]
Type: filesandordirs; Name: "{localappdata}\Rechenmeister"

[Files]
Source: "CoM-Rechenmeister\CoM-Rechenmeister.exe"; DestDir: "{app}"; Flags: ignoreversion
Source: "CoM-Rechenmeister\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{group}\CoM-Rechenmeister"; Filename: "{app}\CoM-Rechenmeister.exe"; IconFilename: "{app}\CoM-Rechenmeister.ico"; Check: returnTrue()
Name: "{commondesktop}\CoM-Rechenmeister"; Filename: "{app}\CoM-Rechenmeister.exe";  IconFilename: "{app}\CoM-Rechenmeister.ico"; Check: returnFalse()


[Run]
Filename: "{app}\CoM-Rechenmeister.exe"; Parameters: "-Xappcds:generatecache"; Check: returnFalse()
Filename: "{app}\CoM-Rechenmeister.exe"; Description: "{cm:LaunchProgram,CoM-Rechenmeister}"; Flags: nowait postinstall skipifsilent; Check: returnTrue()
Filename: "{app}\CoM-Rechenmeister.exe"; Parameters: "-install -svcName ""CoM-Rechenmeister"" -svcDesc ""CoM-Rechenmeister"" -mainExe ""CoM-Rechenmeister.exe""  "; Check: returnFalse()

[UninstallRun]
Filename: "{app}\CoM-Rechenmeister.exe "; Parameters: "-uninstall -svcName CoM-Rechenmeister -stopOnUninstall"; Check: returnFalse()

[Code]
function returnTrue(): Boolean;
begin
  Result := True;
end;

function returnFalse(): Boolean;
begin
  Result := False;
end;

function InitializeSetup(): Boolean;
begin
// Possible future improvements:
//   if version less or same => just launch app
//   if upgrade => check if same app is running and wait for it to exit
//   Add pack200/unpack200 support? 
  Result := True;
end;  
