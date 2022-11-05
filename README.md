# JavaFX Battleship Game

<p align="center">
This is a network based Battleship implementation utilizing the JavaFX GUI framework.
The initial commit of this project was the original source code written for a college project I had written with a class
partner for a Design Patterns course. I planned on making a major refactor on the project at some point as the initial
commit will show the crazy amount of duplication that had existed. There were also numerous bugs that were nearly
impossible to track down due to its non-sustainability. I have since cut the code base in half, organized the roles of
functionality, and have added new capability to the implementation.
</p>

<p align="center">
The key design pattern utilized for this project is a bidirectional observer pattern which registers some observer to
some observable object, where the observable will provide message updates to the observer. In this projects case,
both the Player and Controller objects are Observer's and Observable to each other; meaning they can update one another
on state information. For more information regarding the message flow & message set through the socket interface,
I have provided message flow diagrams under the docs directory.
</p>

<p align="center">
The main new feature I have introduced is an automated controller engine for a single player mode. The automated engine
is pretty primitive, but surprisingly can still be pretty challenging. I could have made the engine more difficult by
calculating the set of "center" positions where the current largest ship could reside, but I believe the game would have
been impossible to play and win if I had implemented that as the only engine. Sometime in the future I may add a mode
selector capability to enable the ability to add that more advanced selection process.
</p>

<p align="center"> <img src="https://github.com/xTriixrx/Battleship/blob/master/imgs/battleship-gui.png"/> </p>

## Setup Instructions

<p align="center">
These instructions will assume you have Apache Maven installed to pull the dependencies needed in order to play the
game. If you intend to play with someone else, they will also need to perform these steps on their system and network
access must be configured in order to connect. Also keep in mind you will have to coordinate with the opponent acting
as the client in order to connect properly (server player must be running first).
</p>

### Linux Instruction's

```Bash
# Replace XX with the version of javafx that will be used
mkdir -p javafx-XX/lib

# Pull all dependencies needed defined by pom.xml into a lib folder
mvn -DoutputDirectory=lib dependency:copy-dependencies

# Move all javafx library jars to its own library folder
mv lib/javafx-* javafx-XX/lib

# If you are starting as the server to play an automated opponent on port 5000:
./scripts/startServer.sh javafx-XX/lib lib 5000 true

# If you are starting as the server to play a real opponent on port 5000:
./scripts/startServer.sh javafx-XX/lib lib 5000 false

# If you are starting as the client to play a real opponent on port 5000:
./scripts/startClient.sh javafx-XX/lib lib 192.168.1.1 5000 false
```

### Windows Instruction's

<p align="center">Windows Instructions will be provided shortly.</p>