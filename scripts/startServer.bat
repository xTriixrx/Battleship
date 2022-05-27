@echo off

set JAVAFX_LIB=%1
set GAME_LIB=%2
set PORT=%3
set ARGC=0

for %%x in (%*) do Set /A ARGC+=1

if not %ARGC% == 3 (
	echo Expected 3 arguments but only received %ARGC% arguments.
	echo ./startServer.bat [JAVAFX_LIB] [GAME_LIB] [PORT]
	echo ./startServer.bat C:\javafx-sdk-18.0.1\lib lib 5000
	EXIT /B
)

START /B java --module-path %JAVAFX_LIB% --add-modules javafx.controls,javafx.fxml -cp ^
	"%JAVAFX_LIB%\*;%GAME_LIB%/*" com.qfi.battleship.GUIDriverRunner --type=server --port=%PORT% --host=127.0.0.1

EXIT /B