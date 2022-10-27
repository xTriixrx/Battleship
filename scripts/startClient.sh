#!/bin/sh

HOST="$3"
PORT="$4"
OPPONENT="$5"
GAME_LIB="$2"
JAVAFX_LIB="$1"
LOG4J_FILE_PATH="log4j2.xml"

if [ $# -ne 5 ]; then
	echo "Expected 5 parameters but only received $# parameters."
	echo "For logging support, it is expected the log4j2.xml file should exist in current working directory."
	echo "./startClient.sh [JAVAFX_LIB] [GAME_LIB] [HOST] [PORT] [AUTOMATED]"
	echo "./startClient.sh javafx-17.0.2/lib lib 192.168.0.1 5000 false"
	exit 1
fi

java -Dlog4j.configurationFile=$LOG4J_FILE_PATH --module-path $JAVAFX_LIB --add-modules javafx.controls,javafx.fxml \
-cp "$JAVAFX_LIB/*:$GAME_LIB/*" com.qfi.battleship.GUIDriverRunner --type=client --port=$PORT --host=$HOST --automatedOpponent=$OPPONENT &

exit 0
