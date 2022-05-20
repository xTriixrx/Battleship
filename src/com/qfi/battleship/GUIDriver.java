package com.qfi.battleship;

import java.util.Map;

import javax.swing.JOptionPane;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GUIDriver extends Application
{
	private int port = 0;
	private String host = "";
	private String type = "";
	private Runnable logic = null;
	private int instanceNumber = 0;
	private String instanceName = "";
	private BoardController controller = null;
	
	private static final String TYPE = "type";
	private static final String HOST = "host";
	private static final String PORT = "port";
	private static final String SERVER = "server";
	private static final String CLIENT = "client";
	
	@Override
	public void init()
	{	
		Parameters params = getParameters();
		Map<String, String> kwargList = params.getNamed();
		
		System.out.println("Host: " + kwargList.get(HOST));
		System.out.println("Port: " + kwargList.get(PORT));
		System.out.println("Instance Type: " + kwargList.get(TYPE));
		
		host = kwargList.get(HOST);
		type = kwargList.get(TYPE);
		port = Integer.parseInt(kwargList.get(PORT));
		
		if (type.equalsIgnoreCase(SERVER))
		{
			instanceNumber = 2;
			logic = new Server(port);
			controller = ((Server) logic).getController();
			instanceName = SERVER.substring(0, 1).toUpperCase() + SERVER.substring(1);
		}
		else if (type.equalsIgnoreCase(CLIENT))
		{
			instanceNumber = 1;
			logic = new Client(host, port);
			controller = ((Client) logic).getController();
			instanceName = CLIENT.substring(0, 1).toUpperCase() + CLIENT.substring(1);
		}
		else
		{
			// Invalid number of players
		}
		
		Thread logicThread = new Thread(logic);
		logicThread.start();
		
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception
	{
		try {
			FXMLLoader loader = new FXMLLoader();
			System.out.println(controller.getID());
			loader.setController(controller);

			primaryStage.setTitle("Network Battleship " + instanceName);
			loader.setLocation(getClass().getResource("layout/BattleshipLayout.fxml"));
			Parent root = null;
			root = loader.load();
			
			Scene scene = new Scene(root, 800, 600);
			scene.getStylesheets().add(getClass().getResource("styles/application.css").toExternalForm());
			
			primaryStage.setScene(scene);
			if(controller.getCurrentTurn() == instanceNumber)
				infoBox("It is your turn first!", "Player " + Integer.toString(instanceNumber));
			else
				infoBox("It is the opponents turn first...", "Player " + Integer.toString(instanceNumber));
			primaryStage.maximizedProperty().addListener((observable, oldValue, newValue) -> {
	            if (newValue)
	                primaryStage.setMaximized(false);
	        });
			
			primaryStage.setOnCloseRequest(event -> controller.shutdown());
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void infoBox(String infoMessage, String titleBar)
    {
        JOptionPane.showMessageDialog(null, infoMessage, instanceName + ": " + titleBar, JOptionPane.INFORMATION_MESSAGE);
    }
	
	public static void main(String[] args)
	{
		launch(args);
	}
}
