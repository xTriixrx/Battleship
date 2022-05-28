package com.qfi.battleship;

import java.util.Map;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;
import javax.swing.JOptionPane;
import javafx.application.Application;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * GUIDriver is an generic Application class which will instantiate a Battleship FXML layout in coordination with
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
	private String type = "";
	private Runnable logic = null;
	private int instanceNumber = 0;
	private String instanceName = "";
	private Controller controller = null;
	private Runnable runnableOpponent = null;
	private boolean automatedOpponent = false;
	private Controller automatedController = null;
	private Logger logger = LogManager.getLogger(GUIDriver.class);
	
	private static final String TYPE = "type";
	private static final String HOST = "host";
	private static final String PORT = "port";
	private static final String ERROR = "error";
	private static final String AUTOMATED = "automatedOpponent";
	
	/**
	 * This method is called immediately after the Application class is loaded and constructed and prior to the 
	 * start method being called. This method is used to initialize data members needed prior to the construction
	 * of the scene/stage.
	 */
	@Override
	public void init()
	{	
		Parameters params = getParameters();
		Map<String, String> kwargList = params.getNamed();
		
		host = kwargList.get(HOST);
		type = kwargList.get(TYPE);
		port = Integer.parseInt(kwargList.get(PORT));
		automatedOpponent = Boolean.parseBoolean(kwargList.get(AUTOMATED));
		
		logger.info("Host: {}.", host);
		logger.info("Port: {}.", port);
		logger.info("Instance Type: {}.", type);
		logger.info("Automated opponent: {}.", automatedOpponent);
		
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
			String invalid = "Invalid player type {}. Shutting down driver.";
			infoBox(invalid, ERROR);
			logger.error(invalid);
			System.exit(1);
		}
		
		// Instantiate player logic, instanceName, and board controller
		controller = new BoardController(instanceNumber);
		logic = new Player(controller, instanceNumber, host, port);
		instanceName = ((Player) logic).getName();
		
		logger.debug("Controller ID: " + controller.getID());
		
		//
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
	 */
	@Override
	public void start(Stage primaryStage) throws Exception
	{
		Parent root = null;
		Scene scene = null;
		FXMLLoader loader = null;
		
		try
		{
			// Instantiate and load controller
			loader = new FXMLLoader();
			loader.setController(controller);
			
			// Set the stage title and set the location of the layout to be loaded
			primaryStage.setTitle("Network Battleship " + instanceName);
			loader.setLocation(getClass().getResource("layout/BattleshipLayout.fxml"));
			
			// Load the layout configured
			root = loader.load();
			
			// Create a scene based on the loaded layout, grab css resources and set the scene for the primary stage.
			scene = new Scene(root, 800, 600);
			scene.getStylesheets().add(getClass().getResource("styles/application.css").toExternalForm());
			primaryStage.setScene(scene);
			
			// Depending on who's turn it is, create a pop up signifying whos turn it is first.
			if(controller.getCurrentTurn() == instanceNumber)
			{
				infoBox("It is your turn first!", "Player " + Integer.toString(instanceNumber));
			}
			else
			{
				infoBox("It is the opponents turn first...", "Player " + Integer.toString(instanceNumber));
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
		catch(Exception e)
		{
			logger.error(e, e);
		}
	}
	
	/**
	 * 
	 * @param playerID
	 */
	private void startAutomatedOpponent(int playerID, String automatedControllerName, String automatedThreadName)
	{
		// Instantiate automated controller and runnable player instance of either server or client playerID
		automatedController = new AutomatedController(playerID);
		runnableOpponent = new Player(automatedController, playerID, host, port);
		
		logger.debug("Automated Controller ID: " + automatedController.getID());
		
		Thread automatedLogic = new Thread((Runnable) automatedController);
		automatedLogic.setName(automatedControllerName);
		automatedLogic.start();
		
		// Instantiate and start automated logic thread
		Thread automatedThread = new Thread(runnableOpponent);
		automatedThread.setName(automatedThreadName);
		automatedThread.start();
	}
	
	/**
	 * 
	 */
	private void startLogic(String threadName)
	{
		// Start the logic thread
		Thread logicThread = new Thread(logic);
		logicThread.setName(threadName);
		logicThread.start();
	}
	
	/**
	 * An Java Swing pop up info box for showing basic pop up information to the user during the game.
	 * 
	 * @param infoMessage The message to be inserted into the pop up message.
	 * @param titleBar The partial title of the pop up box.
	 */
	public void infoBox(String infoMessage, String titleBar)
    {
        JOptionPane.showMessageDialog(null, infoMessage, instanceName + ": " + titleBar, JOptionPane.INFORMATION_MESSAGE);
    }
	
	/**
	 * Called by the GUIDriverRunner class to inject command line arguments into the Application runtime.
	 * 
	 * @param args Keyword arguments from the command line injected by the GUIDriverRunner class.
	 */
	public static void main(String[] args)
	{
		launch(args);
	}
}
