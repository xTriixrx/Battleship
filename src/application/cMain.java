package application;
	
import java.net.UnknownHostException;

import javax.swing.JOptionPane;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;


public class cMain extends Application {
	private static Client client;
	private static BoardController controller;
	private static int count = 0;
	
	@Override
	public void start(Stage primaryStage) {
		try {
			FXMLLoader loader = new FXMLLoader();
			System.out.println(controller.getID());
			loader.setController(controller);

			primaryStage.setTitle("Network Battleship Client");
			loader.setLocation(getClass().getResource("/application/BattleshipLayout.fxml"));
			Parent root = null;
			root = loader.load();
			
			Scene scene = new Scene(root, 800, 600);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			
			primaryStage.setScene(scene);
			if(controller.getCurrentTurn() == 1)
				infoBox("It is your turn first!", "Player 1");
			else
				infoBox("It is player 2's turn first...", "Player 1");
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
        JOptionPane.showMessageDialog(null, infoMessage, "Client: " + titleBar, JOptionPane.INFORMATION_MESSAGE);
    }
	
	public static void launchGUI(String[] args, Client c, BoardController con) {
		client = c;
		controller = con;
		launch(args);
	}
	
}
