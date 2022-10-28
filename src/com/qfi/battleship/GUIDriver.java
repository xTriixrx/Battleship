package com.qfi.battleship;

import java.util.Map;
import java.util.Objects;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;
import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;
import javafx.application.Application;

/**
 * GUIDriver is a generic Application class which will instantiate a Battleship FXML layout in coordination with
 * an associated communication logic thread and board controller class. One instance of the GUIDriver is for a single
 * player; the first player to start their application should start a server instance while the second instance should
 * be a client instance.
 *  
 * @author Vincent.Nigro
 * @version 1.0.0
 */
public class GUIDriver extends Application
{
	private int port = 0;
	private String host = "";
	private Runnable logic = null;
	private int instanceNumber = 0;
	private String instanceName = "";
	private Controller controller = null;
	
	private static final String TYPE = "type";
	private static final String ERROR = "error";
	private static final String HOST_ARG = "host";
	private static final String PORT_ARG = "port";
	private static final String AUTOMATED = "automatedOpponent";
	private static final Logger logger = LogManager.getLogger(GUIDriver.class);
	
	/**
	 * This method is called immediately after the Application class is loaded and constructed and prior to the 
	 * start method being called. This method is used to initialize data members needed prior to the construction
	 * of the scene/stage.
	 */
	@Override
	public void init()
	{
		String type = "";
		boolean automatedOpponent = false;
		Parameters params = getParameters();
		Map<String, String> kwargList = params.getNamed();

		type = kwargList.get(TYPE);
		host = kwargList.get(HOST_ARG);
		port = Integer.parseInt(kwargList.get(PORT_ARG));
		automatedOpponent = Boolean.parseBoolean(kwargList.get(AUTOMATED));
		
		logger.info("Host: " + host + ".");
		logger.info("Port: " + port + ".");
		logger.info("Instance Type: " + type + ".");
		logger.info("Automated opponent: " + automatedOpponent + ".");
		
		if (type.equalsIgnoreCase(Player.SERVER))
		{
			instanceNumber = Player.SERVER_ID;
		}
		else if (type.equalsIgnoreCase(Player.CLIENT))
		{
			instanceNumber = Player.CLIENT_ID;
		}
		else
		{
			// Invalid player type
			String invalid = "Invalid player type " + type + ". Shutting down driver.";
			logic = new Player(controller, instanceNumber, host, port);
			((Player) logic).infoBox(invalid, ERROR);
			logger.error(invalid);
			System.exit(1);
		}
		
		// Instantiate player logic, instanceName, and board controller
		controller = new BoardController(instanceNumber);
		logic = new Player(controller, instanceNumber, host, port);
		instanceName = ((Player) logic).getName();

		logger.debug("Controller ID: " + controller.getID() + ".");
		
		// If the automated opponent flag is set, an inverse automated player will also be instantiated.
		if (automatedOpponent)
		{
			if (type.equalsIgnoreCase(Player.SERVER))
			{
				startLogic(Player.SERVER);
				startAutomatedOpponent(Player.CLIENT_ID, Player.CLIENT, Player.CLIENT);
			}
			else if (type.equalsIgnoreCase(Player.CLIENT))
			{
				startAutomatedOpponent(Player.SERVER_ID, Player.SERVER, Player.SERVER);
				startLogic(Player.CLIENT);
			}
		}
		else
		{
			startLogic(type);
		}
	}
	
	/**
	 * The main entry point for all JavaFX applications the start method is called after the init method has returned
	 * and, after the system is ready for the application to begin running. This method will set our instantiated controller,
	 * load the Battleship layout FXML, and set the scene.
	 *
	 * @param primaryStage - The primaryStage that is passed from the JavaFX runtime.
	 */
	@Override
	public void start(Stage primaryStage)
	{
		Parent root = null;

		// Instantiate and load controller
		FXMLLoader loader = new FXMLLoader();
		loader.setController(controller);

		// Set the stage title and set the location of the layout to be loaded
		primaryStage.setTitle("Network Battleship " + instanceName);
		loader.setLocation(getClass().getResource("layout/BattleshipLayout.fxml"));

		try
		{
			// Load the layout configured
			root = loader.load();
		}
		catch(Exception e)
		{
			logger.error(e, e);
		}

		if (root != null)
		{
			// Create a scene based on the loaded layout, grab css resources and set the scene for the primary stage.
			Scene scene = new Scene(root, 800, 600);

			scene.getStylesheets().add(Objects.requireNonNull(
					getClass().getResource("styles/application.css")).toExternalForm());
			primaryStage.setScene(scene);

			// Depending on whose turn it is, create a pop-up signifying whose turn it is first.
			if (controller.getCurrentTurn() == instanceNumber)
			{
				((Player) logic).infoBox("It is your turn first!", "Player " + instanceNumber);
			}
			else
			{
				((Player) logic).infoBox("It is the opponents turn first...", "Player " + instanceNumber);
			}

			// Disables maximizing the primary stage
			primaryStage.maximizedProperty().addListener((observable, oldValue, newValue) ->
			{
				if (newValue)
				{
					primaryStage.setMaximized(false);
				}
			});

			// On close of stage, trigger the controller's shutdown function.
			primaryStage.setOnCloseRequest(event -> controller.shutdown());

			// Show the scene
			primaryStage.show();
		}
	}
	
	/**
	 * Initializes an automated opponents' appropriate threads in order to play against the user.
	 *
	 * @param playerID - A number representation of whether a client or server instance is initializing.
	 * @param automatedControllerName - A String representing the players' name.
	 * @param automatedThreadName - A String representing the threads' name.
	 */
	private void startAutomatedOpponent(int playerID, String automatedControllerName, String automatedThreadName)
	{
		// Instantiate automated controller and runnable player instance of either server or client playerID
		Controller automatedController = new AutomatedController(playerID);
		Player runnableOpponent = new Player(automatedController, playerID, host, port);
		runnableOpponent.setAutomated(true);
		
		logger.debug("Automated Controller ID: " + automatedController.getID() + ".");

		// Instantiate and start automated controller thread
		Thread automatedLogic = new Thread((Runnable) automatedController);
		automatedLogic.setName(automatedControllerName);
		automatedLogic.start();
		
		// Instantiate and start automated logic thread
		Thread automatedThread = new Thread(runnableOpponent);
		automatedThread.setName(automatedThreadName);
		automatedThread.start();
	}
	
	/**
	 * The main initialization logic for creating and starting a new logic player thread for either a client
	 * or a server.
	 *
	 * @param threadName - A String representing the type of player (server or client) that has started.
	 */
	private void startLogic(String threadName)
	{
		// Start the logic thread
		Thread logicThread = new Thread(logic);
		logicThread.setName(threadName);
		logicThread.start();
	}

	/**
	 * Called by the GUIDriverRunner class to inject command line arguments into the Application runtime.
	 * 
	 * @param args - Keyword arguments from the command line injected by the GUIDriverRunner class.
	 */
	public static void main(String[] args)
	{
		launch(args);
	}
}
