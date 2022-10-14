#!/bin/sh

PORT="$3"
OPPONENT="$4"
GAME_LIB="$2"
JAVAFX_LIB="$1"

if [ $# -ne 4 ]; then
	echo "Expected 4 parameters but only received $# parameters."
	echo "./startServer.sh [JAVAFX_LIB] [GAME_LIB] [PORT] [AUTOMATED]"
	echo "./startServer.sh javafx-17.0.2/lib lib 5000 false"
	exit 1
fi

java -Dlog4j2.disable.jmx="TRUE" --module-path $JAVAFX_LIB --add-modules javafx.controls,javafx.fxml \
-cp "$JAVAFX_LIB/*:$GAME_LIB/*" com.qfi.battleship.GUIDriverRunner --type=server --port=$PORT --host=127.0.0.1 --automatedOpponent=$OPPONENT &

exit 0
