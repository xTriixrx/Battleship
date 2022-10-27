#!/bin/sh

PORT="$3"
OPPONENT="$4"
GAME_LIB="$2"
JAVAFX_LIB="$1"
LOG4J_FILE_PATH="log4j2.xml"

if [ $# -ne 4 ]; then
	echo "Expected 4 parameters but only received $# parameters."
	echo "For logging support, it is expected the log4j2.xml file should exist in current working directory."
	echo "./startServer.sh [JAVAFX_LIB] [GAME_LIB] [PORT] [AUTOMATED]"
	echo "./startServer.sh javafx-17.0.2/lib lib 5000 false"
	exit 1
fi

java -Dlog4j.configurationFile=$LOG4J_FILE_PATH --module-path $JAVAFX_LIB --add-modules javafx.controls,javafx.fxml \
-cp "$JAVAFX_LIB/*:$GAME_LIB/*" com.qfi.battleship.GUIDriverRunner --type=server --port=$PORT --host=127.0.0.1 --automatedOpponent=$OPPONENT &

exit 0
