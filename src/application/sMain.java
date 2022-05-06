package application;

import javax.swing.JOptionPane;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class sMain extends Application {

	private static Server server;
	private static Scontroller controller;
	private static int count = 0;
	
	@Override
	public void start(Stage primaryStage) {
		try {
			FXMLLoader loader = new FXMLLoader();
			System.out.println(controller.getID());
			
			loader.setController(controller);

			primaryStage.setTitle("Network Battleship Server");
			loader.setLocation(getClass().getResource("/application/ServerBattleshipLayout.fxml"));
			Parent root = null;
			root = loader.load();
			
			Scene scene = new Scene(root, 800, 600);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			
			primaryStage.setScene(scene);
			if(controller.getTurn() == 2)
				infoBox("It is your turn first!", "Player 2");
			else
				infoBox("It is player 1's turn first...", "Player 2");
			primaryStage.maximizedProperty().addListener((observable, oldValue, newValue) -> {
	            if (newValue)
	                primaryStage.setMaximized(false);
	        });
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
    public static void infoBox(String infoMessage, String titleBar)
    {
        JOptionPane.showMessageDialog(null, infoMessage, "Server: " + titleBar, JOptionPane.INFORMATION_MESSAGE);
    }
	
	public static void launchGUI(String[] args, Server s, Scontroller con) {
		server = s;
		controller = con;
		launch(args);
	}
	
}
