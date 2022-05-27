@echo off

set JAVAFX_LIB=%1
set GAME_LIB=%2
set HOST=%3
set PORT=%4
set ARGC=0

for %%x in (%*) do Set /A ARGC+=1

if not %ARGC% == 4 (
	echo Expected 4 arguments but only received %ARGC% arguments.
	echo ./startClient.bat [JAVAFX_LIB] [GAME_LIB] [HOST] [PORT]
	echo ./startClient.bat C:\javafx-sdk-18.0.1\lib lib 192.168.5.1 5000
	EXIT /B
)

START /B java --module-path %JAVAFX_LIB% --add-modules javafx.controls,javafx.fxml -cp ^
	"%JAVAFX_LIB%\*;%GAME_LIB%/*" com.qfi.battleship.GUIDriverRunner --type=client --host=%HOST% --port=%PORT%

EXIT /B