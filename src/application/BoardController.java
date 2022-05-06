package application;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;

import javafx.collections.ObservableList;
import javafx.beans.value.ObservableValue;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.io.File;
import java.nio.file.Paths;

public class BoardController implements Initializable, Observer, Observable {

	@FXML
	private GridPane opponentGrid;
	@FXML
	private GridPane playerGrid;
	@FXML
	private ImageView pictureOne;
	@FXML
	private ImageView pictureTwo;
	@FXML
	private ImageView pictureThree;
	@FXML
	private ImageView pictureFour;
	@FXML
	private ImageView pictureFive;
	@FXML
	private AnchorPane anchorPane;

	@FXML
	private Text opponentCarrier;
	@FXML
	private Text opponentBattleship;
	@FXML
	private Text opponentCruiser;
	@FXML
	private Text opponentSubmarine;
	@FXML
	private Text opponentDestroyer;
	@FXML
	private Text playerCarrier;
	@FXML
	private Text playerBattleship;
	@FXML
	private Text playerCruiser;
	@FXML
	private Text playerSubmarine;
	@FXML
	private Text playerDestroyer;
	
	@FXML
	private Button autoShips;

	@FXML
	private ChoiceBox<String> OrientationChoice;
	private String orientation = "Horizontal";

	private Armada armada = new Armada();

	private ObservableList<Node> buttonList;

	private char mySymbol = 'A';
	private int currentTurn = 0;
	private int myTurn = 0;
	private int opponentTurn = 0;
	private static final int clientTurn = 1;
	private static final char clientSymbol = 'Z';
	private static final int serverTurn = 2;
	private static final char serverSymbol = 'X';
	private static Observer sobs;
	private static String toSend;
	private static String toRecieve;
	private static boolean myTurnFlag = true;
	private boolean isShipsSet = false;
	private boolean CarrierSunk = false;
	private boolean BattleshipSunk = false;
	private boolean CruiserSunk = false;
	private boolean SubmarineSunk = false;
	private boolean DestroyerSunk = false;
	
	private DragDropTwoController twoController;
	private DragDropThreeController threeController;
	private DragDropFourController fourController;
	private DragDropFiveController fiveController;
	
	//final URL resource = getClass().getResource("Explosion.wav");
	//final AudioClip EXclip = new AudioClip(resource.toString());
	
	/*public void playExplosion() {
		EXclip.play(1.0);
	}*/
	
	BoardController(int whoami)
	{
		if (whoami == 1) // client
		{
			myTurn = clientTurn;
			mySymbol = clientSymbol;
			opponentTurn = serverTurn;
		}
		else if (whoami == 2) // server
		{
			myTurn = serverTurn;
			mySymbol = serverSymbol;
			opponentTurn = clientTurn;
		}
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		buttonList = playerGrid.getChildren();
		for (Node node : opponentGrid.getChildren()) {
			if (node.getId() != null) {
				// System.out.println(node.getId());
				initMouseEvent((Button) node);
			}
		}
		
		twoController = new DragDropTwoController();
		threeController = new DragDropThreeController();
		fourController = new DragDropFourController();
		fiveController = new DragDropFiveController();
		
		autoShips.setStyle("-fx-background-color: white");
		pictureOne.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_ENTERED, PictureOneClickEvent);
		pictureTwo.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_ENTERED, PictureTwoClickEvent);
		pictureThree.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_ENTERED, PictureThreeClickEvent);
		pictureFour.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_ENTERED, PictureFourClickEvent);
		pictureFive.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_ENTERED, PictureFiveClickEvent);
		autoShips.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_CLICKED, automateArmada);
		OrientationChoice.getItems().addAll("Horizontal", "Vertical");
		OrientationChoice.getSelectionModel().selectFirst();

		OrientationChoice.getSelectionModel().selectedItemProperty()
				.addListener((ObservableValue<? extends String> observable, String oldValue,
						String newValue) -> orientation = newValue);
	}

	public void initMouseEvent(Button b) {
		b.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_CLICKED, mouseClickEvent);
	}

	public Armada getArmada() {
		return armada;
	}

	public void setIsShipsSet(boolean s) {
		isShipsSet = s;
	}

	public boolean getIsShipsSet() {
		return isShipsSet;
	}

	public int getCurrentTurn() {
		return currentTurn;
	}

	public void setCurrentTurn(int t) {
		currentTurn = t;
	}

	public int getID() {
		return System.identityHashCode(this);
	}

	private EventHandler<MouseEvent> mouseClickEvent = new EventHandler<MouseEvent>() {

		@Override
		public void handle(MouseEvent e) {
			System.out.println("CONTROLLER " + myTurn + ": current turn is: " + currentTurn);

			if (currentTurn == myTurn && isShipsSet) {
				toSend = ((Node) e.getTarget()).getId();
				toSend = new StringBuilder(toSend).append(mySymbol).toString();
				((Button) e.getTarget()).setDisable(true);
				((Button) e.getTarget()).setMouseTransparent(false);
				System.out.println(toSend);
				myTurnFlag = true;
				notifyObserver(toSend);
				pictureOne.setDisable(true);
				pictureTwo.setDisable(true);
				pictureThree.setDisable(true);
				pictureFour.setDisable(true);
				pictureFive.setDisable(true);
				autoShips.setDisable(true);
			}

		}
	};

	@Override
	public void notifyObserver(String str) {
		sobs.update(str);
	}

	public void updateOpponentGrid(String pos, String HM) {
		for (Node node : opponentGrid.getChildren()) {
			if (node.getId() != null) {
				if (node.getId().equals(pos)) {
					if (HM.equals("Hit")) {
						node.setStyle("-fx-background-color: red");
					} else if (HM.equals("Miss")) {
						node.setStyle("-fx-background-color: white");
					}
					break;
				}
			}
		}
		notifyObserver(Integer.toString(opponentTurn));
		currentTurn = opponentTurn;
	}

	public void updateplayerGrid(String pos, String HM) {
		for (Node node : playerGrid.getChildren()) {
			if (node.getId() != null) {
				if (node.getId().equals(pos)) {
					if (HM.equals("Hit")) {
						node.setDisable(true);
						node.setMouseTransparent(false);
						node.setStyle("-fx-background-color: red");
						if (!CarrierSunk && armada.isCarrierSunk()) {
							CarrierSunk = true;
							if(CarrierSunk && BattleshipSunk && CruiserSunk && 
									SubmarineSunk && DestroyerSunk) {
								System.out.println("CARRIER SUNK");
								playerCarrier.setStyle("-fx-text-fill: red;");
								playerCarrier.setStyle("-fx-strikethrough: true");
								notifyObserver("OVER");
							}
							else {
							CarrierSunk = true;
							System.out.println("CARRIER SUNK");
							playerCarrier.setStyle("-fx-text-fill: red;");
							playerCarrier.setStyle("-fx-strikethrough: true");
							notifyObserver("CARRIER");
							}
						}
						if (!BattleshipSunk && armada.isBattleshipSunk()) {
							BattleshipSunk = true;
							if(CarrierSunk && BattleshipSunk && CruiserSunk && 
									SubmarineSunk && DestroyerSunk) {
								System.out.println("BATTLESHIP SUNK");
								playerBattleship.setStyle("-fx-text-fill: red;");
								playerBattleship.setStyle("-fx-strikethrough: true");
								notifyObserver("OVER");
							}
							else {
							System.out.println("BATTLESHIP SUNK");
							playerBattleship.setStyle("-fx-text-fill: red;");
							playerBattleship.setStyle("-fx-strikethrough: true");
							notifyObserver("BATTLESHIP");
							}
						}
						if (!CruiserSunk && armada.isCruiserSunk()) {
							CruiserSunk = true;
							if(CarrierSunk && BattleshipSunk && CruiserSunk && 
									SubmarineSunk && DestroyerSunk) {
								System.out.println("CRUISER SUNK");
								playerCruiser.setStyle("-fx-text-fill: red;");
								playerCruiser.setStyle("-fx-strikethrough: true");
								notifyObserver("OVER");
							}
							else {
							System.out.println("CRUISER SUNK");
							playerCruiser.setStyle("-fx-text-fill: red;");
							playerCruiser.setStyle("-fx-strikethrough: true");
							notifyObserver("CRUISER");
							}
						}
						if (!SubmarineSunk && armada.isSubmarineSunk()) {
							SubmarineSunk = true;
							if(CarrierSunk && BattleshipSunk && CruiserSunk && 
									SubmarineSunk && DestroyerSunk) {
								System.out.println("SUBMARINE SUNK");
								playerSubmarine.setStyle("-fx-text-fill: red;");
								playerSubmarine.setStyle("-fx-strikethrough: true");
								notifyObserver("OVER");
							}
							else {
							System.out.println("SUBMARINE SUNK");
							playerSubmarine.setStyle("-fx-text-fill: red;");
							playerSubmarine.setStyle("-fx-strikethrough: true");
							notifyObserver("SUBMARINE");
							}
						}
						if (!DestroyerSunk && armada.isDestroyerSunk()) {
							DestroyerSunk = true;
							if(CarrierSunk && BattleshipSunk && CruiserSunk && 
									SubmarineSunk && DestroyerSunk) {
								System.out.println("DESTROYER SUNK");
								playerDestroyer.setStyle("-fx-text-fill: red;");
								playerDestroyer.setStyle("-fx-strikethrough: true");
								notifyObserver("OVER");
							}
							else {
							System.out.println("DESTROYER SUNK");
							playerDestroyer.setStyle("-fx-text-fill: red;");
							playerDestroyer.setStyle("-fx-strikethrough: true");
							notifyObserver("DESTROYER");
							}
						}
					} else if (HM.equals("Miss")) {
						node.setDisable(true);
						node.setMouseTransparent(false);
						node.setStyle("-fx-background-color: white");
					}
					break;
				}
			}
		}
	}

	@Override
	public void update(String s) {
		System.out.println("SC: Received " + s + ".");

		if (s.equals("SET")) {
			isShipsSet = true;
		}
		else if(s.equals("CARRIER")) {
			opponentCarrier.setStyle("-fx-text-fill: red;");
			opponentCarrier.setStyle("-fx-strikethrough: true");
		}
		else if(s.equals("BATTLESHIP")) {
			opponentBattleship.setStyle("-fx-text-fill: red;");
			opponentBattleship.setStyle("-fx-strikethrough: true");
		}
		else if(s.equals("CRUISER")) {
			opponentCruiser.setStyle("-fx-text-fill: red;");
			opponentCruiser.setStyle("-fx-strikethrough: true");
		}
		else if(s.equals("SUBMARINE")) {
			opponentSubmarine.setStyle("-fx-text-fill: red;");
			opponentSubmarine.setStyle("-fx-strikethrough: true");
		}
		else if(s.equals("DESTROYER")) {
			opponentDestroyer.setStyle("-fx-text-fill: red;");
			opponentDestroyer.setStyle("-fx-strikethrough: true");
		}
		else if (currentTurn == myTurn && myTurnFlag) {
			StringBuilder temp = new StringBuilder(toSend);
			String t = "";
			if (temp.length() == 4) {
				temp.deleteCharAt(3);
				t = temp.toString();
			} else if (temp.length() == 5) {
				temp.deleteCharAt(4);
				t = temp.toString();
			}

			/*
			 * if(s.length() == 4) { if(s.charAt(3) == '1') myTurn = 1; } else if(s.length()
			 * == 5) { if(s.charAt(4) == '1') myTurn = 1; }
			 */

			updateOpponentGrid(t, s);
		} else if (currentTurn == opponentTurn) {
			StringBuilder temp = new StringBuilder(s);
			String HorM = "";
			/*
			 * int ran = (int) (Math.random() * 2 + 1);
			 * 
			 * if(ran == 1) HorM = "Hit"; else if(ran == 2) HorM = "Miss";
			 */

			temp.setCharAt(0, 'P');
			String t = "";
			if (temp.length() == 4) {
				temp.deleteCharAt(3);
				t = temp.toString();
			} else if (temp.length() == 5) {
				temp.deleteCharAt(4);
				t = temp.toString();
			}

			boolean isHit = armada.calculateHit(t);
			armada.updateArmada(t);

			if (isHit)
				HorM = "Hit";
			else
				HorM = "Miss";

			updateplayerGrid(t, HorM);

			notifyObserver(HorM + Integer.toString(myTurn));
			currentTurn = myTurn;
			myTurnFlag = false;
		}

	}

	@Override
	public void registerObserver(Observer s) {
		sobs = s;

	}

	@Override
	public void removeObserver() {
		sobs = null;
	}

	/**/

	
	/*
	 * Initiates the automatic placement of ships
	 * Calls different functions for each ship
	 */
	private void automateArmadaPlacement() {
		pictureOne.setDisable(true);
		pictureTwo.setDisable(true);
		pictureThree.setDisable(true);
		pictureFour.setDisable(true);
		pictureFive.setDisable(true);
		ArrayList<String> usedButtons = new ArrayList<String>();

		for (int i = 0; i < 5; i++) {

			// Carrier
			if (i == 0) {
				placeCarrier(usedButtons);
//				for(int j = 0; j < usedButtons.size(); j++)
//				 System.out.println(usedButtons.get(j));
			} else if (i == 1) {
				placeBattleship(usedButtons);
			} else if (i == 2) {
				placeCruiser(usedButtons);
			} else if (i == 3) {
				placeSubmarine(usedButtons);
			} else if (i == 4) {
				placeDestroyer(usedButtons);
			} else
				System.exit(1);

		}
		notifyObserver("SHIPS");
	}

	/*
	 * Automcatically places the battleship 
	 * Chooses spot at random if no ship there
	 */
	private void placeBattleship(ArrayList<String> used) {
		ArrayList<String> battleship = new ArrayList<String>();
		boolean placeableButton = false;
		boolean placeableShip = false;
		String alignment = "";
		char L = ' ';
		int Bnum = 0;

		while (!placeableShip) {
			placeableButton = false;
			battleship.clear();
			while (!placeableButton) {
				int HorV = (int) (Math.random() * 2 + 1);

				if (HorV == 1)
					alignment = "Horizontal";
				else if (HorV == 2)
					alignment = "Vertical";

				int num = (int) (Math.random() * 10 + 1);
				int let = (int) (Math.random() * 10 + 1);

				switch (let) {
				case 1:
					L = 'A';
					break;
				case 2:
					L = 'B';
					break;
				case 3:
					L = 'C';
					break;
				case 4:
					L = 'D';
					break;
				case 5:
					L = 'E';
					break;
				case 6:
					L = 'F';
					break;
				case 7:
					L = 'G';
					break;
				case 8:
					L = 'H';
					break;
				case 9:
					L = 'I';
					break;
				case 10:
					L = 'J';
					break;
				default:
					break;
				}

				StringBuilder sb = new StringBuilder();
				// sb.append("P");
				sb.append(L);
			//num = 3;
				sb.append(Integer.toString(num));
				String buttonToInsert = (sb.toString());

			//buttonToInsert = "A3";

				if (alignment.equals("Horizontal")) {
//					System.out.println("Horizontal");
					if (buttonToInsert.charAt(0) == 'A')
						placeableButton = false;
					else if (buttonToInsert.charAt(0) == 'B')
						placeableButton = false;
					else if (buttonToInsert.charAt(0) == 'I')
						placeableButton = false;
					else if (buttonToInsert.charAt(0) == 'J')
						placeableButton = false;
					else {
						placeableButton = true;
					}
					if (placeableButton == true) {

						battleship.add(buttonToInsert);

						if (buttonToInsert.charAt(0) == 'C') {
							sb.setCharAt(0, 'A');
							battleship.add(sb.toString());

							sb.setCharAt(0, 'B');
							battleship.add(sb.toString());

							sb.setCharAt(0, 'D');
							battleship.add(sb.toString());


						}

						if (buttonToInsert.charAt(0) == 'D') {
							sb.setCharAt(0, 'B');
							battleship.add(sb.toString());

							sb.setCharAt(0, 'C');
							battleship.add(sb.toString());

							sb.setCharAt(0, 'E');
							battleship.add(sb.toString());


						}

						if (buttonToInsert.charAt(0) == 'E') {
							sb.setCharAt(0, 'C');
							battleship.add(sb.toString());

							sb.setCharAt(0, 'D');
							battleship.add(sb.toString());

							sb.setCharAt(0, 'F');
							battleship.add(sb.toString());


						}

						if (buttonToInsert.charAt(0) == 'F') {
							sb.setCharAt(0, 'D');
							battleship.add(sb.toString());

							sb.setCharAt(0, 'E');
							battleship.add(sb.toString());

							sb.setCharAt(0, 'G');
							battleship.add(sb.toString());

						}

						if (buttonToInsert.charAt(0) == 'G') {
							sb.setCharAt(0, 'F');
							battleship.add(sb.toString());
							
							sb.setCharAt(0, 'E');
							battleship.add(sb.toString());

							sb.setCharAt(0, 'H');
							battleship.add(sb.toString());


						}

						if (buttonToInsert.charAt(0) == 'H') {
							sb.setCharAt(0, 'G');
							battleship.add(sb.toString());

							sb.setCharAt(0, 'I');
							battleship.add(sb.toString());

							sb.setCharAt(0, 'J');
							battleship.add(sb.toString());


						}

					}
				} else if (alignment.equals("Vertical")) {
//					System.out.println("Vertical");
					if (buttonToInsert.length() == 2) {
						if (buttonToInsert.charAt(1) == '1') {
							placeableButton = false;
						} else if (buttonToInsert.charAt(1) == '2') {
							placeableButton = false;
						} else if (buttonToInsert.charAt(1) == '9') {
							placeableButton = false;
						} else {
							placeableButton = true;
						}
						if (placeableButton == true) {
							battleship.add(buttonToInsert);
							
							if (buttonToInsert.charAt(1) == '3') {
								sb.setCharAt(1, '1');
								battleship.add(sb.toString());

								sb.setCharAt(1, '2');
								battleship.add(sb.toString());

								sb.setCharAt(1, '4');
								battleship.add(sb.toString());


							}

							if (buttonToInsert.charAt(1) == '4') {
								sb.setCharAt(1, '2');
								battleship.add(sb.toString());

								sb.setCharAt(1, '3');
								battleship.add(sb.toString());

								sb.setCharAt(1, '5');
								battleship.add(sb.toString());


							}

							if (buttonToInsert.charAt(1) == '5') {
								sb.setCharAt(1, '3');
								battleship.add(sb.toString());

								sb.setCharAt(1, '4');
								battleship.add(sb.toString());

								sb.setCharAt(1, '6');
								battleship.add(sb.toString());


							}

							if (buttonToInsert.charAt(1) == '6') {
								sb.setCharAt(1, '4');
								battleship.add(sb.toString());

								sb.setCharAt(1, '5');
								battleship.add(sb.toString());

								sb.setCharAt(1, '7');
								battleship.add(sb.toString());


							}

							if (buttonToInsert.charAt(1) == '7') {
								sb.setCharAt(1, '5');
								battleship.add(sb.toString());

								sb.setCharAt(1, '6');
								battleship.add(sb.toString());

								sb.setCharAt(1, '8');
								battleship.add(sb.toString());


							}

							if (buttonToInsert.charAt(1) == '8') {
								sb.setCharAt(1, '7');
								battleship.add(sb.toString());

								sb.setCharAt(1, '9');
								battleship.add(sb.toString());

								sb.setCharAt(1, '1');
								sb.append('0');
								battleship.add(sb.toString());

							}
						}
					}
				}
			}
			
			
			boolean inUsed = false;
			for(int i = 0; i < used.size(); i++) {
				for(int j = 0; j < battleship.size(); j++) {
					if(used.get(i).equals(battleship.get(j))) {
						inUsed = true;
						break;
					}
				}
			}
			if(inUsed == true) {
				placeableShip = false;
			}else {
				placeableShip = true;
			}
		}
		
		
		for(int i = 0; i < battleship.size(); i++) {
			armada.addToBattleship(battleship.get(i));
		}
		
		for (int k = 0; k < buttonList.size(); k++) {
			for (int j = 0; j < 4; j++) {
				if (buttonList.get(k).getId() != null) {
					if (buttonList.get(k).getId().toString().length() == 3) {
						if (buttonList.get(k).getId().toString().charAt(1) == armada.getBattleShip().get(j).charAt(0)
								&& buttonList.get(k).getId().toString().charAt(2) == armada.getBattleShip().get(j)
										.charAt(1)
								&& armada.getBattleShip().get(j).length() == 2) {
							buttonList.get(k).setStyle("-fx-background-color: green");
							buttonList.get(k).setDisable(true);
						}
					} else if (buttonList.get(k).getId().toString().length() == 4) {
						if (buttonList.get(k).getId().toString().charAt(1) == armada.getBattleShip().get(j).charAt(0)
								&& buttonList.get(k).getId().toString().charAt(2) == armada.getBattleShip().get(j)
										.charAt(1)
										&& armada.getBattleShip().get(j).length() == 3
										&& buttonList.get(k).getId().toString().charAt(3) == armada.getBattleShip().get(j)
										.charAt(2)) {
							buttonList.get(k).setStyle("-fx-background-color: green");
							buttonList.get(k).setDisable(true);
						}
					}
				}
			}
		}
		for(int i = 0; i < battleship.size(); i++) {
			used.add(battleship.get(i));
		}
	}

	
	/*
	 * Automatically places the cruiser
	 * Chooses spot at random if no ship there
	 */
	private void placeCruiser(ArrayList<String> used) {
		ArrayList<String> cruiser = new ArrayList<String>();
		boolean placeableButton = false;
		boolean placeableShip = false;
		String alignment = "";
		char L = ' ';
		int Bnum = 0;

		while (!placeableShip) {
			placeableButton = false;
			cruiser.clear();
			while (!placeableButton) {
				int HorV = (int) (Math.random() * 2 + 1);

				if (HorV == 1)
					alignment = "Horizontal";
				else if (HorV == 2)
					alignment = "Vertical";
				//alignment = "Vertical";
				int num = (int) (Math.random() * 10 + 1);
				int let = (int) (Math.random() * 10 + 1);

				switch (let) {
				case 1:
					L = 'A';
					break;
				case 2:
					L = 'B';
					break;
				case 3:
					L = 'C';
					break;
				case 4:
					L = 'D';
					break;
				case 5:
					L = 'E';
					break;
				case 6:
					L = 'F';
					break;
				case 7:
					L = 'G';
					break;
				case 8:
					L = 'H';
					break;
				case 9:
					L = 'I';
					break;
				case 10:
					L = 'J';
					break;
				default:
					break;
				}

				StringBuilder sb = new StringBuilder();
				// sb.append("P");
				//L = 'J';
				sb.append(L);
				
				//num = 9;
				sb.append(Integer.toString(num));
				String buttonToInsert = (sb.toString());

				//buttonToInsert = "J9";

				if (alignment.equals("Horizontal")) {
//					System.out.println("Horizontal");
					if (buttonToInsert.charAt(0) == 'A')
						placeableButton = false;
					else if (buttonToInsert.charAt(0) == 'J')
						placeableButton = false;
					else {
						placeableButton = true;
					}
					if (placeableButton == true) {

						cruiser.add(buttonToInsert);
						if (buttonToInsert.charAt(0) == 'B') {
							sb.setCharAt(0, 'A');
							cruiser.add(sb.toString());

							sb.setCharAt(0, 'C');
							cruiser.add(sb.toString());


						}
						
						if (buttonToInsert.charAt(0) == 'C') {
							sb.setCharAt(0, 'A');
							cruiser.add(sb.toString());

							sb.setCharAt(0, 'B');
							cruiser.add(sb.toString());

						}

						if (buttonToInsert.charAt(0) == 'D') {
							sb.setCharAt(0, 'B');
							cruiser.add(sb.toString());

							sb.setCharAt(0, 'C');
							cruiser.add(sb.toString());

						}

						if (buttonToInsert.charAt(0) == 'E') {
							sb.setCharAt(0, 'C');
							cruiser.add(sb.toString());

							sb.setCharAt(0, 'D');
							cruiser.add(sb.toString());

						}

						if (buttonToInsert.charAt(0) == 'F') {
							sb.setCharAt(0, 'D');
							cruiser.add(sb.toString());

							sb.setCharAt(0, 'E');
							cruiser.add(sb.toString());


						}

						if (buttonToInsert.charAt(0) == 'G') {
							sb.setCharAt(0, 'F');
							cruiser.add(sb.toString());
							
							sb.setCharAt(0, 'E');
							cruiser.add(sb.toString());

						}

						if (buttonToInsert.charAt(0) == 'H') {
							sb.setCharAt(0, 'G');
							cruiser.add(sb.toString());

							sb.setCharAt(0, 'I');
							cruiser.add(sb.toString());


						}
						if (buttonToInsert.charAt(0) == 'I') {
							sb.setCharAt(0, 'H');
							cruiser.add(sb.toString());

							sb.setCharAt(0, 'J');
							cruiser.add(sb.toString());


						}

					}
				} else if (alignment.equals("Vertical")) {
//					System.out.println("Vertical");
					if (buttonToInsert.length() == 2) {
						if (buttonToInsert.charAt(1) == '1') {
							placeableButton = false;
						} else if (buttonToInsert.charAt(1) == '1' && buttonToInsert.charAt(2) == '0') {
							placeableButton = false;
						} else {
							placeableButton = true;
						}
						if (placeableButton == true) {
							cruiser.add(buttonToInsert);
							if (buttonToInsert.charAt(1) == '2') {
								sb.setCharAt(1, '1');
								cruiser.add(sb.toString());

								sb.setCharAt(1, '3');
								cruiser.add(sb.toString());

							}
							
							if (buttonToInsert.charAt(1) == '3') {
								sb.setCharAt(1, '1');
								cruiser.add(sb.toString());

								sb.setCharAt(1, '2');
								cruiser.add(sb.toString());



							}

							if (buttonToInsert.charAt(1) == '4') {
								sb.setCharAt(1, '2');
								cruiser.add(sb.toString());

								sb.setCharAt(1, '3');
								cruiser.add(sb.toString());


							}

							if (buttonToInsert.charAt(1) == '5') {
								sb.setCharAt(1, '3');
								cruiser.add(sb.toString());

								sb.setCharAt(1, '4');
								cruiser.add(sb.toString());



							}

							if (buttonToInsert.charAt(1) == '6') {
								sb.setCharAt(1, '4');
								cruiser.add(sb.toString());

								sb.setCharAt(1, '5');
								cruiser.add(sb.toString());


							}

							if (buttonToInsert.charAt(1) == '7') {
								sb.setCharAt(1, '5');
								cruiser.add(sb.toString());

								sb.setCharAt(1, '6');
								cruiser.add(sb.toString());



							}

							if (buttonToInsert.charAt(1) == '8') {
								sb.setCharAt(1, '7');
								cruiser.add(sb.toString());

								sb.setCharAt(1, '9');
								cruiser.add(sb.toString());


							}
							if (buttonToInsert.charAt(1) == '9') {

								sb.setCharAt(1, '8');
								cruiser.add(sb.toString());

								sb.setCharAt(1, '1');
								sb.append('0');
								cruiser.add(sb.toString());

							}
						}
					}
				}
			}
			
			boolean inUsed = false;
			for(int i = 0; i < used.size(); i++) {
				for(int j = 0; j < cruiser.size(); j++) {
					if(used.get(i).equals(cruiser.get(j))) {
						inUsed = true;
						break;
					}
				}
			}
			if(inUsed == true) {
				placeableShip = false;
			}else {
				placeableShip = true;
			}
		}
		
		
		for(int i = 0; i < cruiser.size(); i++) {
			armada.addToCruiser(cruiser.get(i));
		}
		
		for (int k = 0; k < buttonList.size(); k++) {
			for (int j = 0; j < 3; j++) {
				if (buttonList.get(k).getId() != null) {
					if (buttonList.get(k).getId().toString().length() == 3) {
						if ( buttonList.get(k).getId().toString().charAt(1) == armada.getCruiser().get(j).charAt(0)
								&& buttonList.get(k).getId().toString().charAt(2) == armada.getCruiser().get(j)
										.charAt(1) && armada.getCruiser().get(j).length() == 2
								) {
							buttonList.get(k).setDisable(true);
							buttonList.get(k).setStyle("-fx-background-color: green");
						}
					} else if (buttonList.get(k).getId().toString().length() == 4) {
						if (buttonList.get(k).getId().toString().charAt(1) == armada.getCruiser().get(j).charAt(0)
								&& buttonList.get(k).getId().toString().charAt(2) == armada.getCruiser().get(j)
										.charAt(1)
										&& armada.getCruiser().get(j).length() == 3
										&& buttonList.get(k).getId().toString().charAt(3) == armada.getCruiser().get(j)
										.charAt(2)) {
							buttonList.get(k).setDisable(true);
							buttonList.get(k).setStyle("-fx-background-color: green");
						}
					}
				}
			}
		}
		for(int i = 0; i < cruiser.size(); i++) {
			used.add(cruiser.get(i));
		}
	}

	/*
	 * Automatically places the Submarine
	 * Chooses spot at random if no ship there
	 */
	private void placeSubmarine(ArrayList<String> used) {

		ArrayList<String> submarine = new ArrayList<String>();
		boolean placeableButton = false;
		boolean placeableShip = false;
		String alignment = "";
		char L = ' ';
		int Bnum = 0;

		while (!placeableShip) {
			placeableButton = false;
			submarine.clear();
			while (!placeableButton) {
				int HorV = (int) (Math.random() * 2 + 1);

				if (HorV == 1)
					alignment = "Horizontal";
				else if (HorV == 2)
					alignment = "Vertical";
				//alignment = "Vertical";
				int num = (int) (Math.random() * 10 + 1);
				int let = (int) (Math.random() * 10 + 1);

				switch (let) {
				case 1:
					L = 'A';
					break;
				case 2:
					L = 'B';
					break;
				case 3:
					L = 'C';
					break;
				case 4:
					L = 'D';
					break;
				case 5:
					L = 'E';
					break;
				case 6:
					L = 'F';
					break;
				case 7:
					L = 'G';
					break;
				case 8:
					L = 'H';
					break;
				case 9:
					L = 'I';
					break;
				case 10:
					L = 'J';
					break;
				default:
					break;
				}

				StringBuilder sb = new StringBuilder();
				// sb.append("P");
				//L = 'J';
				sb.append(L);
				
				//num = 9;
				sb.append(Integer.toString(num));
				String buttonToInsert = (sb.toString());

				//buttonToInsert = "J9";

				if (alignment.equals("Horizontal")) {
//					System.out.println("Horizontal");
					if (buttonToInsert.charAt(0) == 'A')
						placeableButton = false;
					else if (buttonToInsert.charAt(0) == 'J')
						placeableButton = false;
					else {
						placeableButton = true;
					}
					if (placeableButton == true) {

						submarine.add(buttonToInsert);
						if (buttonToInsert.charAt(0) == 'B') {
							sb.setCharAt(0, 'A');
							submarine.add(sb.toString());

							sb.setCharAt(0, 'C');
							submarine.add(sb.toString());


						}
						
						if (buttonToInsert.charAt(0) == 'C') {
							sb.setCharAt(0, 'A');
							submarine.add(sb.toString());

							sb.setCharAt(0, 'B');
							submarine.add(sb.toString());

						}

						if (buttonToInsert.charAt(0) == 'D') {
							sb.setCharAt(0, 'B');
							submarine.add(sb.toString());

							sb.setCharAt(0, 'C');
							submarine.add(sb.toString());

						}

						if (buttonToInsert.charAt(0) == 'E') {
							sb.setCharAt(0, 'C');
							submarine.add(sb.toString());

							sb.setCharAt(0, 'D');
							submarine.add(sb.toString());

						}

						if (buttonToInsert.charAt(0) == 'F') {
							sb.setCharAt(0, 'D');
							submarine.add(sb.toString());

							sb.setCharAt(0, 'E');
							submarine.add(sb.toString());


						}

						if (buttonToInsert.charAt(0) == 'G') {
							sb.setCharAt(0, 'F');
							submarine.add(sb.toString());
							
							sb.setCharAt(0, 'E');
							submarine.add(sb.toString());

						}

						if (buttonToInsert.charAt(0) == 'H') {
							sb.setCharAt(0, 'G');
							submarine.add(sb.toString());

							sb.setCharAt(0, 'I');
							submarine.add(sb.toString());


						}
						if (buttonToInsert.charAt(0) == 'I') {
							sb.setCharAt(0, 'H');
							submarine.add(sb.toString());

							sb.setCharAt(0, 'J');
							submarine.add(sb.toString());


						}

					}
				} else if (alignment.equals("Vertical")) {
//					System.out.println("Vertical");
					if (buttonToInsert.length() == 2) {
						if (buttonToInsert.charAt(1) == '1') {
							placeableButton = false;
						} else if (buttonToInsert.charAt(1) == '1' && buttonToInsert.charAt(2) == '0') {
							placeableButton = false;
						} else {
							placeableButton = true;
						}
						if (placeableButton == true) {
							submarine.add(buttonToInsert);
							if (buttonToInsert.charAt(1) == '2') {
								sb.setCharAt(1, '1');
								submarine.add(sb.toString());

								sb.setCharAt(1, '3');
								submarine.add(sb.toString());

							}
							
							if (buttonToInsert.charAt(1) == '3') {
								sb.setCharAt(1, '1');
								submarine.add(sb.toString());

								sb.setCharAt(1, '2');
								submarine.add(sb.toString());



							}

							if (buttonToInsert.charAt(1) == '4') {
								sb.setCharAt(1, '2');
								submarine.add(sb.toString());

								sb.setCharAt(1, '3');
								submarine.add(sb.toString());


							}

							if (buttonToInsert.charAt(1) == '5') {
								sb.setCharAt(1, '3');
								submarine.add(sb.toString());

								sb.setCharAt(1, '4');
								submarine.add(sb.toString());



							}

							if (buttonToInsert.charAt(1) == '6') {
								sb.setCharAt(1, '4');
								submarine.add(sb.toString());

								sb.setCharAt(1, '5');
								submarine.add(sb.toString());


							}

							if (buttonToInsert.charAt(1) == '7') {
								sb.setCharAt(1, '5');
								submarine.add(sb.toString());

								sb.setCharAt(1, '6');
								submarine.add(sb.toString());



							}

							if (buttonToInsert.charAt(1) == '8') {
								sb.setCharAt(1, '7');
								submarine.add(sb.toString());

								sb.setCharAt(1, '9');
								submarine.add(sb.toString());


							}
							if (buttonToInsert.charAt(1) == '9') {

								sb.setCharAt(1, '8');
								submarine.add(sb.toString());

								sb.setCharAt(1, '1');
								sb.append('0');
								submarine.add(sb.toString());

							}
						}
					}
				}
			}
			
			boolean inUsed = false;
			for(int i = 0; i < used.size(); i++) {
				for(int j = 0; j < submarine.size(); j++) {
					if(used.get(i).equals(submarine.get(j))) {
						inUsed = true;
						break;
					}
				}
			}
			if(inUsed == true) {
				placeableShip = false;
			}else {
				placeableShip = true;
			}
		}
		
		
		for(int i = 0; i < submarine.size(); i++) {
			armada.addToSubmarine(submarine.get(i));
		}
		
		for (int k = 0; k < buttonList.size(); k++) {
			for (int j = 0; j < 3; j++) {
				if (buttonList.get(k).getId() != null) {
					if (buttonList.get(k).getId().toString().length() == 3) {
						if ( buttonList.get(k).getId().toString().charAt(1) == armada.getSubmarine().get(j).charAt(0)
								&& buttonList.get(k).getId().toString().charAt(2) == armada.getSubmarine().get(j)
										.charAt(1) && armada.getSubmarine().get(j).length() == 2
								) {
							buttonList.get(k).setStyle("-fx-background-color: green");
							buttonList.get(k).setDisable(true);
						}
					} else if (buttonList.get(k).getId().toString().length() == 4) {
						if (buttonList.get(k).getId().toString().charAt(1) == armada.getSubmarine().get(j).charAt(0)
								&& buttonList.get(k).getId().toString().charAt(2) == armada.getSubmarine().get(j)
										.charAt(1)
										&& armada.getSubmarine().get(j).length() == 3
										&& buttonList.get(k).getId().toString().charAt(3) == armada.getSubmarine().get(j)
										.charAt(2)) {
							buttonList.get(k).setStyle("-fx-background-color: green");
							buttonList.get(k).setDisable(true);
							
						}
					}
				}
			}
		}
		for(int i = 0; i < submarine.size(); i++) {
			used.add(submarine.get(i));
		}
	
	}

	/*
	 * Automatically places the Destroyer
	 * Chooses spot at random if no ship there
	 */
	private void placeDestroyer(ArrayList<String> used) {


		ArrayList<String> destroyer = new ArrayList<String>();
		boolean placeableButton = false;
		boolean placeableShip = false;
		String alignment = "";
		char L = ' ';
		int Bnum = 0;

		while (!placeableShip) {
			placeableButton = false;
			destroyer.clear();
			while (!placeableButton) {
				int HorV = (int) (Math.random() * 2 + 1);

				if (HorV == 1)
					alignment = "Horizontal";
				else if (HorV == 2)
					alignment = "Vertical";
				//alignment = "Vertical";
				int num = (int) (Math.random() * 10 + 1);
				int let = (int) (Math.random() * 10 + 1);

				switch (let) {
				case 1:
					L = 'A';
					break;
				case 2:
					L = 'B';
					break;
				case 3:
					L = 'C';
					break;
				case 4:
					L = 'D';
					break;
				case 5:
					L = 'E';
					break;
				case 6:
					L = 'F';
					break;
				case 7:
					L = 'G';
					break;
				case 8:
					L = 'H';
					break;
				case 9:
					L = 'I';
					break;
				case 10:
					L = 'J';
					break;
				default:
					break;
				}

				StringBuilder sb = new StringBuilder();
				// sb.append("P");
				//L = 'J';
				sb.append(L);
				
				//num = 9;
				sb.append(Integer.toString(num));
				String buttonToInsert = (sb.toString());

				//buttonToInsert = "J9";

				if (alignment.equals("Horizontal")) {
//					System.out.println("Horizontal");
					if (buttonToInsert.charAt(0) == 'A')
						placeableButton = false;
					else {
						placeableButton = true;
					}
					if (placeableButton == true) {

						destroyer.add(buttonToInsert);
						if (buttonToInsert.charAt(0) == 'B') {

							sb.setCharAt(0, 'A');
							destroyer.add(sb.toString());


						}
						
						if (buttonToInsert.charAt(0) == 'C') {

							sb.setCharAt(0, 'B');
							destroyer.add(sb.toString());

						}

						if (buttonToInsert.charAt(0) == 'D') {
							sb.setCharAt(0, 'C');
							destroyer.add(sb.toString());

						}

						if (buttonToInsert.charAt(0) == 'E') {
							sb.setCharAt(0, 'D');
							destroyer.add(sb.toString());

						}

						if (buttonToInsert.charAt(0) == 'F') {
							sb.setCharAt(0, 'E');
							destroyer.add(sb.toString());


						}

						if (buttonToInsert.charAt(0) == 'G') {
							sb.setCharAt(0, 'F');
							destroyer.add(sb.toString());

						}

						if (buttonToInsert.charAt(0) == 'H') {
							sb.setCharAt(0, 'G');
							destroyer.add(sb.toString());


						}
						if (buttonToInsert.charAt(0) == 'I') {
							sb.setCharAt(0, 'H');
							destroyer.add(sb.toString());


						}if (buttonToInsert.charAt(0) == 'J') {
							sb.setCharAt(0, 'I');
							destroyer.add(sb.toString());
						}

					}
				} else if (alignment.equals("Vertical")) {
//					System.out.println("Vertical");
					if (buttonToInsert.length() == 2) {
						placeableButton = true;
						if (placeableButton == true) {
							destroyer.add(buttonToInsert);
							
							if (buttonToInsert.charAt(1) == '1') {
								sb.setCharAt(1, '2');
								destroyer.add(sb.toString());

							}
							if (buttonToInsert.charAt(1) == '2') {
								sb.setCharAt(1, '3');
								destroyer.add(sb.toString());

							}
							
							if (buttonToInsert.charAt(1) == '3') {
								sb.setCharAt(1, '4');
								destroyer.add(sb.toString());



							}

							if (buttonToInsert.charAt(1) == '4') {
								sb.setCharAt(1, '5');
								destroyer.add(sb.toString());


							}

							if (buttonToInsert.charAt(1) == '5') {
								sb.setCharAt(1, '6');
								destroyer.add(sb.toString());



							}

							if (buttonToInsert.charAt(1) == '6') {
								sb.setCharAt(1, '7');
								destroyer.add(sb.toString());


							}

							if (buttonToInsert.charAt(1) == '7') {
								sb.setCharAt(1, '8');
								destroyer.add(sb.toString());



							}

							if (buttonToInsert.charAt(1) == '8') {
								sb.setCharAt(1, '9');
								destroyer.add(sb.toString());


							}
							if (buttonToInsert.charAt(1) == '9') {
								sb.setCharAt(1, '1');
								sb.append('0');
								destroyer.add(sb.toString());

							}
						}
					}else if(buttonToInsert.length() == 3) {
						if (buttonToInsert.charAt(1) == '1' && buttonToInsert.charAt(2) == '0') {
							placeableButton = false;
						}
					}
				}
			}
			
			boolean inUsed = false;
			for(int i = 0; i < used.size(); i++) {
				for(int j = 0; j < destroyer.size(); j++) {
					if(used.get(i).equals(destroyer.get(j))) {
						inUsed = true;
						break;
					}
				}
			}
			if(inUsed == true) {
				placeableShip = false;
			}else {
				placeableShip = true;
			}
		}
		
		
		for(int i = 0; i < destroyer.size(); i++) {
			armada.addToDestroyer(destroyer.get(i));
		}
		
		for (int k = 0; k < buttonList.size(); k++) {
			for (int j = 0; j < 2; j++) {
				if (buttonList.get(k).getId() != null) {
					if (buttonList.get(k).getId().toString().length() == 3) {
						if ( buttonList.get(k).getId().toString().charAt(1) == armada.getDestroyer().get(j).charAt(0)
								&& buttonList.get(k).getId().toString().charAt(2) == armada.getDestroyer().get(j)
										.charAt(1) && armada.getDestroyer().get(j).length() == 2
								) {
							buttonList.get(k).setDisable(true);
							buttonList.get(k).setStyle("-fx-background-color: green");
						}
					} else if (buttonList.get(k).getId().toString().length() == 4) {
						if (buttonList.get(k).getId().toString().charAt(1) == armada.getDestroyer().get(j).charAt(0)
								&& buttonList.get(k).getId().toString().charAt(2) == armada.getDestroyer().get(j)
										.charAt(1)
										&& armada.getDestroyer().get(j).length() == 3
										&& buttonList.get(k).getId().toString().charAt(3) == armada.getDestroyer().get(j)
										.charAt(2)) {
							buttonList.get(k).setDisable(true);
							buttonList.get(k).setStyle("-fx-background-color: green");
						}
					}
				}
			}
		}
		for(int i = 0; i < destroyer.size(); i++) {
			used.add(destroyer.get(i));
		}
	
	
	}

	/*
	 * Automatically places the Carrier
	 * Chooses spot at random if no ship there
	 */
	private void placeCarrier(ArrayList<String> used) {
		boolean placeableButton = false;
		String alignment = "";
		char L = ' ';
		int Bnum = 0;

		while (!placeableButton) {
			int HorV = (int) (Math.random() * 2 + 1);

			if (HorV == 1)
				alignment = "Horizontal";
			else if (HorV == 2)
				alignment = "Vertical";
			//alignment = "Vertical";

			int num = (int) (Math.random() * 10 + 1);
			int let = (int) (Math.random() * 10 + 1);

			switch (let) {
			case 1:
				L = 'A';
				break;
			case 2:
				L = 'B';
				break;
			case 3:
				L = 'C';
				break;
			case 4:
				L = 'D';
				break;
			case 5:
				L = 'E';
				break;
			case 6:
				L = 'F';
				break;
			case 7:
				L = 'G';
				break;
			case 8:
				L = 'H';
				break;
			case 9:
				L = 'I';
				break;
			case 10:
				L = 'J';
				break;
			default:
				break;
			}

			StringBuilder sb = new StringBuilder();
			// sb.append("P");
			//L = 'A';
			sb.append(L);
			//num = 3;
			sb.append(Integer.toString(num));
			String buttonToInsert = (sb.toString());

			//buttonToInsert = "A3";

			if (alignment.equals("Horizontal")) {
//				System.out.println("Horizontal");
				if (buttonToInsert.charAt(0) == 'A')
					placeableButton = false;
				else if (buttonToInsert.charAt(0) == 'B')
					placeableButton = false;
				else if (buttonToInsert.charAt(0) == 'I')
					placeableButton = false;
				else if (buttonToInsert.charAt(0) == 'J')
					placeableButton = false;
				else {
					placeableButton = true;
				}
				if (placeableButton == true) {
					armada.addToCarrier(buttonToInsert);
					used.add(buttonToInsert);

					if (buttonToInsert.charAt(0) == 'C') {
						sb.setCharAt(0, 'A');
						armada.addToCarrier(sb.toString());
						used.add(sb.toString());

						sb.setCharAt(0, 'B');
						armada.addToCarrier(sb.toString());
						used.add(sb.toString());

						sb.setCharAt(0, 'D');
						armada.addToCarrier(sb.toString());
						used.add(sb.toString());

						sb.setCharAt(0, 'E');
						armada.addToCarrier(sb.toString());
						used.add(sb.toString());

					}

					if (buttonToInsert.charAt(0) == 'D') {
						sb.setCharAt(0, 'B');
						armada.addToCarrier(sb.toString());
						used.add(sb.toString());

						sb.setCharAt(0, 'C');
						armada.addToCarrier(sb.toString());
						used.add(sb.toString());

						sb.setCharAt(0, 'E');
						armada.addToCarrier(sb.toString());
						used.add(sb.toString());

						sb.setCharAt(0, 'F');
						armada.addToCarrier(sb.toString());
						used.add(sb.toString());

					}

					if (buttonToInsert.charAt(0) == 'E') {
						sb.setCharAt(0, 'C');
						armada.addToCarrier(sb.toString());
						used.add(sb.toString());

						sb.setCharAt(0, 'D');
						armada.addToCarrier(sb.toString());
						used.add(sb.toString());

						sb.setCharAt(0, 'F');
						armada.addToCarrier(sb.toString());
						used.add(sb.toString());

						sb.setCharAt(0, 'G');
						armada.addToCarrier(sb.toString());
						used.add(sb.toString());

					}

					if (buttonToInsert.charAt(0) == 'F') {
						sb.setCharAt(0, 'D');
						armada.addToCarrier(sb.toString());
						used.add(sb.toString());

						sb.setCharAt(0, 'E');
						armada.addToCarrier(sb.toString());
						used.add(sb.toString());

						sb.setCharAt(0, 'G');
						armada.addToCarrier(sb.toString());
						used.add(sb.toString());

						sb.setCharAt(0, 'H');
						armada.addToCarrier(sb.toString());
						used.add(sb.toString());

					}

					if (buttonToInsert.charAt(0) == 'G') {
						sb.setCharAt(0, 'F');
						armada.addToCarrier(sb.toString());
						used.add(sb.toString());

						sb.setCharAt(0, 'E');
						armada.addToCarrier(sb.toString());
						used.add(sb.toString());

						sb.setCharAt(0, 'H');
						armada.addToCarrier(sb.toString());
						used.add(sb.toString());

						sb.setCharAt(0, 'I');
						armada.addToCarrier(sb.toString());
						used.add(sb.toString());

					}

					if (buttonToInsert.charAt(0) == 'H') {
						sb.setCharAt(0, 'F');
						armada.addToCarrier(sb.toString());
						used.add(sb.toString());

						sb.setCharAt(0, 'G');
						armada.addToCarrier(sb.toString());
						used.add(sb.toString());

						sb.setCharAt(0, 'I');
						armada.addToCarrier(sb.toString());
						used.add(sb.toString());

						sb.setCharAt(0, 'J');
						armada.addToCarrier(sb.toString());
						used.add(sb.toString());

					}

				}
			} else if (alignment.equals("Vertical")) {
//				System.out.println("Vertical");
				if (buttonToInsert.length() == 2) {
					if (buttonToInsert.charAt(1) == '1') {
						placeableButton = false;
					} else if (buttonToInsert.charAt(1) == '2') {
						placeableButton = false;
					} else if (buttonToInsert.charAt(1) == '9') {
						placeableButton = false;
					} else {
						placeableButton = true;
					}
					if (placeableButton == true) {
						armada.addToCarrier(buttonToInsert);
						if (buttonToInsert.charAt(1) == '3') {
							sb.setCharAt(1, '1');
							armada.addToCarrier(sb.toString());
							used.add(sb.toString());

							sb.setCharAt(1, '2');
							armada.addToCarrier(sb.toString());
							used.add(sb.toString());

							sb.setCharAt(1, '4');
							armada.addToCarrier(sb.toString());
							used.add(sb.toString());

							sb.setCharAt(1, '5');
							armada.addToCarrier(sb.toString());
							used.add(sb.toString());

						}

						if (buttonToInsert.charAt(1) == '4') {
							sb.setCharAt(1, '2');
							armada.addToCarrier(sb.toString());
							used.add(sb.toString());

							sb.setCharAt(1, '3');
							armada.addToCarrier(sb.toString());
							used.add(sb.toString());

							sb.setCharAt(1, '5');
							armada.addToCarrier(sb.toString());
							used.add(sb.toString());

							sb.setCharAt(1, '6');
							armada.addToCarrier(sb.toString());
							used.add(sb.toString());

						}

						if (buttonToInsert.charAt(1) == '5') {
							sb.setCharAt(1, '3');
							armada.addToCarrier(sb.toString());
							used.add(sb.toString());

							sb.setCharAt(1, '4');
							armada.addToCarrier(sb.toString());
							used.add(sb.toString());

							sb.setCharAt(1, '6');
							armada.addToCarrier(sb.toString());
							used.add(sb.toString());

							sb.setCharAt(1, '7');
							armada.addToCarrier(sb.toString());
							used.add(sb.toString());

						}

						if (buttonToInsert.charAt(1) == '6') {
							sb.setCharAt(1, '4');
							armada.addToCarrier(sb.toString());
							used.add(sb.toString());

							sb.setCharAt(1, '5');
							armada.addToCarrier(sb.toString());
							used.add(sb.toString());

							sb.setCharAt(1, '7');
							armada.addToCarrier(sb.toString());
							used.add(sb.toString());

							sb.setCharAt(1, '8');
							armada.addToCarrier(sb.toString());
							used.add(sb.toString());

						}

						if (buttonToInsert.charAt(1) == '7') {
							sb.setCharAt(1, '5');
							armada.addToCarrier(sb.toString());
							used.add(sb.toString());

							sb.setCharAt(1, '6');
							armada.addToCarrier(sb.toString());
							used.add(sb.toString());

							sb.setCharAt(1, '8');
							armada.addToCarrier(sb.toString());
							used.add(sb.toString());

							sb.setCharAt(1, '9');
							armada.addToCarrier(sb.toString());
							used.add(sb.toString());

						}

						if (buttonToInsert.charAt(1) == '8') {
							sb.setCharAt(1, '6');
							armada.addToCarrier(sb.toString());
							used.add(sb.toString());

							sb.setCharAt(1, '7');
							armada.addToCarrier(sb.toString());
							used.add(sb.toString());

							sb.setCharAt(1, '9');
							armada.addToCarrier(sb.toString());
							used.add(sb.toString());

							sb.setCharAt(1, '1');
							sb.append('0');
							armada.addToCarrier(sb.toString());
							used.add(sb.toString());

						}
					}
				}
			}
		}

		for (int k = 0; k < buttonList.size(); k++) {
			for (int j = 0; j < 5; j++) {
				if (buttonList.get(k).getId() != null) {
					if (buttonList.get(k).getId().toString().length() == 3) {
						if (buttonList.get(k).getId().toString().charAt(1) == armada.getCarrier().get(j).charAt(0)
								&& buttonList.get(k).getId().toString().charAt(2) == armada.getCarrier().get(j)
										.charAt(1)
								&& armada.getCarrier().get(j).length() == 2) {
							buttonList.get(k).setDisable(true);
							buttonList.get(k).setStyle("-fx-background-color: green");
						}
					} else if (buttonList.get(k).getId().toString().length() == 4) {
						if (buttonList.get(k).getId().toString().charAt(1) == armada.getCarrier().get(j).charAt(0)
								&& buttonList.get(k).getId().toString().charAt(2) == armada.getCarrier().get(j)
										.charAt(1)
										&& armada.getCarrier().get(j).length() == 3
										&& buttonList.get(k).getId().toString().charAt(3) == armada.getCarrier().get(j)
										.charAt(2) ) {
							buttonList.get(k).setDisable(true);
							buttonList.get(k).setStyle("-fx-background-color: green");
						}
					}
				}
			}
		}

	}

	
	/*
	 * Sets up the event handler for the highlighting for image one
	 */
	private void setDragLengthOne() {

		for (Node target : playerGrid.getChildren()) {
			if (target.getId() != null) {
				target.setOnDragEntered(new EventHandler<DragEvent>() {
					@Override
					public void handle(DragEvent event) {
						/* the drag-and-drop gesture entered the target */
						/* show to the user that it is an actual gesture target */
						highlightImageOne((Button) target, event);
					}
				});
			}

		}

		for (Node target : playerGrid.getChildren()) {
			if (target.getId() != null) {
				target.setOnDragExited(new EventHandler<DragEvent>() {
					public void handle(DragEvent event) {
						/* mouse moved away, remove the graphical cues */
						unHighlightImageOne((Button) target, event);
						event.consume();
					}
				});
			}

		}

	}
	/*
	 * Sets up the event handler for the highlighting for image two
	 */
	private void setDragLengthTwo() {

		for (Node target : playerGrid.getChildren()) {
			if (target.getId() != null) {
				target.setOnDragEntered(new EventHandler<DragEvent>() {
					@Override
					public void handle(DragEvent event) {
						/* the drag-and-drop gesture entered the target */
						/* show to the user that it is an actual gesture target */
						highlightImageTwo((Button) target, event);
						event.consume();
					}
				});
			}

		}

		for (Node target : playerGrid.getChildren()) {
			if (target.getId() != null) {
				target.setOnDragExited(new EventHandler<DragEvent>() {
					public void handle(DragEvent event) {
						/* mouse moved away, remove the graphical cues */
						unHighlightImageTwo((Button) target, event);
						event.consume();
					}
				});
			}

		}
	}
	/*
	 * Sets up the event handler for the highlighting for image three
	 */
	private void setDragLengthThree() {

		for (Node target : playerGrid.getChildren()) {
			if (target.getId() != null) {
				target.setOnDragEntered(new EventHandler<DragEvent>() {
					@Override
					public void handle(DragEvent event) {
						/* the drag-and-drop gesture entered the target */
						/* show to the user that it is an actual gesture target */
						highlightImageThree((Button) target, event);
						event.consume();
					}
				});
			}

		}

		for (Node target : playerGrid.getChildren()) {
			if (target.getId() != null) {
				target.setOnDragExited(new EventHandler<DragEvent>() {
					public void handle(DragEvent event) {
						/* mouse moved away, remove the graphical cues */
						unHighlightImageThree((Button) target, event);
						event.consume();
					}
				});
			}

		}
	}
	/*
	 * Sets up the event handler for the highlighting for image four
	 */
	private void setDragLengthFour() {

		for (Node target : playerGrid.getChildren()) {
			if (target.getId() != null) {
				target.setOnDragEntered(new EventHandler<DragEvent>() {
					@Override
					public void handle(DragEvent event) {
						/* the drag-and-drop gesture entered the target */
						/* show to the user that it is an actual gesture target */
						highlightImageFour((Button) target, event);
						event.consume();
					}
				});
			}

		}

		for (Node target : playerGrid.getChildren()) {
			if (target.getId() != null) {
				target.setOnDragExited(new EventHandler<DragEvent>() {
					public void handle(DragEvent event) {
						/* mouse moved away, remove the graphical cues */
						unHighlightImageFour((Button) target, event);
						event.consume();
					}
				});
			}

		}

	}
	/*
	 * Sets up the event handler for the highlighting for image five
	 */
	private void setDragLengthFive() {

		for (Node target : playerGrid.getChildren()) {
			if (target.getId() != null) {
				target.setOnDragEntered(new EventHandler<DragEvent>() {
					@Override
					public void handle(DragEvent event) {
						/* the drag-and-drop gesture entered the target */
						/* show to the user that it is an actual gesture target */
						highlightImageFive((Button) target, event);
						event.consume();
					}
				});
			}

		}

		for (Node target : playerGrid.getChildren()) {
			if (target.getId() != null) {
				target.setOnDragExited(new EventHandler<DragEvent>() {
					public void handle(DragEvent event) {
						/* mouse moved away, remove the graphical cues */
						unHighlightImageFive((Button) target, event);
						event.consume();
					}
				});
			}

		}

	}

	/*
	 * Set up the detection, drag over and drag done for each image
	 */
	private void configureDragAndDrop(ImageView image) {
		image.setOnDragDetected(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				Dragboard db = pictureOne.startDragAndDrop(TransferMode.ANY);

				ClipboardContent content = new ClipboardContent();
				content.putString("");
				db.setContent(content);

				event.consume();
			}

		});

		for (Node target : playerGrid.getChildren()) {
			if (target.getId() != null) {
				target.setOnDragOver(new EventHandler<DragEvent>() {
					public void handle(DragEvent event) {
						/* data is dragged over the target */
						/*
						 * accept it only if it is not dragged from the same node and if it has a string
						 * data
						 */
						if (event.getGestureSource() != target && event.getDragboard().hasString()) {
							/* allow for moving */
							event.acceptTransferModes(TransferMode.ANY);
						}

						event.consume();
					}
				});
			}

		}

		// Start source drag done
		image.setOnDragDone(new EventHandler<DragEvent>() {
			public void handle(DragEvent event) {
				/* the drag and drop gesture ended */
				/* if the data was successfully moved, clear it */
				if (event.getTransferMode() == TransferMode.MOVE) {
					image.setDisable(true);
				}
				event.consume();
			}
		});
		// End Source Drag Done

	}

	/*
	 * Sets up the actions for when image one is dropped on a button
	 */
	private void configureDroppedImageOne() {

		ObservableList<Node> buttonList = playerGrid.getChildren();

		for (Node target : playerGrid.getChildren()) {
			if (target.getId() != null) {
				target.setOnDragDropped(new EventHandler<DragEvent>() {
					public void handle(DragEvent event) {
						/* data dropped */
						/* if there is a string data on dragboard, read it and use it */
						Dragboard db = event.getDragboard();
						boolean success = false;
						if (db.hasString()) {
//							Button targetButton = (Button) target;
							if (orientation == "Horizontal") {
								StringBuilder s = new StringBuilder();
								if (target.getId().toString().length() == 3) {
									switch (target.getId().toString().charAt(1)) {
									case 'A':
										twoController.addTargetToArmada((Button)target, armada);
										for (int i = 0; i < 100; i++) {
											Node button = buttonList.get(i);
											if (button.getId().toString().length() == 3) {
												if (twoController.highlightLengthTwoHorizontalTargetThree(button, (Button) target, 'B'))
													twoController.addButtonsToArmada(button, armada);

											}

										}
									case 'B':
										twoController.addTargetToArmada((Button)target, armada);
										for (int i = 0; i < 100; i++) {
											Node button = buttonList.get(i);
											if (button.getId().toString().length() == 3) {
												if (twoController.highlightLengthTwoHorizontalTargetThree(button, (Button) target, 'A'))
													twoController.addButtonsToArmada(button, armada);
											}

										}
										break;
									case 'C':
										twoController.addTargetToArmada((Button)target, armada);
										for (int i = 0; i < 100; i++) {
											Node button = buttonList.get(i);
											if (button.getId().toString().length() == 3) {
												if (twoController.highlightLengthTwoHorizontalTargetThree(button, (Button) target, 'B'))
													twoController.addButtonsToArmada(button, armada);
											}

										}
										break;
									case 'D':
										twoController.addTargetToArmada((Button)target, armada);
										for (int i = 0; i < 100; i++) {
											Node button = buttonList.get(i);
											if (button.getId().toString().length() == 3) {
												if (twoController.highlightLengthTwoHorizontalTargetThree(button, (Button) target, 'C'))
													twoController.addButtonsToArmada(button, armada);
											}

										}
										break;
									case 'E':
										twoController.addTargetToArmada((Button)target, armada);
										for (int i = 0; i < 100; i++) {
											Node button = buttonList.get(i);
											if (button.getId().toString().length() == 3) {
												if (twoController.highlightLengthTwoHorizontalTargetThree(button, (Button) target, 'D'))
													twoController.addButtonsToArmada(button, armada);
											}

										}
										break;
									case 'F':
										twoController.addTargetToArmada((Button)target, armada);
										for (int i = 0; i < 100; i++) {
											Node button = buttonList.get(i);
											if (button.getId().toString().length() == 3) {
												if (twoController.highlightLengthTwoHorizontalTargetThree(button, (Button) target, 'E'))
													twoController.addButtonsToArmada(button, armada);
											}

										}
										break;
									case 'G':
										twoController.addTargetToArmada((Button)target, armada);
										for (int i = 0; i < 100; i++) {
											Node button = buttonList.get(i);
											if (button.getId().toString().length() == 3) {
												if (twoController.highlightLengthTwoHorizontalTargetThree(button, (Button) target, 'F'))
													twoController.addButtonsToArmada(button, armada);
											}

										}
										break;
									case 'H':
										twoController.addTargetToArmada((Button)target, armada);
										for (int i = 0; i < 100; i++) {
											Node button = buttonList.get(i);
											if (button.getId().toString().length() == 3) {
												if (twoController.highlightLengthTwoHorizontalTargetThree(button, (Button) target, 'G'))
													twoController.addButtonsToArmada(button, armada);
											}

										}
										break;
									case 'I':
										twoController.addTargetToArmada((Button)target, armada);
										for (int i = 0; i < 100; i++) {
											Node button = buttonList.get(i);
											if (button.getId().toString().length() == 3) {
												if (twoController.highlightLengthTwoHorizontalTargetThree(button, (Button) target, 'H'))
													twoController.addButtonsToArmada(button, armada);
											}

										}
										break;
									case 'J':
										twoController.addTargetToArmada((Button)target, armada);
										for (int i = 0; i < 100; i++) {
											Node button = buttonList.get(i);
											if (button.getId().toString().length() == 3) {
												if (twoController.highlightLengthTwoHorizontalTargetThree(button, (Button) target, 'I'))
													twoController.addButtonsToArmada(button, armada);
											}

										}
										break;

									default:
										break;
									}
								} else if (target.getId().toString().length() == 4) {
									switch (target.getId().toString().charAt(1)) {
									case 'A':
										twoController.addTargetToArmadaFour((Button)target, armada);
										for (int i = 0; i < 100; i++) {
											Node button = buttonList.get(i);
											if (button.getId().toString().length() == 4) {
												if (twoController.highlightLengthTwoHorizontalTargetFour(button, (Button) target, 'B'))
													twoController.addButtonsToArmadaFour(button, armada);
											}

										}
									case 'B':
										twoController.addTargetToArmadaFour((Button)target, armada);
										for (int i = 0; i < 100; i++) {
											Node button = buttonList.get(i);
											if (button.getId().toString().length() == 4) {
												if (twoController.highlightLengthTwoHorizontalTargetFour(button, (Button) target, 'A'))
													twoController.addButtonsToArmadaFour(button, armada);
											}

										}
										break;
									case 'C':
										twoController.addTargetToArmadaFour((Button)target, armada);
										for (int i = 0; i < 100; i++) {
											Node button = buttonList.get(i);
											if (button.getId().toString().length() == 4) {
												if (twoController.highlightLengthTwoHorizontalTargetFour(button, (Button) target, 'B'))
													twoController.addButtonsToArmadaFour(button, armada);
											}
										}
										break;
									case 'D':
										twoController.addTargetToArmadaFour((Button)target, armada);
										for (int i = 0; i < 100; i++) {
											Node button = buttonList.get(i);
											if (button.getId().toString().length() == 4) {
												if (twoController.highlightLengthTwoHorizontalTargetFour(button, (Button) target, 'C'))
													twoController.addButtonsToArmadaFour(button, armada);
											}
										}
										break;
									case 'E':
										twoController.addTargetToArmadaFour((Button)target, armada);
										for (int i = 0; i < 100; i++) {
											Node button = buttonList.get(i);
											if (button.getId().toString().length() == 4) {
												if (twoController.highlightLengthTwoHorizontalTargetFour(button, (Button) target, 'D'))
													twoController.addButtonsToArmadaFour(button, armada);
											}
										}
										break;
									case 'F':
										twoController.addTargetToArmadaFour((Button)target, armada);
										for (int i = 0; i < 100; i++) {
											Node button = buttonList.get(i);
											if (button.getId().toString().length() == 4) {
												if (twoController.highlightLengthTwoHorizontalTargetFour(button, (Button) target, 'E'))
													twoController.addButtonsToArmadaFour(button, armada);
											}

										}
										break;
									case 'G':
										twoController.addTargetToArmadaFour((Button)target, armada);
										for (int i = 0; i < 100; i++) {
											Node button = buttonList.get(i);
											if (button.getId().toString().length() == 4) {
												if (twoController.highlightLengthTwoHorizontalTargetFour(button, (Button) target, 'F'))
													twoController.addButtonsToArmadaFour(button, armada);
												
											}

										}
										break;
									case 'H':
										twoController.addTargetToArmadaFour((Button)target, armada);
										for (int i = 0; i < 100; i++) {
											Node button = buttonList.get(i);
											if (button.getId().toString().length() == 4) {
												if (twoController.highlightLengthTwoHorizontalTargetFour(button, (Button) target, 'G'))
													twoController.addButtonsToArmadaFour(button, armada);
											}

										}
										break;
									case 'I':
										twoController.addTargetToArmadaFour((Button)target, armada);
										for (int i = 0; i < 100; i++) {
											Node button = buttonList.get(i);
											if (button.getId().toString().length() == 4) {
												if (twoController.highlightLengthTwoHorizontalTargetFour(button, (Button) target, 'H'))
													twoController.addButtonsToArmadaFour(button, armada);
											}

										}
										break;
									case 'J':
										twoController.addTargetToArmadaFour((Button)target, armada);
										for (int i = 0; i < 100; i++) {
											Node button = buttonList.get(i);
											if (button.getId().toString().length() == 4) {
												if (twoController.highlightLengthTwoHorizontalTargetFour(button, (Button) target, 'I'))
													twoController.addButtonsToArmadaFour(button, armada);
											}

										}
										break;
									default:
										break;
									}
								}
							} else if (orientation == "Vertical") {
								StringBuilder s = new StringBuilder();
								if (target.getId().toString().length() == 3) {
									switch (target.getId().toString().charAt(2)) {
									case '1':
										if (target.getId().toString().length() == 3) {
											twoController.addTargetToArmada((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 3) {
													if (twoController.highlightLengthTwoVertical(button, (Button) target, '2'))
														twoController.addButtonsToArmada(button, armada);
												}

											}
										} else if (target.getId().toString().charAt(4) == '0') {
											twoController.addTargetToArmada((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (twoController.highlightLengthTwoVertical(button, (Button) target, '9'))
													twoController.addButtonsToArmada(button, armada);
											}
										}
										break;
									case '2':
										twoController.addTargetToArmada((Button) target, armada);
										for (int i = 0; i < 100; i++) {
											Node button = buttonList.get(i);
											if (button.getId().toString().length() == 3) {
												if (twoController.highlightLengthTwoVertical(button, (Button) target, '1'))
													twoController.addButtonsToArmada(button, armada);
											}

										}
										break;
									case '3':
										twoController.addTargetToArmada((Button) target, armada);
										for (int i = 0; i < 100; i++) {
											Node button = buttonList.get(i);
											if (button.getId().toString().length() == 3) {
												if (twoController.highlightLengthTwoVertical(button, (Button) target, '2'))
													twoController.addButtonsToArmada(button, armada);
											}

										}
										break;
									case '4':
										twoController.addTargetToArmada((Button) target, armada);
										for (int i = 0; i < 100; i++) {
											Node button = buttonList.get(i);
											if (button.getId().toString().length() == 3) {
												if (twoController.highlightLengthTwoVertical(button, (Button) target, '3'))
													twoController.addButtonsToArmada(button, armada);
											}

										}
										break;
									case '5':
										twoController.addTargetToArmada((Button) target, armada);
										for (int i = 0; i < 100; i++) {
											Node button = buttonList.get(i);
											if (button.getId().toString().length() == 3) {
												if (twoController.highlightLengthTwoVertical(button, (Button) target, '4'))
													twoController.addButtonsToArmada(button, armada);
											}

										}
										break;
									case '6':
										twoController.addTargetToArmada((Button) target, armada);
										for (int i = 0; i < 100; i++) {
											Node button = buttonList.get(i);
											if (button.getId().toString().length() == 3) {
												if (twoController.highlightLengthTwoVertical(button, (Button) target, '5'))
													twoController.addButtonsToArmada(button, armada);
											}

										}
										break;
									case '7':
										twoController.addTargetToArmada((Button) target, armada);
										for (int i = 0; i < 100; i++) {
											Node button = buttonList.get(i);
											if (button.getId().toString().length() == 3) {
												if (twoController.highlightLengthTwoVertical(button, (Button) target, '6'))
													twoController.addButtonsToArmada(button, armada);
											}

										}
										break;
									case '8':
										twoController.addTargetToArmada((Button) target, armada);
										for (int i = 0; i < 100; i++) {
											Node button = buttonList.get(i);
											if (button.getId().toString().length() == 3) {
												if (twoController.highlightLengthTwoVertical(button, (Button) target, '7'))
													twoController.addButtonsToArmada(button, armada);
											}

										}
										break;
									case '9':
										twoController.addTargetToArmada((Button) target, armada);
										for (int i = 0; i < 100; i++) {
											Node button = buttonList.get(i);
											if (twoController.highlightLengthTwoVertical(button, (Button) target, '8'))
												twoController.addButtonsToArmada(button, armada);
										}
										break;

									default:
										break;
									}
								} else if (target.getId().toString().length() == 4) {

									switch (target.getId().toString().charAt(2)) {
									case '1':
										if (target.getId().toString().length() == 4) {
											twoController.addTargetToArmada((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (twoController.highlightLengthTwoVertical(button, (Button) target, '9')) 
													twoController.addButtonsToArmada(button, armada);
											}
										}
										break;

									default:
										break;
									}

								}
							}
							success = true;
						}
						pictureOne.setDisable(true);
						event.setDropCompleted(success);
						event.consume();
					}
				});
			}
		}

	}
	/*
	 * Sets up the actions for when image two is dropped on a button
	 */
	private void configureDroppedImageTwo() {
		for (Node target : playerGrid.getChildren()) {
			if (target.getId() != null) {
				target.setOnDragDropped(new EventHandler<DragEvent>() {
					public void handle(DragEvent event) {
						/* data dropped */
						/* if there is a string data on dragboard, read it and use it */
						Dragboard db = event.getDragboard();
						boolean success = false;
						if (db.hasString()) {
							StringBuilder s = new StringBuilder();
							if (orientation == "Horizontal") {
								if (event.getGestureSource() != target && event.getDragboard().hasString()) {
//									System.out.println(target.getId());

									if (target.getId().toString().length() == 3) {
										switch (target.getId().toString().charAt(1)) {
										case 'A':
											threeController.addTargetToSubmarine((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 3) {
													if (threeController.highlightLengthThreeHorizontalTargetThree(button, (Button)target, 'B', 'C'))
														threeController.addButtonsToSubmarine(button, armada);
												}
											}
											break;

										case 'B':
											threeController.addTargetToSubmarine((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 3) {
													if (threeController.highlightLengthThreeHorizontalTargetThree(button, (Button)target, 'A', 'C'))
														threeController.addButtonsToSubmarine(button, armada);
												}

											}
											break;
										case 'C':
											threeController.addTargetToSubmarine((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 3) {
													if (threeController.highlightLengthThreeHorizontalTargetThree(button, (Button)target, 'B', 'D'))
														threeController.addButtonsToSubmarine(button, armada);
												}

											}
											break;
										case 'D':
											threeController.addTargetToSubmarine((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 3) {
													if (threeController.highlightLengthThreeHorizontalTargetThree(button, (Button)target, 'C', 'E'))
														threeController.addButtonsToSubmarine(button, armada);
												}

											}
											break;
										case 'E':
											threeController.addTargetToSubmarine((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 3) {
													if (threeController.highlightLengthThreeHorizontalTargetThree(button, (Button)target, 'D', 'F'))
														threeController.addButtonsToSubmarine(button, armada);
												}

											}
											break;
										case 'F':
											threeController.addTargetToSubmarine((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 3) {
													if (threeController.highlightLengthThreeHorizontalTargetThree(button, (Button)target, 'E', 'G'))
														threeController.addButtonsToSubmarine(button, armada);
												}

											}
											break;
										case 'G':
											threeController.addTargetToSubmarine((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 3) {
													if (threeController.highlightLengthThreeHorizontalTargetThree(button, (Button)target, 'F', 'H'))
														threeController.addButtonsToSubmarine(button, armada);
												}

											}
											break;
										case 'H':
											threeController.addTargetToSubmarine((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 3) {
													if (threeController.highlightLengthThreeHorizontalTargetThree(button, (Button)target, 'G', 'I'))
														threeController.addButtonsToSubmarine(button, armada);
												}

											}
											break;
										case 'I':
											threeController.addTargetToSubmarine((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 3) {
													if (threeController.highlightLengthThreeHorizontalTargetThree(button, (Button)target, 'H', 'J'))
														threeController.addButtonsToSubmarine(button, armada);
													
												}

											}
											break;
										case 'J':
											threeController.addTargetToSubmarine((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 3) {
													if (threeController.highlightLengthThreeHorizontalTargetThree(button, (Button)target, 'H', 'I'))
														threeController.addButtonsToSubmarine(button, armada);
												}

											}
											break;
										default:
											break;
										}
									} else if (target.getId().toString().length() == 4) {
										switch (target.getId().toString().charAt(1)) {
										case 'A':
											threeController.addTargetToSubmarineFour((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 4) {
													if (threeController.highlightLengthThreeHorizontalTargetFour(button, (Button)target, 'B', 'C'))
														threeController.addButtonsToSubmarineFour(button, armada);
												}

											}
											break;
										case 'B':
											threeController.addTargetToSubmarineFour((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 4) {
													if (threeController.highlightLengthThreeHorizontalTargetFour(button, (Button)target, 'A', 'C'))
														threeController.addButtonsToSubmarineFour(button, armada);
												}

											}
											break;
										case 'C':
											threeController.addTargetToSubmarineFour((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 4) {
													if (threeController.highlightLengthThreeHorizontalTargetFour(button, (Button)target, 'B', 'D'))
														threeController.addButtonsToSubmarineFour(button, armada);
												}
											}
											break;
										case 'D':
											threeController.addTargetToSubmarineFour((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 4) {
													if (threeController.highlightLengthThreeHorizontalTargetFour(button, (Button)target, 'C', 'E'))
														threeController.addButtonsToSubmarineFour(button, armada);
												}
											}
											break;
										case 'E':
											threeController.addTargetToSubmarineFour((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 4) {
													if (threeController.highlightLengthThreeHorizontalTargetFour(button, (Button)target, 'D', 'F'))
														threeController.addButtonsToSubmarineFour(button, armada);
												}
											}
											break;
										case 'F':
											threeController.addTargetToSubmarineFour((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 4) {
													if (threeController.highlightLengthThreeHorizontalTargetFour(button, (Button)target, 'E', 'G'))
														threeController.addButtonsToSubmarineFour(button, armada);
												}

											}
											break;
										case 'G':
											threeController.addTargetToSubmarineFour((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 4) {
													if (threeController.highlightLengthThreeHorizontalTargetFour(button, (Button)target, 'F', 'H'))
														threeController.addButtonsToSubmarineFour(button, armada);
												}

											}
											break;
										case 'H':
											threeController.addTargetToSubmarineFour((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 4) {
													if (threeController.highlightLengthThreeHorizontalTargetFour(button, (Button)target, 'G', 'I'))
														threeController.addButtonsToSubmarineFour(button, armada);
												}

											}
											break;
										case 'I':
											threeController.addTargetToSubmarineFour((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 4) {
													if (threeController.highlightLengthThreeHorizontalTargetFour(button, (Button)target, 'H', 'J'))
														threeController.addButtonsToSubmarineFour(button, armada);
												}

											}
											break;
										case 'J':
											threeController.addTargetToSubmarineFour((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 4) {
													if (threeController.highlightLengthThreeHorizontalTargetFour(button, (Button) target, 'H', 'I'))
														threeController.addButtonsToSubmarineFour(button, armada);
												}

											}
											break;
										default:
											break;
										}
									}
								}
							} else if (orientation == "Vertical") {
								if (target.getId().toString().length() == 3) {
									switch (target.getId().toString().charAt(2)) {
									case '1':
										if (target.getId().toString().length() == 3) {
											threeController.addTargetToSubmarine((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 3) {
													if (threeController.highlightLengthThreeVerticalTargetThreeSpecialCaseOneA(button, (Button)target, '2', '3'))
														threeController.addButtonsToSubmarine(button, armada);
												}

											}
										} else if (target.getId().toString().charAt(4) == '0') {
											threeController.addTargetToSubmarineFour((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (threeController.highlightLengthThreeVerticalTargetThreeSpecialCaseOneB(button, (Button)target, '8', '9'))
													threeController.addButtonsToSubmarine(button, armada);
												
											}
										}
										break;

									case '2':
										threeController.addTargetToSubmarine((Button) target, armada);
										for (int i = 0; i < 100; i++) {
											Node button = buttonList.get(i);
											if (button.getId().toString().length() == 3) {
												if (threeController.highlightLengthThreeVerticalTargetThree(button, (Button)target, '1', '3'))
													threeController.addButtonsToSubmarine(button, armada);
											}

										}
										break;
									case '3':
										threeController.addTargetToSubmarine((Button) target, armada);
										for (int i = 0; i < 100; i++) {
											Node button = buttonList.get(i);
											if (button.getId().toString().length() == 3) {
												if (threeController.highlightLengthThreeVerticalTargetThree(button, (Button)target, '2', '4'))
													threeController.addButtonsToSubmarine(button, armada);
											}

										}
										break;
									case '4':
										threeController.addTargetToSubmarine((Button) target, armada);
										for (int i = 0; i < 100; i++) {
											Node button = buttonList.get(i);
											if (button.getId().toString().length() == 3) {
												if (threeController.highlightLengthThreeVerticalTargetThree(button, (Button)target, '3', '5'))
													threeController.addButtonsToSubmarine(button, armada);
											}

										}
										break;
									case '5':
										threeController.addTargetToSubmarine((Button) target, armada);
										for (int i = 0; i < 100; i++) {
											Node button = buttonList.get(i);
											if (button.getId().toString().length() == 3) {
												if (threeController.highlightLengthThreeVerticalTargetThree(button, (Button)target, '4', '6'))
													threeController.addButtonsToSubmarine(button, armada);
												
											}

										}
										break;
									case '6':
										threeController.addTargetToSubmarine((Button) target, armada);
										for (int i = 0; i < 100; i++) {
											Node button = buttonList.get(i);
											if (button.getId().toString().length() == 3) {
												if (threeController.highlightLengthThreeVerticalTargetThree(button, (Button)target, '5', '7'))
													threeController.addButtonsToSubmarine(button, armada);
											}

										}
										break;
									case '7':
										threeController.addTargetToSubmarine((Button) target, armada);
										for (int i = 0; i < 100; i++) {
											Node button = buttonList.get(i);
											if (button.getId().toString().length() == 3) {
												if (threeController.highlightLengthThreeVerticalTargetThree(button, (Button)target, '6', '8'))
													threeController.addButtonsToSubmarine(button, armada);
												
											}

										}
										break;
									case '8':
										threeController.addTargetToSubmarine((Button) target, armada);
										for (int i = 0; i < 100; i++) {
											Node button = buttonList.get(i);
											if (button.getId().toString().length() == 3) {
												if (threeController.highlightLengthThreeVerticalTargetThree(button, (Button)target, '7', '9')) {
													threeController.addButtonsToSubmarine(button, armada);
												}
											}

										}
										break;
									case '9':
										threeController.addTargetToSubmarine((Button)target, armada);
										for (int i = 0; i < 100; i++) {
											Node button = buttonList.get(i);
											if (button.getId().toString().length() == 3) {
												if (threeController.highlightLengthThreeVerticalTargetThreeSpecialCaseNine(button, (Button) target, '8'))
													if (button.getId().toString().length() != 4) {
														threeController.addButtonsToSubmarine(button, armada);
													}
											} else if (button.getId().toString().length() == 4) {
												if (threeController.highlightLengthThreeVerticalTargetThreeSpecialCaseTen(button,  (Button) target, '1', '0'))
													if (button.getId().toString().length() != 3) {
														threeController.addButtonsToSubmarineFour(button, armada);
													}
											}

										}
										break;
									default:
										break;
									}
								} else if (target.getId().toString().length() == 4) {
									switch (target.getId().toString().charAt(2)) {
									case '1':
										if (target.getId().toString().length() == 4) {
											threeController.addTargetToSubmarineFour((Button)target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (threeController.highlightLengthThreeVerticalTargetFour(button, (Button)target, '8', '9'))
													threeController.addButtonsToSubmarine(button, armada);
											}
										}
										break;
									default:
										break;
									}
								}
							}
							success = true;
						}
						pictureTwo.setDisable(true);
						event.setDropCompleted(success);
						event.consume();
					}
				});
			}
		}
	}
	/*
	 * Sets up the actions for when image three is dropped on a button
	 */
	private void configureDroppedImageThree() {

		for (Node target : playerGrid.getChildren()) {
			if (target.getId() != null) {
				target.setOnDragDropped(new EventHandler<DragEvent>() {
					public void handle(DragEvent event) {
						/* data dropped */
						/* if there is a string data on dragboard, read it and use it */
						Dragboard db = event.getDragboard();
						boolean success = false;
						if (db.hasString()) {
							StringBuilder s = new StringBuilder();
							if (orientation == "Horizontal") {
								if (event.getGestureSource() != target && event.getDragboard().hasString()) {
									if (target.getId().toString().length() == 3) {
										switch (target.getId().toString().charAt(1)) {
										case 'A':
											threeController.addTargetToCruiser((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 3) {
													if (threeController.highlightLengthThreeHorizontalTargetThree(button, (Button) target, 'B', 'C'))
														threeController.addButtonsToCruiser(button, armada);

												}
											}
											break;

										case 'B':
											threeController.addTargetToCruiser((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 3) {
													if (threeController.highlightLengthThreeHorizontalTargetThree(button, (Button) target, 'A', 'C'))
														threeController.addButtonsToCruiser(button, armada);
												}

											}
											break;
										case 'C':
											threeController.addTargetToCruiser((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 3) {
													if (threeController.highlightLengthThreeHorizontalTargetThree(button, (Button) target, 'B', 'D'))
														threeController.addButtonsToCruiser(button, armada);
												}

											}
											break;
										case 'D':
											threeController.addTargetToCruiser((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 3) {
													if (threeController.highlightLengthThreeHorizontalTargetThree(button, (Button) target, 'C', 'E'))
														threeController.addButtonsToCruiser(button, armada);
												}

											}
											break;
										case 'E':
											threeController.addTargetToCruiser((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 3) {
													if (threeController.highlightLengthThreeHorizontalTargetThree(button, (Button) target, 'D', 'F'))
														threeController.addButtonsToCruiser(button, armada);
												}

											}
											break;
										case 'F':
											threeController.addTargetToCruiser((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 3) {
													if (threeController.highlightLengthThreeHorizontalTargetThree(button, (Button) target, 'E', 'G'))
														threeController.addButtonsToCruiser(button, armada);
												}

											}
											break;
										case 'G':
											threeController.addTargetToCruiser((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 3) {
													if (threeController.highlightLengthThreeHorizontalTargetThree(button, (Button) target, 'F', 'H'))
														threeController.addButtonsToCruiser(button, armada);
												}

											}
											break;
										case 'H':
											threeController.addTargetToCruiser((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 3) {
													if (threeController.highlightLengthThreeHorizontalTargetThree(button, (Button) target, 'G', 'I'))
														threeController.addButtonsToCruiser(button, armada);
												}

											}
											break;
										case 'I':
											threeController.addTargetToCruiser((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 3) {
													if (threeController.highlightLengthThreeHorizontalTargetThree(button, (Button) target, 'H', 'J'))
														threeController.addButtonsToCruiser(button, armada);
												}

											}
											break;
										case 'J':
											threeController.addTargetToCruiser((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 3) {
													if (threeController.highlightLengthThreeHorizontalTargetThree(button, (Button) target, 'H', 'I'))
														threeController.addButtonsToCruiser(button, armada);
												}

											}
											break;
										default:
											break;
										}
									} else if (target.getId().toString().length() == 4) {
										switch (target.getId().toString().charAt(1)) {
										case 'A':
											threeController.addTargetToCruiserFour((Button) target, armada);
											armada.addToCruiser(s.toString());
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 4) {
													if (threeController.highlightLengthThreeHorizontalTargetFour(button, (Button) target, 'B', 'C'))
														threeController.addButtonsToCruiserFour(button, armada);
												}

											}
											break;
										case 'B':
											threeController.addTargetToCruiserFour((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 4) {
													if (threeController.highlightLengthThreeHorizontalTargetFour(button, (Button) target, 'A', 'C'))
														threeController.addButtonsToCruiserFour(button, armada);
													
												}

											}
											break;
										case 'C':
											threeController.addTargetToCruiserFour((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 4) {
													if (threeController.highlightLengthThreeHorizontalTargetFour(button, (Button) target, 'B', 'D'))
														threeController.addButtonsToCruiserFour(button, armada);
												}
											}
											break;
										case 'D':
											threeController.addTargetToCruiserFour((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 4) {
													if (threeController.highlightLengthThreeHorizontalTargetFour(button, (Button) target, 'C', 'E'))
														threeController.addButtonsToCruiserFour(button, armada);
												}
											}
											break;
										case 'E':
											threeController.addTargetToCruiserFour((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 4) {
													if (threeController.highlightLengthThreeHorizontalTargetFour(button, (Button) target, 'D', 'F'))
														threeController.addButtonsToCruiserFour(button, armada);
												}
											}
											break;
										case 'F':
											threeController.addTargetToCruiserFour((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 4) {
													if (threeController.highlightLengthThreeHorizontalTargetFour(button, (Button) target, 'E', 'G'))
														threeController.addButtonsToCruiserFour(button, armada);
												}

											}
											break;
										case 'G':
											threeController.addTargetToCruiserFour((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 4) {
													if (threeController.highlightLengthThreeHorizontalTargetFour(button, (Button) target, 'F', 'H'))
														threeController.addButtonsToCruiserFour(button, armada);
												}

											}
											break;
										case 'H':
											threeController.addTargetToCruiserFour((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 4) {
													if (threeController.highlightLengthThreeHorizontalTargetFour(button, (Button) target, 'G', 'I'))
														threeController.addButtonsToCruiserFour(button, armada);
												}

											}
											break;
										case 'I':
											threeController.addTargetToCruiserFour((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 4) {
													if (threeController.highlightLengthThreeHorizontalTargetFour(button, (Button) target, 'H', 'J'))
														threeController.addButtonsToCruiserFour(button, armada);
												}

											}
											break;
										case 'J':
											threeController.addTargetToCruiserFour((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 4) {
													if (threeController.highlightLengthThreeHorizontalTargetFour(button, (Button) target, 'H', 'I'))
														threeController.addButtonsToCruiserFour(button, armada);
												}

											}
											break;
										default:
											break;
										}
									}
								}
							} else if (orientation == "Vertical") {
								if (target.getId().toString().length() == 3) {
									switch (target.getId().toString().charAt(2)) {
									case '1':
										if (target.getId().toString().length() == 3) {
											threeController.addTargetToCruiser((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 3) {
													if (threeController.highlightLengthThreeVerticalTargetThreeSpecialCaseOneA(button, (Button) target, '2', '3'))
														threeController.addButtonsToCruiser(button, armada);
												}

											}
										} else if (target.getId().toString().charAt(4) == '0') {
											threeController.addTargetToCruiser((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (threeController.highlightLengthThreeVerticalTargetThreeSpecialCaseOneB(button, (Button) target, '8', '9'))
													threeController.addButtonsToCruiser(button, armada);
											}
										}
										break;

									case '2':
										threeController.addTargetToCruiser((Button) target, armada);
										for (int i = 0; i < 100; i++) {
											Node button = buttonList.get(i);
											if (button.getId().toString().length() == 3) {
												if (threeController.highlightLengthThreeVerticalTargetThree(button, (Button) target, '1', '3'))
													threeController.addButtonsToCruiser(button, armada);
											}

										}
										break;
									case '3':
										threeController.addTargetToCruiser((Button) target, armada);
										for (int i = 0; i < 100; i++) {
											Node button = buttonList.get(i);
											if (button.getId().toString().length() == 3) {
												if (threeController.highlightLengthThreeVerticalTargetThree(button, (Button) target, '2', '4'))
													threeController.addButtonsToCruiser(button, armada);
											}

										}
										break;
									case '4':
										threeController.addTargetToCruiser((Button) target, armada);
										for (int i = 0; i < 100; i++) {
											Node button = buttonList.get(i);
											if (button.getId().toString().length() == 3) {
												if (threeController.highlightLengthThreeVerticalTargetThree(button, (Button) target, '3', '5'))
													threeController.addButtonsToCruiser(button, armada);
											}

										}
										break;
									case '5':
										threeController.addTargetToCruiser((Button) target, armada);
										for (int i = 0; i < 100; i++) {
											Node button = buttonList.get(i);
											if (button.getId().toString().length() == 3) {
												if (threeController.highlightLengthThreeVerticalTargetThree(button, (Button) target, '4', '6'))
													threeController.addButtonsToCruiser(button, armada);
											}

										}
										break;
									case '6':
										threeController.addTargetToCruiser((Button) target, armada);
										for (int i = 0; i < 100; i++) {
											Node button = buttonList.get(i);
											if (button.getId().toString().length() == 3) {
												if (threeController.highlightLengthThreeVerticalTargetThree(button, (Button) target, '5', '7'))
													threeController.addButtonsToCruiser(button, armada);
											}

										}
										break;
									case '7':
										threeController.addTargetToCruiser((Button) target, armada);
										for (int i = 0; i < 100; i++) {
											Node button = buttonList.get(i);
											if (button.getId().toString().length() == 3) {
												if (threeController.highlightLengthThreeVerticalTargetThree(button, (Button) target, '6', '8'))
													threeController.addButtonsToCruiser(button, armada);
											}

										}
										break;
									case '8':
										threeController.addTargetToCruiser((Button) target, armada);
										for (int i = 0; i < 100; i++) {
											Node button = buttonList.get(i);
											if (button.getId().toString().length() == 3) {
												if (threeController.highlightLengthThreeVerticalTargetThree(button, (Button) target, '7', '9'))
													threeController.addButtonsToCruiser(button, armada);
											}

										}
										break;
									case '9':
										threeController.addTargetToCruiser((Button) target, armada);
										for (int i = 0; i < 100; i++) {
											Node button = buttonList.get(i);
											if (button.getId().toString().length() == 3) {
												if (threeController.highlightLengthThreeVerticalTargetThreeSpecialCaseNine(button, (Button) target, '8'))
													if (button.getId().toString().length() != 4) {
														threeController.addButtonsToCruiser(button, armada);
													}
											} else if (button.getId().toString().length() == 4) {
												if (threeController.highlightLengthThreeVerticalTargetThreeSpecialCaseTen(button, (Button) target, '1', '0'))
													if (button.getId().toString().length() != 3) {
														threeController.addButtonsToCruiserFour(button, armada);
													}
											}

										}
										break;
									default:
										break;
									}
								} else if (target.getId().toString().length() == 4) {
									switch (target.getId().toString().charAt(2)) {
									case '1':
										if (target.getId().toString().length() == 4) {
											threeController.addTargetToCruiserFour((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (threeController.highlightLengthThreeVerticalTargetFour(button, (Button) target, '8', '9'))
													threeController.addButtonsToCruiser(button, armada);
											}
										}
										break;
									default:
										break;
									}
								}
							}
							success = true;
						}
						pictureThree.setDisable(true);
						event.setDropCompleted(success);
						event.consume();
					}
				});
			}
		}

	}
	/*
	 * Sets up the actions for when image four is dropped on a button
	 */
	private void configureDroppedImageFour() {
		for (Node target : playerGrid.getChildren()) {
			if (target.getId() != null) {
				target.setOnDragDropped(new EventHandler<DragEvent>() {
					public void handle(DragEvent event) {
						/* data dropped */
						/* if there is a string data on dragboard, read it and use it */
						Dragboard db = event.getDragboard();
						boolean success = false;
						if (db.hasString()) {
							StringBuilder s = new StringBuilder();
							if (orientation == "Horizontal") {

								if (target.getId().toString().length() == 3) {
									switch (target.getId().toString().charAt(1)) {
									case 'A':
										fourController.addTargetToBattleship((Button) target, armada);
										for (int i = 0; i < 100; i++) {
											Node button = buttonList.get(i);
											if (button.getId().toString().length() == 3) {
												if (fourController.highlightLengthFourHorizontalTargetThree(button, (Button) target, 'B', 'C', 'D'))
													fourController.addButtonToBattleship(button, armada);
											}

										}
										break;
									case 'B':
										fourController.addTargetToBattleship((Button) target, armada);
										for (int i = 0; i < 100; i++) {
											Node button = buttonList.get(i);
											if (button.getId().toString().length() == 3) {
												if (fourController.highlightLengthFourHorizontalTargetThree(button, (Button) target, 'A', 'C', 'D'))
													fourController.addButtonToBattleship(button, armada);
											}

										}
										break;
									case 'C':
										fourController.addTargetToBattleship((Button) target, armada);
										for (int i = 0; i < 100; i++) {
											Node button = buttonList.get(i);
											if (button.getId().toString().length() == 3) {
												if (fourController.highlightLengthFourHorizontalTargetThree(button, (Button) target, 'B', 'D', 'E'))
													fourController.addButtonToBattleship(button, armada);
											}

										}
										break;
									case 'D':
										fourController.addTargetToBattleship((Button) target, armada);
										for (int i = 0; i < 100; i++) {
											Node button = buttonList.get(i);
											if (button.getId().toString().length() == 3) {
												if (fourController.highlightLengthFourHorizontalTargetThree(button, (Button) target, 'C', 'E', 'F'))
													fourController.addButtonToBattleship(button, armada);
											}

										}
										break;
									case 'E':
										fourController.addTargetToBattleship((Button) target, armada);
										for (int i = 0; i < 100; i++) {
											Node button = buttonList.get(i);
											if (button.getId().toString().length() == 3) {
												if (fourController.highlightLengthFourHorizontalTargetThree(button, (Button) target, 'D', 'F', 'G'))
													fourController.addButtonToBattleship(button, armada);
											}

										}
										break;
									case 'F':
										fourController.addTargetToBattleship((Button) target, armada);
										for (int i = 0; i < 100; i++) {
											Node button = buttonList.get(i);
											if (button.getId().toString().length() == 3) {
												if (fourController.highlightLengthFourHorizontalTargetThree(button, (Button) target, 'E', 'G', 'H'))
													fourController.addButtonToBattleship(button, armada);
											}

										}
										break;
									case 'G':
										fourController.addTargetToBattleship((Button) target, armada);
										for (int i = 0; i < 100; i++) {
											Node button = buttonList.get(i);
											if (button.getId().toString().length() == 3) {
												if (fourController.highlightLengthFourHorizontalTargetThree(button, (Button) target, 'F', 'H', 'I'))
													fourController.addButtonToBattleship(button, armada);
											}

										}
										break;
									case 'H':
										fourController.addTargetToBattleship((Button) target, armada);
										for (int i = 0; i < 100; i++) {
											Node button = buttonList.get(i);
											if (button.getId().toString().length() == 3) {
												if (fourController.highlightLengthFourHorizontalTargetThree(button, (Button) target, 'G', 'I', 'J'))
													fourController.addButtonToBattleship(button, armada);
											}

										}
										break;
									case 'I':
										fourController.addTargetToBattleship((Button) target, armada);
										for (int i = 0; i < 100; i++) {
											Node button = buttonList.get(i);
											if (button.getId().toString().length() == 3) {
												if (fourController.highlightLengthFourHorizontalTargetThree(button, (Button) target, 'G', 'H', 'J'))
													fourController.addButtonToBattleship(button, armada);
											}

										}
										break;
									case 'J':
										fourController.addTargetToBattleship((Button) target, armada);
										for (int i = 0; i < 100; i++) {
											Node button = buttonList.get(i);
											if (button.getId().toString().length() == 3) {
												if (fourController.highlightLengthFourHorizontalTargetThree(button, (Button) target, 'G', 'H', 'I'))
													fourController.addButtonToBattleship(button, armada);
											}

										}
										break;
									default:
										break;
									}
								} else if (target.getId().toString().length() == 4) {
									switch (target.getId().toString().charAt(1)) {
									case 'A':
										fourController.addTargetToBattleshipFour((Button) target, armada);
										for (int i = 0; i < 100; i++) {
											Node button = buttonList.get(i);
											if (button.getId().toString().length() == 4) {
												if (fourController.highlightLengthFourHorizontalTargetFour(button, (Button) target, 'B', 'C', 'D'))
													fourController.addButtonToBattleshipFour(button, armada);
											}

										}
										break;
									case 'B':
										fourController.addTargetToBattleshipFour((Button) target, armada);
										for (int i = 0; i < 100; i++) {
											Node button = buttonList.get(i);
											if (button.getId().toString().length() == 4) {
												if (fourController.highlightLengthFourHorizontalTargetFour(button, (Button) target, 'A', 'C', 'D'))
													fourController.addButtonToBattleshipFour(button, armada);
											}

										}
										break;
									case 'C':
										fourController.addTargetToBattleshipFour((Button) target, armada);
										for (int i = 0; i < 100; i++) {
											Node button = buttonList.get(i);
											if (button.getId().toString().length() == 4) {
												if (fourController.highlightLengthFourHorizontalTargetFour(button, (Button) target, 'B', 'D', 'E'))
													fourController.addButtonToBattleshipFour(button, armada);
											}
										}
										break;
									case 'D':
										fourController.addTargetToBattleshipFour((Button) target, armada);
										for (int i = 0; i < 100; i++) {
											Node button = buttonList.get(i);
											if (button.getId().toString().length() == 4) {
												if (fourController.highlightLengthFourHorizontalTargetFour(button, (Button) target, 'C', 'E', 'F'))
													fourController.addButtonToBattleshipFour(button, armada);
											}
										}
										break;
									case 'E':
										fourController.addTargetToBattleshipFour((Button) target, armada);
										for (int i = 0; i < 100; i++) {
											Node button = buttonList.get(i);
											if (button.getId().toString().length() == 4) {
												if (fourController.highlightLengthFourHorizontalTargetFour(button, (Button) target, 'D', 'F', 'G'))
													fourController.addButtonToBattleshipFour(button, armada);
											}
										}
										break;
									case 'F':
										fourController.addTargetToBattleshipFour((Button) target, armada);
										for (int i = 0; i < 100; i++) {
											Node button = buttonList.get(i);
											if (button.getId().toString().length() == 4) {
												if (fourController.highlightLengthFourHorizontalTargetFour(button, (Button) target, 'E', 'G', 'H'))
													fourController.addButtonToBattleshipFour(button, armada);
											}

										}
										break;
									case 'G':
										fourController.addTargetToBattleshipFour((Button) target, armada);
										for (int i = 0; i < 100; i++) {
											Node button = buttonList.get(i);
											if (button.getId().toString().length() == 4) {
												if (fourController.highlightLengthFourHorizontalTargetFour(button, (Button) target, 'F', 'H', 'I'))
													fourController.addButtonToBattleshipFour(button, armada);
											}

										}
										break;
									case 'H':
										fourController.addTargetToBattleshipFour((Button) target, armada);
										for (int i = 0; i < 100; i++) {
											Node button = buttonList.get(i);
											if (button.getId().toString().length() == 4) {
												if (fourController.highlightLengthFourHorizontalTargetFour(button, (Button) target, 'G', 'I', 'J'))
													fourController.addButtonToBattleshipFour(button, armada);
											}

										}
										break;
									case 'I':
										fourController.addTargetToBattleshipFour((Button) target, armada);
										for (int i = 0; i < 100; i++) {
											Node button = buttonList.get(i);
											if (button.getId().toString().length() == 4) {
												if (fourController.highlightLengthFourHorizontalTargetFour(button, (Button) target, 'G', 'H', 'J'))
													fourController.addButtonToBattleshipFour(button, armada);
											}

										}
										break;
									case 'J':
										fourController.addTargetToBattleshipFour((Button) target, armada);
										for (int i = 0; i < 100; i++) {
											Node button = buttonList.get(i);
											if (button.getId().toString().length() == 4) {
												if (fourController.highlightLengthFourHorizontalTargetFour(button, (Button) target, 'G', 'H', 'I'))
													fourController.addButtonToBattleshipFour(button, armada);
											}

										}
										break;
									default:
										break;
									}
								}

							} else if (orientation == "Vertical") {

								if (event.getGestureSource() != target && event.getDragboard().hasString()) {
									if (target.getId().toString().length() == 3) {
										switch (target.getId().toString().charAt(2)) {
										case '2':
											fourController.addTargetToBattleship((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 3) {
													if (fourController.highlightLengthFourVerticalTargetThree(button, (Button) target, '1', '3', '4'))
														fourController.addButtonToBattleship(button, armada);
												}

											}
											break;
										case '3':
											fourController.addTargetToBattleship((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 3) {
													if (fourController.highlightLengthFourVerticalTargetThree(button, (Button) target, '2', '4', '5'))
														fourController.addButtonToBattleship(button, armada);
												}

											}
											break;
										case '4':
											fourController.addTargetToBattleship((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 3) {
													if (fourController.highlightLengthFourVerticalTargetThree(button, (Button) target, '3', '5', '6'))
														fourController.addButtonToBattleship(button, armada);
												}

											}
											break;
										case '5':
											fourController.addTargetToBattleship((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 3) {
													if (fourController.highlightLengthFourVerticalTargetThree(button, (Button) target, '4', '6', '7'))
														fourController.addButtonToBattleship(button, armada);
												}

											}
											break;
										case '6':
											fourController.addTargetToBattleship((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 3) {
													if (fourController.highlightLengthFourVerticalTargetThree(button, (Button) target, '5', '7', '8'))
														fourController.addButtonToBattleship(button, armada);
												}

											}
											break;
										case '7':
											fourController.addTargetToBattleship((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 3) {
													if (fourController.highlightLengthFourVerticalTargetThree(button, (Button) target, '6', '8', '9'))
														fourController.addButtonToBattleship(button, armada);
												}

											}
											break;
										case '8':
											fourController.addTargetToBattleship((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 3) {
													if (fourController.highlightLengthFourVerticalTargetThreeSpecialCaseOne(button, (Button) target, '9', '7'))
														fourController.addButtonToBattleship(button, armada);
												} else if (button.getId().toString().length() == 4) {
													if (fourController.highlightLengthFourVerticalTargetThreeSpecialCaseTwo(button, (Button) target, '1', '0'))
														fourController.addButtonToBattleship(button, armada);
												}

											}
											break;
										case '9':
											fourController.addTargetToBattleship((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 3) {
													if (fourController.highlightLengthFourVerticalTargetThreeSpecialCaseOne(button, (Button) target, '8', '7'))
														fourController.addButtonToBattleship(button, armada);
													
												} else if (button.getId().toString().length() == 4) {
													if (fourController.highlightLengthFourVerticalTargetThreeSpecialCaseTwo(button, (Button) target, '1', '0'))
														if (button.getId().toString().length() != 3) 
															fourController.addButtonToBattleship(button, armada);
												}

											}
											break;

										case '1':
											fourController.addTargetToBattleship((Button) target, armada);
											if (target.getId().toString().length() == 3) {
												for (int i = 0; i < 100; i++) {
													Node button = buttonList.get(i);
													if (button.getId().toString().length() == 3) {
														if (fourController.highlightLengthFourVerticalTargetThreeSpecialCaseTen(button, (Button) target, '2', '3', '4'))
															fourController.addButtonToBattleship(button, armada);
													}
												}
											}
											break;
										default:
											break;
										}
									} else if (target.getId().toString().length() == 4) {
										switch (target.getId().toString().charAt(2)) {
										case '1':
											fourController.addTargetToBattleshipFour((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 3) {
													if (fourController.highlightLengthFourVerticalTargetFour(button, (Button) target, '7', '8', '9'))
														fourController.addButtonToBattleship(button, armada);
												}
											}
											break;
										default:
											break;
										}
									}
								}

							}
							success = true;
						}
						pictureFour.setDisable(true);
						event.setDropCompleted(success);
						event.consume();
					}
				});
			}
		}
	}
	/*
	 * Sets up the actions for when image five is dropped on a button
	 */
	private void configureDroppedImageFive() {
		for (Node target : playerGrid.getChildren()) {
			if (target.getId() != null) {
				target.setOnDragDropped(new EventHandler<DragEvent>() {
					public void handle(DragEvent event) {
						/* data dropped */
						/* if there is a string data on dragboard, read it and use it */
						Dragboard db = event.getDragboard();
						boolean success = false;
						if (db.hasString()) {
							StringBuilder s = new StringBuilder();
							if (orientation == "Horizontal") {

								if (event.getGestureSource() != target && event.getDragboard().hasString()) {
//									System.out.println(target.getId());

									if (target.getId().toString().length() == 3) {
										switch (target.getId().toString().charAt(1)) {
										case 'A':
											fiveController.addTargetToCarrier((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 3) {
													if (fiveController.highlightLengthFiveHorizontalTargetThree(button, (Button) target, 'B', 'C', 'D', 'E'))
														fiveController.addButtonsToCarrier(button, armada);

												}

											}
											break;
										case 'B':
											fiveController.addTargetToCarrier((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 3) {
													if (fiveController.highlightLengthFiveHorizontalTargetThree(button, (Button) target, 'A', 'C', 'D', 'E'))
														fiveController.addButtonsToCarrier(button, armada);
												}

											}
											break;
										case 'C':
											fiveController.addTargetToCarrier((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 3) {
													if (fiveController.highlightLengthFiveHorizontalTargetThree(button, (Button) target, 'B', 'D', 'E', 'F'))
														fiveController.addButtonsToCarrier(button, armada);
												}

											}
											break;
										case 'D':
											fiveController.addTargetToCarrier((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 3) {
													if (fiveController.highlightLengthFiveHorizontalTargetThree(button, (Button) target, 'C', 'E', 'F', 'G'))
														fiveController.addButtonsToCarrier(button, armada);
												}

											}
											break;
										case 'E':
											fiveController.addTargetToCarrier((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 3) {
													if (fiveController.highlightLengthFiveHorizontalTargetThree(button, (Button) target, 'D', 'F', 'G', 'H'))
														fiveController.addButtonsToCarrier(button, armada);
												}

											}
											break;
										case 'F':
											fiveController.addTargetToCarrier((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 3) {
													if (fiveController.highlightLengthFiveHorizontalTargetThree(button, (Button) target, 'E', 'G', 'H', 'I'))
														fiveController.addButtonsToCarrier(button, armada);
												}

											}
											break;
										case 'G':
											fiveController.addTargetToCarrier((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 3) {
													if (fiveController.highlightLengthFiveHorizontalTargetThree(button, (Button) target, 'F', 'H', 'I', 'J'))
														fiveController.addButtonsToCarrier(button, armada);
												}

											}
											break;
										case 'H':
											fiveController.addTargetToCarrier((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 3) {
													if (fiveController.highlightLengthFiveHorizontalTargetThree(button, (Button) target, 'F', 'G', 'I', 'J'))
														fiveController.addButtonsToCarrier(button, armada);
												}

											}
											break;
										case 'I':
											fiveController.addTargetToCarrier((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 3) {
													if (fiveController.highlightLengthFiveHorizontalTargetThree(button, (Button) target, 'F', 'G', 'H', 'J'))
														fiveController.addButtonsToCarrier(button, armada);
												}

											}

										case 'J':
											fiveController.addTargetToCarrier((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 3) {
													if (fiveController.highlightLengthFiveHorizontalTargetThree(button, (Button) target, 'F', 'G', 'H', 'I'))
														fiveController.addButtonsToCarrier(button, armada);
												}

											}
											break;
										default:
											break;
										}
									} else if (target.getId().toString().length() == 4) {
										switch (target.getId().toString().charAt(1)) {
										case 'A':
											fiveController.addTargetToCarrierFour((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 4) {
													if (fiveController.highlightLengthFiveHorizontalTargetFour(button, (Button) target, 'B', 'C', 'D', 'E'))
														fiveController.addButtonsToCarrierFour(button, armada);
												}

											}
											break;
										case 'B':
											fiveController.addTargetToCarrierFour((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 4) {
													if (fiveController.highlightLengthFiveHorizontalTargetThree(button, (Button) target, 'A', 'C', 'D', 'E'))
														fiveController.addButtonsToCarrierFour(button, armada);
												}

											}
											break;
										case 'C':
											fiveController.addTargetToCarrierFour((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 4) {
													if (fiveController.highlightLengthFiveHorizontalTargetThree(button, (Button) target, 'B', 'D', 'E', 'F'))
														fiveController.addButtonsToCarrierFour(button, armada);
												}
											}
											break;
										case 'D':
											fiveController.addTargetToCarrierFour((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 4) {
													if (fiveController.highlightLengthFiveHorizontalTargetThree(button, (Button) target, 'C', 'E', 'F', 'G'))
														fiveController.addButtonsToCarrierFour(button, armada);
												}
											}
											break;
										case 'E':
											fiveController.addTargetToCarrierFour((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 4) {
													if (fiveController.highlightLengthFiveHorizontalTargetThree(button, (Button) target, 'D', 'F', 'G', 'H'))
														fiveController.addButtonsToCarrierFour(button, armada);
												}
											}
											break;
										case 'F':
											fiveController.addTargetToCarrierFour((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 4) {
													if (fiveController.highlightLengthFiveHorizontalTargetThree(button, (Button) target, 'E', 'G', 'H', 'I'))
														fiveController.addButtonsToCarrierFour(button, armada);
												}

											}
											break;
										case 'G':
											fiveController.addTargetToCarrierFour((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 4) {
													if (fiveController.highlightLengthFiveHorizontalTargetThree(button, (Button) target, 'F', 'H', 'I', 'J'))
														fiveController.addButtonsToCarrierFour(button, armada);
												}

											}
											break;
										case 'H':
											fiveController.addTargetToCarrierFour((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 4) {
													if (fiveController.highlightLengthFiveHorizontalTargetThree(button, (Button) target, 'F', 'G', 'I', 'J'))
														fiveController.addButtonsToCarrierFour(button, armada);
												}

											}
											break;
										case 'I':
											fiveController.addTargetToCarrierFour((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 4) {
													if (fiveController.highlightLengthFiveHorizontalTargetThree(button, (Button) target, 'F', 'G', 'H', 'J'))
														fiveController.addButtonsToCarrierFour(button, armada);
												}

											}
											break;
										case 'J':
											fiveController.addTargetToCarrierFour((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 4) {
													if (fiveController.highlightLengthFiveHorizontalTargetThree(button, (Button) target, 'F', 'G', 'H', 'I'))
														fiveController.addButtonsToCarrierFour(button, armada);
												}

											}
											break;
										default:
											break;
										}
									}
								}

							} else if (orientation == "Vertical") {

								if (event.getGestureSource() != target && event.getDragboard().hasString()) {
//									System.out.println(target.getId());

									if (target.getId().toString().length() == 3) {
										switch (target.getId().toString().charAt(2)) {
										case '2':
											fiveController.addTargetToCarrier((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 3) {
													if (fiveController.hightlightLengthFiveVerticalTargetThree(button, (Button) target, '1','3', '4','5'))
														fiveController.addButtonsToCarrier(button, armada);
												}

											}
											break;
										case '3':
											fiveController.addTargetToCarrier((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 3) {
													if (fiveController.hightlightLengthFiveVerticalTargetThree(button, (Button) target, '1','3', '4','5'))
														fiveController.addButtonsToCarrier(button, armada);
												}

											}
											break;
										case '4':
											fiveController.addTargetToCarrier((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 3) {
													if (fiveController.hightlightLengthFiveVerticalTargetThree(button, (Button) target, '3','5', '6','7'))
														fiveController.addButtonsToCarrier(button, armada);
												}

											}
											break;
										case '5':
											fiveController.addTargetToCarrier((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 3) {
													if (fiveController.hightlightLengthFiveVerticalTargetThree(button, (Button) target, '4','6', '7','8'))
														fiveController.addButtonsToCarrier(button, armada);
												}

											}
											break;
										case '6':
											fiveController.addTargetToCarrier((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 3) {
													if (fiveController.hightlightLengthFiveVerticalTargetThree(button, (Button) target, '5','7', '8','9'))
														fiveController.addButtonsToCarrier(button, armada);
												}

											}
											break;
										case '7':
											fiveController.addTargetToCarrier((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 3) {
													if (fiveController.hightlightLengthFiveVerticalTargetThree(button, (Button) target, '5','6', '8','9'))
														fiveController.addButtonsToCarrier(button, armada);
												}

											}
											break;
										case '8':
											fiveController.addTargetToCarrier((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 3) {
													if (fiveController.highlightLengthFiveVerticalTargetThreeSpecialCaseThree(button, (Button) target, '6','7', '9'))
														fiveController.addButtonsToCarrier(button, armada);
												} else if (button.getId().toString().length() == 4) {
													if (fiveController.highlightLengthFiveVerticalTargetThreeSpecialCaseFour(button, (Button) target, '1','0'))
														fiveController.addButtonsToCarrier(button, armada);
												}

											}
											break;
										case '9':
											fiveController.addTargetToCarrier((Button) target, armada);
											for (int i = 0; i < 100; i++) {
												Node button = buttonList.get(i);
												if (button.getId().toString().length() == 3) {
													if (fiveController.highlightLengthFiveVerticalTargetThreeSpecialCaseThree(button, (Button) target, '6', '7', '8'))
														fiveController.addButtonsToCarrier(button, armada);
													
												} else if (button.getId().toString().length() == 4) {
													if (fiveController.highlightLengthFiveVerticalTargetThreeSpecialCaseFour(button, (Button) target, '1','0'))
														if (button.getId().toString().length() == 4) 
															fiveController.addButtonsToCarrier(button, armada);
												}

											}
											break;

										case '1':
											fiveController.addTargetToCarrier((Button) target, armada);
											if (target.getId().toString().length() == 3) {
												for (int i = 0; i < 100; i++) {
													Node button = buttonList.get(i);
													if (button.getId().toString().length() == 3) {
														if (fiveController.highlightLengthFiveVerticalTargetThreeSpecialCaseOne(button, (Button) target, '2', '3', '4', '5'))
															fiveController.addButtonsToCarrier(button, armada);
													}
												}
											}
											break;
										default:
											break;
										}
									} else if (target.getId().toString().length() == 4) {
										switch (target.getId().toString().charAt(2)) {
										case '1':
											fiveController.addTargetToCarrierFour((Button) target, armada);
											if (target.getId().toString().length() == 4) {
												for (int i = 0; i < 100; i++) {
													Node button = buttonList.get(i);
													if (button.getId().toString().length() == 3) {
														if (fiveController.highlightLengthFiveVerticalTargetFour(button, (Button) target, '6', '7', '8', '9'))
															fiveController.addButtonsToCarrier(button, armada);
													}
												}
											}
											break;
										default:
											break;
										}
									}
								}

							}
							success = true;
						}
						pictureFive.setDisable(true);
						event.setDropCompleted(success);
						event.consume();
					}
				});
			}
		}

	}

	
	/*
	 * Determines what buttons are highlighted when image  one is dragged horizontally
	 */
	private void calculateHorizontalOne(Button target, DragEvent event) {
		if (event.getGestureSource() != target && event.getDragboard().hasString()) {
//			System.out.println(target.getId());

			if (target.getId().toString().length() == 3) {
				switch (target.getId().toString().charAt(1)) {
				case 'A':

					target.setStyle("-fx-background-color: black");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (twoController.highlightLengthTwoHorizontalTargetThree(button, target, 'B'))
								button.setStyle("-fx-background-color: black");
						}

					}
				case 'B':

					target.setStyle("-fx-background-color: black");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (twoController.highlightLengthTwoHorizontalTargetThree(button, target, 'A'))
								button.setStyle("-fx-background-color: black");
						}

					}
					break;
				case 'C':

					target.setStyle("-fx-background-color: black");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (twoController.highlightLengthTwoHorizontalTargetThree(button,target, 'B'))
								button.setStyle("-fx-background-color:black");
						}

					}
					break;
				case 'D':

					target.setStyle("-fx-background-color: black");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (twoController.highlightLengthTwoHorizontalTargetThree(button, target, 'C'))
								button.setStyle("-fx-background-color:black");
						}

					}
					break;
				case 'E':

					target.setStyle("-fx-background-color: black");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (twoController.highlightLengthTwoHorizontalTargetThree(button, target, 'D'))
									
									/*(button.getId().toString().charAt(1) == 'D')
									&& (button.getId().toString().charAt(2) == target.getId().toString().charAt(2)))*/
								button.setStyle("-fx-background-color:black");
						}

					}
					break;
				case 'F':

					target.setStyle("-fx-background-color: black");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (twoController.highlightLengthTwoHorizontalTargetThree(button, target, 'E'))
								button.setStyle("-fx-background-color:black");
						}

					}
					break;
				case 'G':

					target.setStyle("-fx-background-color: black");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (twoController.highlightLengthTwoHorizontalTargetThree(button, target, 'F'))
								button.setStyle("-fx-background-color:black");
						}

					}
					break;
				case 'H':

					target.setStyle("-fx-background-color: black");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (twoController.highlightLengthTwoHorizontalTargetThree(button, target, 'G'))
								button.setStyle("-fx-background-color:black");
						}

					}
					break;
				case 'I':

					target.setStyle("-fx-background-color: black");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (twoController.highlightLengthTwoHorizontalTargetThree(button, target, 'H'))
								button.setStyle("-fx-background-color:black");
						}

					}
					break;
				case 'J':

					target.setStyle("-fx-background-color: black");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (twoController.highlightLengthTwoHorizontalTargetThree(button, target, 'I'))
								button.setStyle("-fx-background-color:black");
						}

					}
					break;

				default:
					target.setStyle("-fx-background-color: green");
					break;
				}
			} else if (target.getId().toString().length() == 4) {
				switch (target.getId().toString().charAt(1)) {
				case 'A':

					target.setStyle("-fx-background-color: black");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 4) {
							if (twoController.highlightLengthTwoHorizontalTargetFour(button, target, 'B'))
								button.setStyle("-fx-background-color:black");
						}

					}
				case 'B':

					target.setStyle("-fx-background-color: black");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 4) {
							if (twoController.highlightLengthTwoHorizontalTargetFour(button, target, 'A'))
								button.setStyle("-fx-background-color:black");
						}

					}
					break;
				case 'C':

					target.setStyle("-fx-background-color: black");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 4) {
							if (twoController.highlightLengthTwoHorizontalTargetFour(button, target, 'B'))
								button.setStyle("-fx-background-color:black");
						}
					}
					break;
				case 'D':

					target.setStyle("-fx-background-color: black");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 4) {
							if (twoController.highlightLengthTwoHorizontalTargetFour(button, target, 'C'))
								button.setStyle("-fx-background-color:black");
						}
					}
					break;
				case 'E':

					target.setStyle("-fx-background-color: black");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 4) {
							if (twoController.highlightLengthTwoHorizontalTargetFour(button, target, 'D'))
								button.setStyle("-fx-background-color:black");
						}
					}
					break;
				case 'F':

					target.setStyle("-fx-background-color: black");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 4) {
							if (twoController.highlightLengthTwoHorizontalTargetFour(button, target, 'E'))
								button.setStyle("-fx-background-color:black");
						}

					}
					break;
				case 'G':

					target.setStyle("-fx-background-color: black");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 4) {
							if (twoController.highlightLengthTwoHorizontalTargetFour(button, target, 'F'))
								button.setStyle("-fx-background-color:black");
						}

					}
					break;
				case 'H':

					target.setStyle("-fx-background-color: black");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 4) {
							if (twoController.highlightLengthTwoHorizontalTargetFour(button, target, 'G'))
								button.setStyle("-fx-background-color:black");
						}

					}
					break;
				case 'I':

					target.setStyle("-fx-background-color: black");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 4) {
							if (twoController.highlightLengthTwoHorizontalTargetFour(button, target, 'H'))
								button.setStyle("-fx-background-color:black");
						}

					}
					break;
				case 'J':

					target.setStyle("-fx-background-color: black");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 4) {
							if (twoController.highlightLengthTwoHorizontalTargetFour(button, target, 'I'))
								button.setStyle("-fx-background-color:black");
						}

					}
					break;
				default:
					target.setStyle("-fx-background-color: green");
					break;
				}
			}
		}
	}
	/*
	 * Determines what buttons are highlighted when image  one is dragged horizontally
	 */
	private void calculateVerticalOne(Button target, DragEvent event) {

		if (event.getGestureSource() != target && event.getDragboard().hasString()) {
//			System.out.println(target.getId());

			if (target.getId().toString().length() == 3) {
				switch (target.getId().toString().charAt(2)) {
				case '1':
					if (target.getId().toString().length() == 3) {
						target.setStyle("-fx-background-color: black");
						for (int i = 0; i < 100; i++) {
							Node button = buttonList.get(i);
							if (button.getId().toString().length() == 3) {
								if (twoController.highlightLengthTwoVertical(button, target, '2'))
									button.setStyle("-fx-background-color:black");
							}

						}
					} else if (target.getId().toString().charAt(4) == '0') {
						target.setStyle("-fx-background-color: black");
						for (int i = 0; i < 100; i++) {
							Node button = buttonList.get(i);
							if (twoController.highlightLengthTwoVertical(button, target, '9'))
								button.setStyle("-fx-background-color:black");
						}
					}
					break;
				case '2':

					target.setStyle("-fx-background-color: black");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (twoController.highlightLengthTwoVertical(button, target, '1'))
								button.setStyle("-fx-background-color:black");
						}

					}
					break;
				case '3':

					target.setStyle("-fx-background-color: black");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (twoController.highlightLengthTwoVertical(button, target, '2'))
								button.setStyle("-fx-background-color:black");
						}

					}
					break;
				case '4':

					target.setStyle("-fx-background-color: black");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (twoController.highlightLengthTwoVertical(button, target, '3'))
								button.setStyle("-fx-background-color:black");
						}

					}
					break;
				case '5':

					target.setStyle("-fx-background-color: black");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (twoController.highlightLengthTwoVertical(button, target, '4'))
								button.setStyle("-fx-background-color:black");
						}

					}
					break;
				case '6':

					target.setStyle("-fx-background-color: black");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (twoController.highlightLengthTwoVertical(button, target, '5'))
								button.setStyle("-fx-background-color:black");
						}

					}
					break;
				case '7':

					target.setStyle("-fx-background-color: black");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (twoController.highlightLengthTwoVertical(button, target, '6'))
								button.setStyle("-fx-background-color:black");
						}

					}
					break;
				case '8':

					target.setStyle("-fx-background-color: black");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (twoController.highlightLengthTwoVertical(button, target, '7'))
								button.setStyle("-fx-background-color:black");
						}

					}
					break;
				case '9':

					target.setStyle("-fx-background-color: black");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (twoController.highlightLengthTwoVertical(button, target, '8'))
							button.setStyle("-fx-background-color:black");

					}
					break;

				default:
					target.setStyle("-fx-background-color: green");
					break;
				}
			} else if (target.getId().toString().length() == 4) {

				switch (target.getId().toString().charAt(2)) {
				case '1':
					if (target.getId().toString().length() == 4) {
						target.setStyle("-fx-background-color: black");
						for (int i = 0; i < 100; i++) {
							Node button = buttonList.get(i);
							if (twoController.highlightLengthTwoVertical(button, target, '9'))
								button.setStyle("-fx-background-color:black");
						}
					}
					break;

				default:
					target.setStyle("-fx-background-color: green");
					break;
				}

			}
		}

	}

	
	/*
	 * Determines what buttons are highlighted when image two is dragged horizontally
	 */
	private void calculateHorizontalTwo(Button target, DragEvent event) {

		if (event.getGestureSource() != target && event.getDragboard().hasString()) {
//			System.out.println(target.getId());

			if (target.getId().toString().length() == 3) {
				switch (target.getId().toString().charAt(1)) {
				case 'A':

					target.setStyle("-fx-background-color: green");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (threeController.highlightLengthThreeHorizontalTargetThree(button, target, 'B', 'C'))
								button.setStyle("-fx-background-color:green");
						}

					}
					break;

				case 'B':

					target.setStyle("-fx-background-color: green");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (threeController.highlightLengthThreeHorizontalTargetThree(button, target, 'A', 'C'))
								button.setStyle("-fx-background-color:green");
						}

					}
					break;
				case 'C':

					target.setStyle("-fx-background-color: green");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (threeController.highlightLengthThreeHorizontalTargetThree(button, target, 'B', 'D'))
								button.setStyle("-fx-background-color:green");
						}

					}
					break;
				case 'D':

					target.setStyle("-fx-background-color: green");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (threeController.highlightLengthThreeHorizontalTargetThree(button, target, 'C', 'E'))
								button.setStyle("-fx-background-color:green");
						}

					}
					break;
				case 'E':

					target.setStyle("-fx-background-color: green");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (threeController.highlightLengthThreeHorizontalTargetThree(button, target, 'D', 'F'))
								button.setStyle("-fx-background-color:green");
						}

					}
					break;
				case 'F':

					target.setStyle("-fx-background-color: green");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (threeController.highlightLengthThreeHorizontalTargetThree(button, target, 'E', 'G'))
								button.setStyle("-fx-background-color:green");
						}

					}
					break;
				case 'G':

					target.setStyle("-fx-background-color: green");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (threeController.highlightLengthThreeHorizontalTargetThree(button, target, 'F', 'H'))
								button.setStyle("-fx-background-color:green");
						}

					}
					break;
				case 'H':

					target.setStyle("-fx-background-color: green");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (threeController.highlightLengthThreeHorizontalTargetThree(button, target, 'G', 'I'))
								button.setStyle("-fx-background-color:green");
						}

					}
					break;
				case 'I':

					target.setStyle("-fx-background-color: green");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (threeController.highlightLengthThreeHorizontalTargetThree(button, target, 'H', 'J'))
								button.setStyle("-fx-background-color:green");
						}

					}
					break;
				case 'J':

					target.setStyle("-fx-background-color: green");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (threeController.highlightLengthThreeHorizontalTargetThree(button, target, 'H', 'I'))
								button.setStyle("-fx-background-color:green");
						}

					}
					break;
				default:
					target.setStyle("-fx-background-color: green");
					break;
				}
			} else if (target.getId().toString().length() == 4) {
				switch (target.getId().toString().charAt(1)) {
				case 'A':

					target.setStyle("-fx-background-color: green");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 4) {
							if (threeController.highlightLengthThreeHorizontalTargetFour(button, target, 'B', 'C'))
								button.setStyle("-fx-background-color:green");
						}

					}
					break;
				case 'B':

					target.setStyle("-fx-background-color: green");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 4) {
							if (threeController.highlightLengthThreeHorizontalTargetFour(button, target, 'A', 'C'))
								button.setStyle("-fx-background-color:green");
						}

					}
					break;
				case 'C':

					target.setStyle("-fx-background-color: green");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 4) {
							if (threeController.highlightLengthThreeHorizontalTargetFour(button, target, 'B', 'D'))
								button.setStyle("-fx-background-color:green");
						}
					}
					break;
				case 'D':

					target.setStyle("-fx-background-color: green");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 4) {
							if (threeController.highlightLengthThreeHorizontalTargetFour(button, target, 'C', 'E'))
								button.setStyle("-fx-background-color:green");
						}
					}
					break;
				case 'E':

					target.setStyle("-fx-background-color: green");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 4) {
							if (threeController.highlightLengthThreeHorizontalTargetFour(button, target, 'D', 'F'))
								button.setStyle("-fx-background-color:green");
						}
					}
					break;
				case 'F':

					target.setStyle("-fx-background-color: green");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 4) {
							if (threeController.highlightLengthThreeHorizontalTargetFour(button, target, 'E', 'G'))
								button.setStyle("-fx-background-color:green");
						}

					}
					break;
				case 'G':

					target.setStyle("-fx-background-color: green");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 4) {
							if (threeController.highlightLengthThreeHorizontalTargetFour(button, target, 'F', 'H'))
								button.setStyle("-fx-background-color:green");
						}

					}
					break;
				case 'H':

					target.setStyle("-fx-background-color: green");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 4) {
							if (threeController.highlightLengthThreeHorizontalTargetFour(button, target, 'G', 'I'))
								button.setStyle("-fx-background-color:green");
						}

					}
					break;
				case 'I':

					target.setStyle("-fx-background-color: green");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 4) {
							if (threeController.highlightLengthThreeHorizontalTargetFour(button, target, 'H', 'J'))
								button.setStyle("-fx-background-color:green");
						}

					}
					break;
				case 'J':

					target.setStyle("-fx-background-color: green");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 4) {
							if (threeController.highlightLengthThreeHorizontalTargetFour(button, target, 'H', 'I'))
								button.setStyle("-fx-background-color:green");
						}

					}
					break;
				default:
					target.setStyle("-fx-background-color: green");
					break;
				}
			}
		}

	}
	/*
	 * Determines what buttons are highlighted when image two is dragged vertically
	 */
	private void calculateVerticalTwo(Button target, DragEvent event) {

		if (event.getGestureSource() != target && event.getDragboard().hasString()) {
//			System.out.println(target.getId());

			if (target.getId().toString().length() == 3) {
				switch (target.getId().toString().charAt(2)) {
				case '1':
					if (target.getId().toString().length() == 3) {
						target.setStyle("-fx-background-color: green");
						for (int i = 0; i < 100; i++) {
							Node button = buttonList.get(i);
							if (button.getId().toString().length() == 3) {
								if (threeController.highlightLengthThreeVerticalTargetThreeSpecialCaseOneA(button, target, '2', '3'))
									button.setStyle("-fx-background-color:green");
							}

						}
					} else if (target.getId().toString().charAt(4) == '0') {
						target.setStyle("-fx-background-color: green");
						for (int i = 0; i < 100; i++) {
							Node button = buttonList.get(i);
							if (threeController.highlightLengthThreeVerticalTargetThreeSpecialCaseOneB(button, target, '8', '9'))
								button.setStyle("-fx-background-color:green");
						}
					}
					break;

				case '2':

					target.setStyle("-fx-background-color: green");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (threeController.highlightLengthThreeVerticalTargetThree(button, target, '1', '3'))
								button.setStyle("-fx-background-color:green");
						}

					}
					break;
				case '3':

					target.setStyle("-fx-background-color: green");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (threeController.highlightLengthThreeVerticalTargetThree(button, target, '2', '4'))
								button.setStyle("-fx-background-color:green");
						}

					}
					break;
				case '4':

					target.setStyle("-fx-background-color: green");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (threeController.highlightLengthThreeVerticalTargetThree(button, target, '3', '5'))
								button.setStyle("-fx-background-color:green");
						}

					}
					break;
				case '5':

					target.setStyle("-fx-background-color: green");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (threeController.highlightLengthThreeVerticalTargetThree(button, target, '4', '6'))
								button.setStyle("-fx-background-color:green");
						}

					}
					break;
				case '6':

					target.setStyle("-fx-background-color: green");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (threeController.highlightLengthThreeVerticalTargetThree(button, target, '5', '7'))
								button.setStyle("-fx-background-color:green");
						}

					}
					break;
				case '7':

					target.setStyle("-fx-background-color: green");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (threeController.highlightLengthThreeVerticalTargetThree(button, target, '6', '8'))
								button.setStyle("-fx-background-color:green");
						}

					}
					break;
				case '8':

					target.setStyle("-fx-background-color: green");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (threeController.highlightLengthThreeVerticalTargetThree(button, target, '7', '9'))
								button.setStyle("-fx-background-color:green");
						}

					}
					break;
				case '9':

					target.setStyle("-fx-background-color: green");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (threeController.highlightLengthThreeVerticalTargetThreeSpecialCaseNine(button, target, '8'))
								if (button.getId().toString().length() != 4) {
									button.setStyle("-fx-background-color:green");
								}
						} else if (button.getId().toString().length() == 4) {
							if (threeController.highlightLengthThreeVerticalTargetThreeSpecialCaseTen(button, target, '1', '0'))
								if (button.getId().toString().length() != 3) {
									button.setStyle("-fx-background-color:green");
								}
						}

					}
					break;
				default:
					target.setStyle("-fx-background-color: green");
					break;
				}
			} else if (target.getId().toString().length() == 4) {
				switch (target.getId().toString().charAt(2)) {
				case '1':
					if (target.getId().toString().length() == 4) {
						target.setStyle("-fx-background-color: green");
						for (int i = 0; i < 100; i++) {
							Node button = buttonList.get(i);
							if (threeController.highlightLengthThreeVerticalTargetFour(button, target, '8', '9'))
								button.setStyle("-fx-background-color:green");
						}
					}
					break;
				default:
					target.setStyle("-fx-background-color: green");
					break;
				}
			}
		}

	}

	
	/*
	 * Determines what buttons are highlighted when image  three is dragged horizontally
	 */
	private void calculateHorizontalThree(Button target, DragEvent event) {

		if (event.getGestureSource() != target && event.getDragboard().hasString()) {
//			System.out.println(target.getId());

			if (target.getId().toString().length() == 3) {
				switch (target.getId().toString().charAt(1)) {
				case 'A':

					target.setStyle("-fx-background-color: aqua");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (threeController.highlightLengthThreeHorizontalTargetThree(button, target, 'B', 'C'))
								button.setStyle("-fx-background-color:aqua");
						}

					}
					break;

				case 'B':

					target.setStyle("-fx-background-color: aqua");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (threeController.highlightLengthThreeHorizontalTargetThree(button, target, 'A', 'C'))
								button.setStyle("-fx-background-color:aqua");
						}

					}
					break;
				case 'C':

					target.setStyle("-fx-background-color: aqua");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (threeController.highlightLengthThreeHorizontalTargetThree(button, target, 'B', 'D'))
								button.setStyle("-fx-background-color:aqua");
						}

					}
					break;
				case 'D':

					target.setStyle("-fx-background-color: aqua");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (threeController.highlightLengthThreeHorizontalTargetThree(button, target, 'C', 'E'))
								button.setStyle("-fx-background-color:aqua");
						}

					}
					break;
				case 'E':

					target.setStyle("-fx-background-color: aqua");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (threeController.highlightLengthThreeHorizontalTargetThree(button, target, 'D', 'F'))
								button.setStyle("-fx-background-color:aqua");
						}

					}
					break;
				case 'F':

					target.setStyle("-fx-background-color: aqua");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (threeController.highlightLengthThreeHorizontalTargetThree(button, target, 'E', 'G'))
								button.setStyle("-fx-background-color:aqua");
						}

					}
					break;
				case 'G':

					target.setStyle("-fx-background-color: aqua");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (threeController.highlightLengthThreeHorizontalTargetThree(button, target, 'F', 'H'))
								button.setStyle("-fx-background-color:aqua");
						}

					}
					break;
				case 'H':

					target.setStyle("-fx-background-color: aqua");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (threeController.highlightLengthThreeHorizontalTargetThree(button, target, 'G', 'I'))
								button.setStyle("-fx-background-color:aqua");
						}

					}
					break;
				case 'I':

					target.setStyle("-fx-background-color: aqua");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (threeController.highlightLengthThreeHorizontalTargetThree(button, target, 'H', 'J'))
								button.setStyle("-fx-background-color:aqua");
						}

					}
					break;
				case 'J':

					target.setStyle("-fx-background-color: aqua");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (threeController.highlightLengthThreeHorizontalTargetThree(button, target, 'H', 'I'))
								button.setStyle("-fx-background-color:aqua");
						}

					}
					break;
				default:
					target.setStyle("-fx-background-color: green");
					break;
				}
			} else if (target.getId().toString().length() == 4) {
				switch (target.getId().toString().charAt(1)) {
				case 'A':

					target.setStyle("-fx-background-color: aqua");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 4) {
							if (threeController.highlightLengthThreeHorizontalTargetFour(button, target, 'B', 'C'))
								button.setStyle("-fx-background-color:aqua");
						}

					}
					break;
				case 'B':

					target.setStyle("-fx-background-color: aqua");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 4) {
							if (threeController.highlightLengthThreeHorizontalTargetFour(button, target, 'A', 'C'))
								button.setStyle("-fx-background-color:aqua");
						}

					}
					break;
				case 'C':

					target.setStyle("-fx-background-color: aqua");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 4) {
							if (threeController.highlightLengthThreeHorizontalTargetFour(button, target, 'B', 'D'))
								button.setStyle("-fx-background-color:aqua");
						}
					}
					break;
				case 'D':

					target.setStyle("-fx-background-color: aqua");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 4) {
							if (threeController.highlightLengthThreeHorizontalTargetFour(button, target, 'C', 'E'))
								button.setStyle("-fx-background-color:aqua");
						}
					}
					break;
				case 'E':

					target.setStyle("-fx-background-color: aqua");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 4) {
							if (threeController.highlightLengthThreeHorizontalTargetFour(button, target, 'D', 'F'))
								button.setStyle("-fx-background-color:aqua");
						}
					}
					break;
				case 'F':

					target.setStyle("-fx-background-color: aqua");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 4) {
							if (threeController.highlightLengthThreeHorizontalTargetFour(button, target, 'E', 'G'))
								button.setStyle("-fx-background-color:aqua");
						}

					}
					break;
				case 'G':

					target.setStyle("-fx-background-color: aqua");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 4) {
							if (threeController.highlightLengthThreeHorizontalTargetFour(button, target, 'F', 'H'))
								button.setStyle("-fx-background-color:aqua");
						}

					}
					break;
				case 'H':

					target.setStyle("-fx-background-color: aqua");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 4) {
							if (threeController.highlightLengthThreeHorizontalTargetFour(button, target, 'G', 'I'))
								button.setStyle("-fx-background-color:aqua");
						}

					}
					break;
				case 'I':

					target.setStyle("-fx-background-color: aqua");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 4) {
							if (threeController.highlightLengthThreeHorizontalTargetFour(button, target, 'H', 'J'))
								button.setStyle("-fx-background-color:aqua");
						}

					}
					break;
				case 'J':

					target.setStyle("-fx-background-color: aqua");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 4) {
							if (threeController.highlightLengthThreeHorizontalTargetFour(button, target, 'H', 'I'))
								button.setStyle("-fx-background-color:aqua");
						}

					}
					break;
				default:
					target.setStyle("-fx-background-color: green");
					break;
				}
			}
		}

	}
	/*
	 * Determines what buttons are highlighted when image  three is dragged vertically
	 */
	private void calculateVerticalThree(Button target, DragEvent event) {

		if (event.getGestureSource() != target && event.getDragboard().hasString()) {
//			System.out.println(target.getId());

			if (target.getId().toString().length() == 3) {
				switch (target.getId().toString().charAt(2)) {
				case '1':
					if (target.getId().toString().length() == 3) {
						target.setStyle("-fx-background-color: aqua");
						for (int i = 0; i < 100; i++) {
							Node button = buttonList.get(i);
							if (button.getId().toString().length() == 3) {
								if (threeController.highlightLengthThreeVerticalTargetThreeSpecialCaseOneA(button, target, '2', '3'))
									button.setStyle("-fx-background-color:aqua");
							}

						}
					} else if (target.getId().toString().charAt(4) == '0') {
						target.setStyle("-fx-background-color: aqua");
						for (int i = 0; i < 100; i++) {
							Node button = buttonList.get(i);
							if (threeController.highlightLengthThreeVerticalTargetThreeSpecialCaseOneB(button, target, '8', '9'))
								button.setStyle("-fx-background-color:aqua");
						}
					}
					break;

				case '2':

					target.setStyle("-fx-background-color: aqua");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (threeController.highlightLengthThreeVerticalTargetThree(button, target, '1', '3'))
								button.setStyle("-fx-background-color:aqua");
						}

					}
					break;
				case '3':

					target.setStyle("-fx-background-color: aqua");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (threeController.highlightLengthThreeVerticalTargetThree(button, target, '2', '4'))
								button.setStyle("-fx-background-color:aqua");
						}

					}
					break;
				case '4':

					target.setStyle("-fx-background-color: aqua");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (threeController.highlightLengthThreeVerticalTargetThree(button, target, '3', '5'))
								button.setStyle("-fx-background-color:aqua");
						}

					}
					break;
				case '5':

					target.setStyle("-fx-background-color: aqua");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (threeController.highlightLengthThreeVerticalTargetThree(button, target, '4', '6'))
								button.setStyle("-fx-background-color:aqua");
						}

					}
					break;
				case '6':

					target.setStyle("-fx-background-color: aqua");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (threeController.highlightLengthThreeVerticalTargetThree(button, target, '5', '7'))
								button.setStyle("-fx-background-color:aqua");
						}

					}
					break;
				case '7':

					target.setStyle("-fx-background-color: aqua");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (threeController.highlightLengthThreeVerticalTargetThree(button, target, '6', '8'))
								button.setStyle("-fx-background-color:aqua");
						}

					}
					break;
				case '8':

					target.setStyle("-fx-background-color: aqua");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (threeController.highlightLengthThreeVerticalTargetThree(button, target, '7', '9'))
								button.setStyle("-fx-background-color:aqua");
						}

					}
					break;
				case '9':

					target.setStyle("-fx-background-color: aqua");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (threeController.highlightLengthThreeVerticalTargetThreeSpecialCaseNine(button, target, '8'))
								if (button.getId().toString().length() != 4) {
									button.setStyle("-fx-background-color:aqua");
								}
						} else if (button.getId().toString().length() == 4) {
							if (threeController.highlightLengthThreeVerticalTargetThreeSpecialCaseTen(button, target, '1', '0'))
								if (button.getId().toString().length() != 3) {
									button.setStyle("-fx-background-color:aqua");
								}
						}

					}
					break;
				default:
					target.setStyle("-fx-background-color: green");
					break;
				}
			} else if (target.getId().toString().length() == 4) {
				switch (target.getId().toString().charAt(2)) {
				case '1':
					if (target.getId().toString().length() == 4) {
						target.setStyle("-fx-background-color: aqua");
						for (int i = 0; i < 100; i++) {
							Node button = buttonList.get(i);
							if (threeController.highlightLengthThreeVerticalTargetFour(button, target, '8', '9'))
								button.setStyle("-fx-background-color:aqua");
						}
					}
					break;
				default:
					target.setStyle("-fx-background-color: green");
					break;
				}
			}
		}

	}

	
	/*
	 * Determines what buttons are highlighted when image  four is dragged horizontally
	 */
	private void calculateHorizontalFour(Button target, DragEvent event) {

		if (event.getGestureSource() != target && event.getDragboard().hasString()) {

			if (target.getId().toString().length() == 3) {
				switch (target.getId().toString().charAt(1)) {
				case 'A':

					target.setStyle("-fx-background-color: red");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (fourController.highlightLengthFourHorizontalTargetThree(button, target, 'B', 'C', 'D'))
								button.setStyle("-fx-background-color:red");
						}

					}
					break;
				case 'B':

					target.setStyle("-fx-background-color: red");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (fourController.highlightLengthFourHorizontalTargetThree(button, target, 'A', 'C', 'D'))
								button.setStyle("-fx-background-color:red");
						}

					}
					break;
				case 'C':

					target.setStyle("-fx-background-color: red");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (fourController.highlightLengthFourHorizontalTargetThree(button, target, 'B', 'D', 'E'))
								button.setStyle("-fx-background-color:red");
						}

					}
					break;
				case 'D':

					target.setStyle("-fx-background-color: red");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (fourController.highlightLengthFourHorizontalTargetThree(button, target, 'C', 'E', 'F'))
								button.setStyle("-fx-background-color:red");
						}

					}
					break;
				case 'E':

					target.setStyle("-fx-background-color: red");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (fourController.highlightLengthFourHorizontalTargetThree(button, target, 'D', 'F', 'G'))
								button.setStyle("-fx-background-color:red");
						}

					}
					break;
				case 'F':

					target.setStyle("-fx-background-color: red");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (fourController.highlightLengthFourHorizontalTargetThree(button, target, 'E', 'G', 'H'))
								button.setStyle("-fx-background-color:red");
						}

					}
					break;
				case 'G':

					target.setStyle("-fx-background-color: red");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (fourController.highlightLengthFourHorizontalTargetThree(button, target, 'F', 'H', 'I'))
								button.setStyle("-fx-background-color:red");
						}

					}
					break;
				case 'H':

					target.setStyle("-fx-background-color: red");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (fourController.highlightLengthFourHorizontalTargetThree(button, target, 'G', 'I', 'J'))
								button.setStyle("-fx-background-color:red");
						}

					}
					break;
				case 'I':

					target.setStyle("-fx-background-color: red");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (fourController.highlightLengthFourHorizontalTargetThree(button, target, 'G', 'H', 'J'))
								button.setStyle("-fx-background-color:red");
						}

					}
					break;
				case 'J':

					target.setStyle("-fx-background-color: red");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (fourController.highlightLengthFourHorizontalTargetThree(button, target, 'G', 'H', 'I'))
								button.setStyle("-fx-background-color:red");
						}

					}
					break;
				default:
					target.setStyle("-fx-background-color: green");
					break;
				}
			} else if (target.getId().toString().length() == 4) {
				switch (target.getId().toString().charAt(1)) {
				case 'A':

					target.setStyle("-fx-background-color: red");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 4) {
							if (fourController.highlightLengthFourHorizontalTargetFour(button, target, 'B', 'C', 'D'))
								button.setStyle("-fx-background-color:red");
						}

					}
					break;
				case 'B':

					target.setStyle("-fx-background-color: red");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 4) {
							if (fourController.highlightLengthFourHorizontalTargetFour(button, target, 'A', 'C', 'D'))
								button.setStyle("-fx-background-color:red");
						}

					}
					break;
				case 'C':

					target.setStyle("-fx-background-color: red");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 4) {
							if (fourController.highlightLengthFourHorizontalTargetFour(button, target, 'B', 'D', 'E'))
								button.setStyle("-fx-background-color:red");
						}
					}
					break;
				case 'D':

					target.setStyle("-fx-background-color: red");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 4) {
							if (fourController.highlightLengthFourHorizontalTargetFour(button, target, 'C', 'E', 'F'))
								button.setStyle("-fx-background-color:red");
						}
					}
					break;
				case 'E':

					target.setStyle("-fx-background-color: red");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 4) {
							if (fourController.highlightLengthFourHorizontalTargetFour(button, target, 'D', 'F', 'G'))
								button.setStyle("-fx-background-color:red");
						}
					}
					break;
				case 'F':

					target.setStyle("-fx-background-color: red");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 4) {
							if (fourController.highlightLengthFourHorizontalTargetFour(button, target, 'E', 'G', 'H'))
								button.setStyle("-fx-background-color:red");
						}

					}
					break;
				case 'G':

					target.setStyle("-fx-background-color: red");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 4) {
							if (fourController.highlightLengthFourHorizontalTargetFour(button, target, 'F', 'H', 'I'))
								button.setStyle("-fx-background-color:red");
						}

					}
					break;
				case 'H':

					target.setStyle("-fx-background-color: red");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 4) {
							if (fourController.highlightLengthFourHorizontalTargetFour(button, target, 'G', 'I','J'))
								button.setStyle("-fx-background-color:red");
						}

					}
					break;
				case 'I':

					target.setStyle("-fx-background-color: red");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 4) {
							if (fourController.highlightLengthFourHorizontalTargetFour(button, target, 'G', 'H','J'))
								button.setStyle("-fx-background-color:red");
						}

					}
					break;
				case 'J':

					target.setStyle("-fx-background-color: red");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 4) {
							if (fourController.highlightLengthFourHorizontalTargetFour(button, target, 'G', 'H','I'))
								button.setStyle("-fx-background-color:red");
						}

					}
					break;
				default:
					target.setStyle("-fx-background-color: green");
					break;
				}
			}
		}

	}
	/*
	 * Determines what buttons are highlighted when image  four is dragged vertically
	 */
	private void calculateVerticalFour(Button target, DragEvent event) {

		if (event.getGestureSource() != target && event.getDragboard().hasString()) {
//			System.out.println(target.getId());

			if (target.getId().toString().length() == 3) {
				switch (target.getId().toString().charAt(2)) {
				case '2':

					target.setStyle("-fx-background-color: red");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (fourController.highlightLengthFourVerticalTargetThree(button, target, '1', '3', '4'))
									
									
									/*(button.getId().toString().charAt(2) == '1')
									&& (button.getId().toString().charAt(1) == target.getId().toString().charAt(1))
									|| (button.getId().toString().charAt(2) == '3') && (button.getId().toString()
											.charAt(1) == target.getId().toString().charAt(1))
									|| (button.getId().toString().charAt(2) == '4') && (button.getId().toString()
											.charAt(1) == target.getId().toString().charAt(1)))*/
								button.setStyle("-fx-background-color:red");
						}

					}
					break;
				case '3':

					target.setStyle("-fx-background-color: red");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (fourController.highlightLengthFourVerticalTargetThree(button, target, '2', '4', '5'))
									
									/*(button.getId().toString().charAt(2) == '2')
									&& (button.getId().toString().charAt(1) == target.getId().toString().charAt(1))
									|| (button.getId().toString().charAt(2) == '4') && (button.getId().toString()
											.charAt(1) == target.getId().toString().charAt(1))
									|| (button.getId().toString().charAt(2) == '5') && (button.getId().toString()
											.charAt(1) == target.getId().toString().charAt(1)))*/
								button.setStyle("-fx-background-color:red");
						}

					}
					break;
				case '4':

					target.setStyle("-fx-background-color: red");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (fourController.highlightLengthFourVerticalTargetThree(button, target, '3', '5', '6'))
									
									/*(button.getId().toString().charAt(2) == '3')
									&& (button.getId().toString().charAt(1) == target.getId().toString().charAt(1))
									|| (button.getId().toString().charAt(2) == '5') && (button.getId().toString()
											.charAt(1) == target.getId().toString().charAt(1))
									|| (button.getId().toString().charAt(2) == '6') && (button.getId().toString()
											.charAt(1) == target.getId().toString().charAt(1)))*/
								button.setStyle("-fx-background-color:red");
						}

					}
					break;
				case '5':

					target.setStyle("-fx-background-color: red");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (fourController.highlightLengthFourVerticalTargetThree(button, target, '4', '6', '7'))
									
									/*(button.getId().toString().charAt(2) == '4')
									&& (button.getId().toString().charAt(1) == target.getId().toString().charAt(1))
									|| (button.getId().toString().charAt(2) == '6') && (button.getId().toString()
											.charAt(1) == target.getId().toString().charAt(1))
									|| (button.getId().toString().charAt(2) == '7') && (button.getId().toString()
											.charAt(1) == target.getId().toString().charAt(1)))*/
								button.setStyle("-fx-background-color:red");
						}

					}
					break;
				case '6':

					target.setStyle("-fx-background-color: red");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (fourController.highlightLengthFourVerticalTargetThree(button, target, '5', '7', '8'))
									
									/*(button.getId().toString().charAt(2) == '5')
									&& (button.getId().toString().charAt(1) == target.getId().toString().charAt(1))
									|| (button.getId().toString().charAt(2) == '7') && (button.getId().toString()
											.charAt(1) == target.getId().toString().charAt(1))
									|| (button.getId().toString().charAt(2) == '8') && (button.getId().toString()
											.charAt(1) == target.getId().toString().charAt(1)))*/
								button.setStyle("-fx-background-color:red");
						}

					}
					break;
				case '7':

					target.setStyle("-fx-background-color: red");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (fourController.highlightLengthFourVerticalTargetThree(button, target, '6', '8', '9'))
									
									/*(button.getId().toString().charAt(2) == '6')
									&& (button.getId().toString().charAt(1) == target.getId().toString().charAt(1))
									|| (button.getId().toString().charAt(2) == '8') && (button.getId().toString()
											.charAt(1) == target.getId().toString().charAt(1))
									|| (button.getId().toString().charAt(2) == '9') && (button.getId().toString()
											.charAt(1) == target.getId().toString().charAt(1)))*/
								button.setStyle("-fx-background-color:red");
						}

					}
					break;
				case '8':

					target.setStyle("-fx-background-color: red");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (fourController.highlightLengthFourVerticalTargetThreeSpecialCaseOne(button, target, '9', '7'))
									
									/*(button.getId().toString().charAt(2) == '9')
									&& (button.getId().toString().charAt(1) == target.getId().toString().charAt(1))
									|| (button.getId().toString().charAt(2) == '7') && (button.getId().toString()
											.charAt(1) == target.getId().toString().charAt(1)))*/
								button.setStyle("-fx-background-color:red");
						} else if (button.getId().toString().length() == 4) {
							if (fourController.highlightLengthFourVerticalTargetThreeSpecialCaseTwo(button, target, '1', '0'))
									
									/*(button.getId().toString().charAt(2) == '1'
									&& button.getId().toString().charAt(3) == '0')
									&& (button.getId().toString().charAt(1) == target.getId().toString()
											.charAt(1)))*/ 
								button.setStyle("-fx-background-color: red");
							
						}

					}
					break;
				case '9':

					target.setStyle("-fx-background-color: red");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (fourController.highlightLengthFourVerticalTargetThreeSpecialCaseOne(button, target, '8', '7'))
									
									/*(button.getId().toString().charAt(2) == '8')
									&& (button.getId().toString().charAt(1) == target.getId().toString().charAt(1))
									|| (button.getId().toString().charAt(2) == '7') && (button.getId().toString()
											.charAt(1) == target.getId().toString().charAt(1)))*/
								button.setStyle("-fx-background-color:red");
						} else if (button.getId().toString().length() == 4) {
							if (fourController.highlightLengthFourVerticalTargetThreeSpecialCaseTwo(button, target, '1', '0'))
									/*(button.getId().toString().charAt(2) == '1')
									&& (button.getId().toString().charAt(3) == '0')
									&& (button.getId().toString().charAt(1) == target.getId().toString().charAt(1)))*/
								//if (button.getId().toString().length() != 3) {
									button.setStyle("-fx-background-color:red");
								//}
						}

					}
					break;

				case '1':

					target.setStyle("-fx-background-color: red");
					if (target.getId().toString().length() == 3) {
						for (int i = 0; i < 100; i++) {
							Node button = buttonList.get(i);
							if (button.getId().toString().length() == 3) {
								if (fourController.highlightLengthFourVerticalTargetThreeSpecialCaseTen(button, target, '2', '3', '4'))
										
										/*(button.getId().toString().charAt(2) == '2')
										&& (button.getId().toString().charAt(1) == target.getId().toString()
												.charAt(1))
										|| (button.getId().toString().charAt(2) == '3') && (button.getId()
												.toString().charAt(1) == target.getId().toString().charAt(1))
										|| (button.getId().toString().charAt(2) == '4') && (button.getId()
												.toString().charAt(1) == target.getId().toString().charAt(1)))*/
									button.setStyle("-fx-background-color:red");
							}
						}
					}
					break;
				default:
					target.setStyle("-fx-background-color: green");
					break;
				}
			} else if (target.getId().toString().length() == 4) {
				switch (target.getId().toString().charAt(2)) {
				case '1':
					target.setStyle("-fx-background-color: red");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (fourController.highlightLengthFourVerticalTargetFour(button, target, '7', '8', '9'))
									
									/*(button.getId().toString().charAt(2) == '7')
									&& (button.getId().toString().charAt(1) == target.getId().toString().charAt(1))
									|| (button.getId().toString().charAt(2) == '8') && (button.getId().toString()
											.charAt(1) == target.getId().toString().charAt(1))
									|| (button.getId().toString().charAt(2) == '9') && (button.getId().toString()
											.charAt(1) == target.getId().toString().charAt(1)))*/
								button.setStyle("-fx-background-color:red");
						}
					}
					break;
				default:
					target.setStyle("-fx-background-color: green");
					break;
				}
			}
		}

	}

	
	/*
	 * Determines what buttons are highlighted when image  five is dragged horizontally
	 */
	private void calculateHorizontalFive(Button target, DragEvent event) {

		if (event.getGestureSource() != target && event.getDragboard().hasString()) {
//			System.out.println(target.getId());

			if (target.getId().toString().length() == 3) {
				switch (target.getId().toString().charAt(1)) {
				case 'A':

					target.setStyle("-fx-background-color: orange");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (fiveController.highlightLengthFiveHorizontalTargetThree(button, target, 'B', 'C', 'D', 'E'))
								button.setStyle("-fx-background-color:orange");
							

						}

					}
					break;
				case 'B':

					target.setStyle("-fx-background-color: orange");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (fiveController.highlightLengthFiveHorizontalTargetThree(button, target, 'A', 'C', 'D', 'E'))
								button.setStyle("-fx-background-color:orange");
							
						}

					}
					break;
				case 'C':

					target.setStyle("-fx-background-color: orange");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (fiveController.highlightLengthFiveHorizontalTargetThree(button, target, 'B', 'D', 'E', 'F'))
								button.setStyle("-fx-background-color:orange");
							
						}

					}
					break;
				case 'D':

					target.setStyle("-fx-background-color: orange");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (fiveController.highlightLengthFiveHorizontalTargetThree(button, target, 'C', 'E', 'F', 'G'))
								button.setStyle("-fx-background-color:orange");
							
						}

					}
					break;
				case 'E':

					target.setStyle("-fx-background-color: orange");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (fiveController.highlightLengthFiveHorizontalTargetThree(button, target, 'D', 'F', 'G', 'H'))
								button.setStyle("-fx-background-color: orange");
							
						}

					}
					break;
				case 'F':

					target.setStyle("-fx-background-color: orange");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (fiveController.highlightLengthFiveHorizontalTargetThree(button, target, 'E', 'G', 'H', 'I'))
								button.setStyle("-fx-background-color: orange");
							
						}

					}
					break;
				case 'G':

					target.setStyle("-fx-background-color: orange");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (fiveController.highlightLengthFiveHorizontalTargetThree(button, target, 'F', 'H', 'I', 'J'))
								button.setStyle("-fx-background-color: orange");
							
						}

					}
					break;
				case 'H':

					target.setStyle("-fx-background-color: orange");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (fiveController.highlightLengthFiveHorizontalTargetThree(button, target, 'F', 'G', 'I', 'J'))
								button.setStyle("-fx-background-color:orange");
							
						}

					}
					break;
				case 'I':

					target.setStyle("-fx-background-color: orange");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (fiveController.highlightLengthFiveHorizontalTargetThree(button, target, 'F', 'G', 'H', 'J'))
								button.setStyle("-fx-background-color:orange");
							
						}

					}

				case 'J':

					target.setStyle("-fx-background-color: orange");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (fiveController.highlightLengthFiveHorizontalTargetThree(button, target, 'F', 'G', 'H', 'I'))
								button.setStyle("-fx-background-color:orange");
							
						}

					}
					break;
				default:
					target.setStyle("-fx-background-color: green");
					break;
				}
			} else if (target.getId().toString().length() == 4) {
				switch (target.getId().toString().charAt(1)) {
				case 'A':

					target.setStyle("-fx-background-color: orange");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 4) {
							if (fiveController.highlightLengthFiveHorizontalTargetFour(button, target, 'B', 'C', 'D', 'E'))
								button.setStyle("-fx-background-color:orange");
							
						}

					}
					break;
				case 'B':

					target.setStyle("-fx-background-color: orange");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 4) {
							if (fiveController.highlightLengthFiveHorizontalTargetFour(button, target, 'A', 'C', 'D', 'E'))
								button.setStyle("-fx-background-color:orange");
							
						}

					}
					break;
				case 'C':

					target.setStyle("-fx-background-color: orange");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 4) {
							if (fiveController.highlightLengthFiveHorizontalTargetFour(button, target, 'B', 'D', 'E', 'F'))
								button.setStyle("-fx-background-color:orange");
							
						}
					}
					break;
				case 'D':

					target.setStyle("-fx-background-color: orange");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 4) {
							if (fiveController.highlightLengthFiveHorizontalTargetFour(button, target, 'C', 'E', 'F', 'G'))
								button.setStyle("-fx-background-color:orange");
							
						}
					}
					break;
				case 'E':

					target.setStyle("-fx-background-color: orange");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 4) {
							if (fiveController.highlightLengthFiveHorizontalTargetFour(button, target, 'D', 'F', 'G', 'H'))
								button.setStyle("-fx-background-color: orange");
							
						}
					}
					break;
				case 'F':

					target.setStyle("-fx-background-color: orange");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 4) {
							if (fiveController.highlightLengthFiveHorizontalTargetFour(button, target, 'E', 'G', 'H', 'I'))
								button.setStyle("-fx-background-color: orange");
							
						}

					}
					break;
				case 'G':

					target.setStyle("-fx-background-color: orange");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 4) {
							if (fiveController.highlightLengthFiveHorizontalTargetFour(button, target, 'F', 'H', 'I', 'J'))
								button.setStyle("-fx-background-color:orange");
							
						}

					}
					break;
				case 'H':

					target.setStyle("-fx-background-color: orange");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 4) {
							if (fiveController.highlightLengthFiveHorizontalTargetFour(button, target, 'F', 'G', 'I', 'J'))
								button.setStyle("-fx-background-color:orange");
							
						}

					}
					break;
				case 'I':

					target.setStyle("-fx-background-color: orange");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 4) {
							if (fiveController.highlightLengthFiveHorizontalTargetFour(button, target, 'F', 'G', 'H', 'J'))
								button.setStyle("-fx-background-color:orange");
							
						}

					}
					break;
				case 'J':

					target.setStyle("-fx-background-color: orange");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 4) {
							if (fiveController.highlightLengthFiveHorizontalTargetFour(button, target, 'F', 'G', 'H', 'I'))
								button.setStyle("-fx-background-color:orange");
							
						}

					}
					break;
				default:
					break;
				}
			}
		}

	}
	/*
	 * Determines what buttons are highlighted when image  five is dragged vertically
	 */
	private void calculateVerticalFive(Button target, DragEvent event) {

		if (event.getGestureSource() != target && event.getDragboard().hasString()) {
//			System.out.println(target.getId());

			if (target.getId().toString().length() == 3) {
				switch (target.getId().toString().charAt(2)) {
				case '2':

					target.setStyle("-fx-background-color: orange");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (fiveController.hightlightLengthFiveVerticalTargetThree(button, target, '1', '3', '4', '5'))
								button.setStyle("-fx-background-color:orange");
						}

					}
					break;
				case '3':

					target.setStyle("-fx-background-color: orange");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (fiveController.hightlightLengthFiveVerticalTargetThree(button, target, '2', '4', '5', '6'))
								button.setStyle("-fx-background-color:orange");
						}

					}
					break;
				case '4':

					target.setStyle("-fx-background-color: orange");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (fiveController.hightlightLengthFiveVerticalTargetThree(button, target, '3', '5', '6', '7'))
								button.setStyle("-fx-background-color: orange");
						}

					}
					break;
				case '5':

					target.setStyle("-fx-background-color: orange");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (fiveController.hightlightLengthFiveVerticalTargetThree(button, target, '4', '6', '7', '8'))
								button.setStyle("-fx-background-color:orange");
						}

					}
					break;
				case '6':

					target.setStyle("-fx-background-color: orange");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (fiveController.hightlightLengthFiveVerticalTargetThree(button, target, '5', '7', '8', '9'))
								button.setStyle("-fx-background-color:orange");
						}

					}
					break;
				case '7':

					target.setStyle("-fx-background-color: orange");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (fiveController.hightlightLengthFiveVerticalTargetThree(button, target, '5', '6', '8', '9'))
								button.setStyle("-fx-background-color:orange");
						}

					}
					break;
				case '8':

					target.setStyle("-fx-background-color: orange");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (fiveController.highlightLengthFiveVerticalTargetThreeSpecialCaseThree(button, target, '6', '7', '9'))
								button.setStyle("-fx-background-color:orange");
						} else if (button.getId().toString().length() == 4) {
							if (fiveController.highlightLengthFiveVerticalTargetThreeSpecialCaseFour(button, target, '1', '0'))
								button.setStyle("-fx-background-color:orange");
							
						}

					}
					break;
				case '9':

					target.setStyle("-fx-background-color: orange");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (fiveController.highlightLengthFiveVerticalTargetThreeSpecialCaseThree(button, target, '6', '7', '8'))
								button.setStyle("-fx-background-color:orange");
						} else if (button.getId().toString().length() == 4) {
							if (fiveController.highlightLengthFiveVerticalTargetThreeSpecialCaseFour(button, target, '1', '0'))
								if (button.getId().toString().length() == 4) {
									button.setStyle("-fx-background-color:orange");
								}
						}

					}
					break;

				case '1':

					target.setStyle("-fx-background-color: orange");
					if (target.getId().toString().length() == 3) {
						for (int i = 0; i < 100; i++) {
							Node button = buttonList.get(i);
							if (button.getId().toString().length() == 3) {
								if (fiveController.highlightLengthFiveVerticalTargetThreeSpecialCaseOne(button, target, '2', '3', '4', '5'))
									button.setStyle("-fx-background-color:orange");
							}
						}
					}
					break;
				default:
					target.setStyle("-fx-background-color: green");
					break;
				}
			} else if (target.getId().toString().length() == 4) {
				switch (target.getId().toString().charAt(2)) {
				case '1':

					target.setStyle("-fx-background-color: orange");
					if (target.getId().toString().length() == 4) {
						for (int i = 0; i < 100; i++) {
							Node button = buttonList.get(i);
							if (button.getId().toString().length() == 3) {
								if (fiveController.highlightLengthFiveVerticalTargetFour(button, target, '6', '7', '8', '9'))
									button.setStyle("-fx-background-color:orange");
							}
						}
					}
					break;
				default:
					break;
				}
			}
		}

	}

	
	/*
	 * Calls functions to highlight buttons for image one
	 */
	private void highlightImageOne(Button target, DragEvent event) {

		if (orientation == "Horizontal") {
			calculateHorizontalOne(target, event);
		} else if (orientation == "Vertical") {
			calculateVerticalOne(target, event);
		}
		event.consume();
	}
	/*
	 * Calls functions to highlight buttons for image two
	 */
	private void highlightImageTwo(Button target, DragEvent event) {
		if (orientation == "Horizontal") {
			calculateHorizontalTwo(target, event);
		} else if (orientation == "Vertical") {
			calculateVerticalTwo(target, event);
		}
	}
	/*
	 * Calls functions to highlight buttons for image three
	 */
	private void highlightImageThree(Button target, DragEvent event) {
		if (orientation == "Horizontal") {
			calculateHorizontalThree(target, event);
		} else if (orientation == "Vertical") {
			calculateVerticalThree(target, event);
		}
	}
	/*
	 * Calls functions to highlight buttons for image four
	 */
	private void highlightImageFour(Button target, DragEvent event) {
		if (orientation == "Horizontal") {
			calculateHorizontalFour(target, event);
		} else if (orientation == "Vertical") {
			calculateVerticalFour(target, event);
		}
	}
	/*
	 * Calls functions to highlight buttons for image five
	 */
	private void highlightImageFive(Button target, DragEvent event) {
		if (orientation == "Horizontal") {
			calculateHorizontalFive(target, event);
		} else if (orientation == "Vertical") {
			calculateVerticalFive(target, event);
		}
	}

	
	/*
	 * Determines what buttons are unhighlighted when image one is dragged horizontally
	 */
	private void unHighlightHorizontalOne(Button target, DragEvent event) {

		if (target.getId().toString().length() == 3) {
			switch (target.getId().toString().charAt(1)) {
			case 'A':

				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (twoController.unhighlightLengthTwoHorizontalTargetThree(button, target, 'B'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'B':

				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (twoController.unhighlightLengthTwoHorizontalTargetThree(button, target, 'A'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'C':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (twoController.unhighlightLengthTwoHorizontalTargetThree(button, target, 'B'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'D':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (twoController.unhighlightLengthTwoHorizontalTargetThree(button, target, 'C'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'E':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (twoController.unhighlightLengthTwoHorizontalTargetThree(button, target, 'D'))
							button.setStyle("-fx-background-color:blue");
					}
				}
				break;
			case 'F':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (twoController.unhighlightLengthTwoHorizontalTargetThree(button, target, 'E'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'G':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (twoController.unhighlightLengthTwoHorizontalTargetThree(button, target, 'F'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'H':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (twoController.unhighlightLengthTwoHorizontalTargetThree(button, target, 'G'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'I':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (twoController.unhighlightLengthTwoHorizontalTargetThree(button, target, 'H'))
							button.setStyle("-fx-background-color:blue");
						
					}

				}
				break;

			case 'J':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (twoController.unhighlightLengthTwoHorizontalTargetThree(button, target, 'I'))
							button.setStyle("-fx-background-color:blue");
						
					}
				}
				break;

			default:
				target.setStyle("-fx-background-color: blue");
				break;
			}
		} else if (target.getId().toString().length() == 4) {
			switch (target.getId().toString().charAt(1)) {
			case 'A':

				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 4) {
						if (twoController.unhighlightLengthTwoHorizontalTargetFour(button, target, 'B'))
							button.setStyle("-fx-background-color:blue");
					}

				}
			case 'B':

				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 4) {
						if (twoController.unhighlightLengthTwoHorizontalTargetFour(button, target, 'A'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'C':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 4) {
						if (twoController.unhighlightLengthTwoHorizontalTargetFour(button, target, 'B'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'D':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 4) {
						if (twoController.unhighlightLengthTwoHorizontalTargetFour(button, target, 'C'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'E':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 4) {
						if (twoController.unhighlightLengthTwoHorizontalTargetFour(button, target, 'D'))
							button.setStyle("-fx-background-color:blue");
					}
				}
				break;
			case 'F':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 4) {
						if (twoController.unhighlightLengthTwoHorizontalTargetFour(button, target, 'E'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'G':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 4) {
						if (twoController.unhighlightLengthTwoHorizontalTargetFour(button, target, 'F'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'H':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 4) {
						if (twoController.unhighlightLengthTwoHorizontalTargetFour(button, target, 'G'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'I':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 4) {
						if (twoController.unhighlightLengthTwoHorizontalTargetFourSpecialCaseI(button, target, 'H', 'J'))
							button.setStyle("-fx-background-color:blue");
						
					}

				}
				break;
			case 'J':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 4) {
						if (twoController.unhighlightLengthTwoHorizontalTargetFourSpecialCaseJ(button, target, 'I'))
							button.setStyle("-fx-background-color:blue");
						
					}

				}
				break;

			default:
				target.setStyle("-fx-background-color: blue");
				break;
			}
		}

	}
	/*
	 * Determines what buttons are unhighlighted when image  one is dragged vertically
	 */
	private void unHighlightVerticalOne(Button target, DragEvent event) {

		if (target.getId().toString().length() == 3) {
			switch (target.getId().toString().charAt(2)) {
			case '1':

				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (twoController.unhighlightLengthTwoVertical(button, target, '2'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case '2':

				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (twoController.unhighlightLengthTwoVertical(button, target, '1'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case '3':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (twoController.unhighlightLengthTwoVertical(button, target, '2'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case '4':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (twoController.unhighlightLengthTwoVertical(button, target, '3'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case '5':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (twoController.unhighlightLengthTwoVertical(button, target, '4'))
							button.setStyle("-fx-background-color:blue");
					}
				}
				break;
			case '6':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (twoController.unhighlightLengthTwoVertical(button, target, '5'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case '7':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (twoController.unhighlightLengthTwoVertical(button, target, '6'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case '8':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (twoController.unhighlightLengthTwoVertical(button, target, '7'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case '9':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (twoController.unhighlightLengthTwoVertical(button, target, '8'))
							button.setStyle("-fx-background-color: blue");

					}
				}
				break;

			default:
				target.setStyle("-fx-background-color: blue");
				break;
			}
		} else if (target.getId().toString().length() == 4) {

			switch (target.getId().toString().charAt(2)) {
			case '1':
				if (target.getId().toString().length() == 4) {
					target.setStyle("-fx-background-color:blue");
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (twoController.unhighlightLengthTwoVertical(button, target, '9'))
							button.setStyle("-fx-background-color:blue");
					}
				}
				break;

			default:
				target.setStyle("-fx-background-color: green");
				break;
			}

		}

	}
	/*
	 * Calls functions for unhighlighting buttons when image one is dragged
	 */
	private void unHighlightImageOne(Button target, DragEvent event) {
		if (orientation == "Horizontal") {
			unHighlightHorizontalOne(target, event);
		} else if (orientation == "Vertical") {
			unHighlightVerticalOne(target, event);
		}
	}

	
	/*
	 * Determines what buttons are unhighlighted when image  two is dragged horizontally
	 */
	private void unHighlightHorizontalTwo(Button target, DragEvent event) {

		if (target.getId().toString().length() == 3) {
			switch (target.getId().toString().charAt(1)) {
			case 'A':

				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (threeController.unhighlightLengthThreeHorizontalTargetThree(button, target, 'B', 'C'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'B':

				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (threeController.unhighlightLengthThreeHorizontalTargetThree(button, target, 'A', 'C'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'C':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (threeController.unhighlightLengthThreeHorizontalTargetThree(button, target, 'B', 'D'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'D':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (threeController.unhighlightLengthThreeHorizontalTargetThree(button, target, 'C', 'E'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'E':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (threeController.unhighlightLengthThreeHorizontalTargetThree(button, target, 'D', 'F'))
							button.setStyle("-fx-background-color:blue");
					}
				}
				break;
			case 'F':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (threeController.unhighlightLengthThreeHorizontalTargetThree(button, target, 'E', 'G'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'G':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (threeController.unhighlightLengthThreeHorizontalTargetThree(button, target, 'F', 'H'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'H':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (threeController.unhighlightLengthThreeHorizontalTargetThree(button, target, 'G', 'I'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'I':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (threeController.unhighlightLengthThreeHorizontalTargetThree(button, target, 'H', 'J'))
							button.setStyle("-fx-background-color:blue");
						
					}

				}
				break;
			case 'J':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (threeController.unhighlightLengthThreeHorizontalTargetThree(button, target, 'H', 'I'))
							button.setStyle("-fx-background-color:blue");
						
					}

				}
				break;
			default:
				target.setStyle("-fx-background-color: blue");
				break;
			}
		} else if (target.getId().toString().length() == 4) {
			switch (target.getId().toString().charAt(1)) {
			case 'A':

				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 4) {
						if (threeController.unhighlightLengthThreeHorizontalTargetFour(button, target, 'B', 'C'))
							button.setStyle("-fx-background-color:blue");
					}

				}
			case 'B':

				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 4) {
						if (threeController.unhighlightLengthThreeHorizontalTargetFour(button, target, 'A', 'C'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'C':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 4) {
						if (threeController.unhighlightLengthThreeHorizontalTargetFour(button, target, 'B', 'D'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'D':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 4) {
						if (threeController.unhighlightLengthThreeHorizontalTargetFour(button, target, 'C', 'E'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'E':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 4) {
						if (threeController.unhighlightLengthThreeHorizontalTargetFour(button, target, 'D', 'F'))
							button.setStyle("-fx-background-color:blue");
					}
				}
				break;
			case 'F':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 4) {
						if (threeController.unhighlightLengthThreeHorizontalTargetFour(button, target, 'E', 'G'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'G':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 4) {
						if (threeController.unhighlightLengthThreeHorizontalTargetFour(button, target, 'F', 'H'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'H':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 4) {
						if (threeController.unhighlightLengthThreeHorizontalTargetFour(button, target, 'G', 'I'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'I':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 4) {
						if (threeController.unhighlightLengthThreeHorizontalTargetFour(button, target, 'H', 'J'))
							button.setStyle("-fx-background-color:blue");
						
					}

				}
				break;
			case 'J':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 4) {
						if (threeController.unhighlightLengthThreeHorizontalTargetFour(button, target, 'H', 'I'))
							button.setStyle("-fx-background-color:blue");
						
					}

				}
				break;
			default:
				target.setStyle("-fx-background-color: blue");
				break;
			}
		}

	}
	/*
	 * Determines what buttons are unhighlighted when image  two is dragged vertically
	 */
	private void unHighlightVerticalTwo(Button target, DragEvent event) {

		if (target.getId().toString().length() == 3) {
			switch (target.getId().toString().charAt(2)) {

			case '1':

				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (threeController.unhighlightLengthThreeVerticalTargetThree(button, target, '2', '3'))
							button.setStyle("-fx-background-color:blue");
					}

				}
			case '2':

				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (threeController.unhighlightLengthThreeVerticalTargetThree(button, target, '1', '3'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case '3':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (threeController.unhighlightLengthThreeVerticalTargetThree(button, target, '2', '4'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case '4':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (threeController.unhighlightLengthThreeVerticalTargetThree(button, target, '3', '5'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case '5':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (threeController.unhighlightLengthThreeVerticalTargetThree(button, target, '4', '6'))
							button.setStyle("-fx-background-color:blue");
					}
				}
				break;
			case '6':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (threeController.unhighlightLengthThreeVerticalTargetThree(button, target, '5', '7'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case '7':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (threeController.unhighlightLengthThreeVerticalTargetThree(button, target, '6', '8'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case '8':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (threeController.unhighlightLengthThreeVerticalTargetThree(button, target, '7', '9'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case '9':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (threeController.unhighlightLengthThreeVerticalTargetThreeSpecialCaseNine(button, target, '8'))
							button.setStyle("-fx-background-color:blue");
					} else if (button.getId().toString().length() == 4) {
						if (threeController.unhighlightLengthThreeVerticalTargetThreeSpecialCaseTen(button, target, '1', '0'))
							button.setStyle("-fx-background-color:blue");

					}
				}
				break;

			default:
				target.setStyle("-fx-background-color: blue");
				break;
			}
		} else if (target.getId().toString().length() == 4) {
			switch (target.getId().toString().charAt(2)) {

			case '1':

				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (threeController.unhighlightLengthThreeVerticalTargetFour(button, target, '8', '9'))
							button.setStyle("-fx-background-color:blue");
					}

				}

			default:
				target.setStyle("-fx-background-color: blue");
				break;
			}
		}

	}
	/*
	 * Calls functions for unhighlighting buttons when image two is dragged
	 */
	private void unHighlightImageTwo(Button target, DragEvent event) {
		if (orientation == "Horizontal") {
			unHighlightHorizontalTwo(target, event);
		} else if (orientation == "Vertical") {
			unHighlightVerticalTwo(target, event);
		}
	}

	/*
	 * Determines what buttons are unhighlighted when image  three is dragged vertically
	 */
	private void unHighlightHorizontalThree(Button target, DragEvent event) {
		if (target.getId().toString().length() == 3) {
			switch (target.getId().toString().charAt(1)) {
			case 'A':

				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (threeController.unhighlightLengthThreeHorizontalTargetThree(button, target, 'B', 'C'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'B':

				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (threeController.unhighlightLengthThreeHorizontalTargetThree(button, target, 'A', 'C'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'C':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (threeController.unhighlightLengthThreeHorizontalTargetThree(button, target, 'B', 'D'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'D':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (threeController.unhighlightLengthThreeHorizontalTargetThree(button, target, 'C', 'E'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'E':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (threeController.unhighlightLengthThreeHorizontalTargetThree(button, target, 'D', 'F'))
							button.setStyle("-fx-background-color:blue");
					}
				}
				break;
			case 'F':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (threeController.unhighlightLengthThreeHorizontalTargetThree(button, target, 'E', 'G'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'G':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (threeController.unhighlightLengthThreeHorizontalTargetThree(button, target, 'F', 'H'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'H':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (threeController.unhighlightLengthThreeHorizontalTargetThree(button, target, 'G', 'I'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'I':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (threeController.unhighlightLengthThreeHorizontalTargetThree(button, target, 'H', 'J'))
							button.setStyle("-fx-background-color:blue");
						
					}

				}
				break;
			case 'J':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (threeController.unhighlightLengthThreeHorizontalTargetThree(button, target, 'H', 'I'))
							button.setStyle("-fx-background-color:blue");
						
					}

				}
				break;
			default:
				target.setStyle("-fx-background-color: blue");
				break;
			}
		} else if (target.getId().toString().length() == 4) {
			switch (target.getId().toString().charAt(1)) {
			case 'A':

				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 4) {
						if (threeController.unhighlightLengthThreeHorizontalTargetFour(button, target, 'B', 'C'))
							button.setStyle("-fx-background-color:blue");
					}

				}
			case 'B':

				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 4) {
						if (threeController.unhighlightLengthThreeHorizontalTargetFour(button, target, 'A', 'C'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'C':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 4) {
						if (threeController.unhighlightLengthThreeHorizontalTargetFour(button, target, 'B', 'D'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'D':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 4) {
						if (threeController.unhighlightLengthThreeHorizontalTargetFour(button, target, 'C', 'E'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'E':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 4) {
						if (threeController.unhighlightLengthThreeHorizontalTargetFour(button, target, 'D', 'F'))
							button.setStyle("-fx-background-color:blue");
					}
				}
				break;
			case 'F':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 4) {
						if (threeController.unhighlightLengthThreeHorizontalTargetFour(button, target, 'E', 'G'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'G':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 4) {
						if (threeController.unhighlightLengthThreeHorizontalTargetFour(button, target, 'F', 'H'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'H':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 4) {
						if (threeController.unhighlightLengthThreeHorizontalTargetFour(button, target, 'G', 'I'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'I':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 4) {
						if (threeController.unhighlightLengthThreeHorizontalTargetFour(button, target, 'H', 'J'))
							button.setStyle("-fx-background-color:blue");
						
					}

				}
				break;
			case 'J':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 4) {
						if (threeController.unhighlightLengthThreeHorizontalTargetFour(button, target, 'H', 'I'))
							button.setStyle("-fx-background-color:blue");
						
					}

				}
				break;
			default:
				target.setStyle("-fx-background-color: blue");
				break;
			}
		}

	}
	/*
	 * Calls functions for unhighlighting buttons when image three is dragged
	 */
	private void unHighlightVerticalThree(Button target, DragEvent event) {

		if (target.getId().toString().length() == 3) {
			switch (target.getId().toString().charAt(2)) {

			case '1':

				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (threeController.unhighlightLengthThreeVerticalTargetThree(button, target, '2', '3'))
							button.setStyle("-fx-background-color:blue");
					}

				}
			case '2':

				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (threeController.unhighlightLengthThreeVerticalTargetThree(button, target, '1', '3'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case '3':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (threeController.unhighlightLengthThreeVerticalTargetThree(button, target, '2', '4'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case '4':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (threeController.unhighlightLengthThreeVerticalTargetThree(button, target, '3', '5'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case '5':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (threeController.unhighlightLengthThreeVerticalTargetThree(button, target, '4', '6'))
							button.setStyle("-fx-background-color:blue");
					}
				}
				break;
			case '6':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (threeController.unhighlightLengthThreeVerticalTargetThree(button, target, '5', '7'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case '7':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (threeController.unhighlightLengthThreeVerticalTargetThree(button, target, '6', '8'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case '8':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (threeController.unhighlightLengthThreeVerticalTargetThree(button, target, '7', '9'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case '9':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (threeController.unhighlightLengthThreeVerticalTargetThreeSpecialCaseNine(button, target, '8'))
							button.setStyle("-fx-background-color:blue");
					} else if (button.getId().toString().length() == 4) {
						if (threeController.unhighlightLengthThreeVerticalTargetThreeSpecialCaseTen(button, target, '1', '0'))
							button.setStyle("-fx-background-color:blue");

					}
				}
				break;

			default:
				target.setStyle("-fx-background-color: blue");
				break;
			}
		} else if (target.getId().toString().length() == 4) {
			switch (target.getId().toString().charAt(2)) {

			case '1':

				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (threeController.unhighlightLengthThreeVerticalTargetFour(button, target, '8', '9'))
							button.setStyle("-fx-background-color:blue");
					}

				}

			default:
				target.setStyle("-fx-background-color: blue");
				break;
			}
		}

	}
	/*
	 * Determines what buttons are unhighlighted when image  four is dragged vertically
	 */
	private void unHighlightImageThree(Button target, DragEvent event) {
		if (orientation == "Horizontal") {
			unHighlightHorizontalThree(target, event);
		} else if (orientation == "Vertical") {
			unHighlightVerticalThree(target, event);
		}
	}

	
	/*
	 * Determines what buttons are unhighlighted when image  four is dragged vertically
	 */
	private void unHightlightVerticalFour(Button target, DragEvent event) {

		if (target.getId().toString().length() == 3) {
			switch (target.getId().toString().charAt(2)) {
			case '2':

				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (fourController.unhighlightLengthFourVerticalTargetThree(button, target, '1', '3', '4'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case '3':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (fourController.unhighlightLengthFourVerticalTargetThree(button, target, '2', '4', '5'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case '4':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (fourController.unhighlightLengthFourVerticalTargetThree(button, target, '3', '5', '6'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case '5':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (fourController.unhighlightLengthFourVerticalTargetThree(button, target, '4', '6', '7'))
							button.setStyle("-fx-background-color:blue");
					}
				}
				break;
			case '6':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (fourController.unhighlightLengthFourVerticalTargetThree(button, target, '5', '7', '8'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case '7':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (fourController.unhighlightLengthFourVerticalTargetThree(button, target, '6', '8', '9'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case '8':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (fourController.unhighlightLengthFourVerticalTargetThreeSepcialCaseOne(button, target, '7', '9'))
							button.setStyle("-fx-background-color:blue");
					} else if (button.getId().toString().length() == 4) {
						if (fourController.unhighlightLengthFourVerticalTargetThreeSpecialCaseTwo(button, target, '1', '0'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case '9':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (fourController.unhighlightLengthFourVerticalTargetThreeSepcialCaseOne(button, target, '7', '8'))
							button.setStyle("-fx-background-color: blue");

					} else if (button.getId().toString().length() == 4) {
						if (fourController.unhighlightLengthFourVerticalTargetThreeSpecialCaseTwo(button, target, '1', '0'))
							button.setStyle("-fx-background-color:blue");

					}
				}
				break;
			case '1':

				target.setStyle("-fx-background-color: blue");
				if (target.getId().toString().length() == 3) {
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (fourController.unhighlightLengthFourVerticalTargetThree(button, target, '2', '3', '4'))
								button.setStyle("-fx-background-color:blue");
						}
					}
				}

				break;
			default:
				target.setStyle("-fx-background-color: blue");
				break;
			}
		} else if (target.getId().toString().length() == 4) {
			switch (target.getId().toString().charAt(2)) {
			case '1':

				target.setStyle("-fx-background-color: blue");
				if (target.getId().toString().length() == 4) {
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (fourController.unhighlightLengthFourVerticalTargetThree(button, target, '7', '8', '9'))
								button.setStyle("-fx-background-color:blue");
						}
					}
				}

				break;
			default:
				target.setStyle("-fx-background-color: blue");
				break;
			}
		}

	}
	/*
	 * Determines what buttons are unhighlighted when image  four is dragged horizontally
	 */
	private void unHightlightHorizontalFour(Button target, DragEvent event) {

		if (target.getId().toString().length() == 3) {
			switch (target.getId().toString().charAt(1)) {
			case 'A':

				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (fourController.unhighlightLengthFourHorizontalTargetThree(button, target, 'B', 'C', 'D'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'B':

				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (fourController.unhighlightLengthFourHorizontalTargetThree(button, target, 'A', 'C', 'D'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'C':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (fourController.unhighlightLengthFourHorizontalTargetThree(button, target, 'B', 'D', 'E'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'D':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (fourController.unhighlightLengthFourHorizontalTargetThree(button, target, 'C', 'E', 'F'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'E':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (fourController.unhighlightLengthFourHorizontalTargetThree(button, target, 'D', 'F', 'G'))
							button.setStyle("-fx-background-color:blue");
					}
				}
				break;
			case 'F':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (fourController.unhighlightLengthFourHorizontalTargetThree(button, target, 'E', 'G', 'H'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'G':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (fourController.unhighlightLengthFourHorizontalTargetThree(button, target, 'F', 'H', 'I'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'H':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (fourController.unhighlightLengthFourHorizontalTargetThree(button, target, 'G', 'I', 'J'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'I':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (fourController.unhighlightLengthFourHorizontalTargetThree(button, target, 'G', 'H', 'J'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'J':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (fourController.unhighlightLengthFourHorizontalTargetThree(button, target, 'G', 'H', 'I'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;

			default:
				target.setStyle("-fx-background-color: blue");
				break;
			}
		} else if (target.getId().toString().length() == 4) {
			switch (target.getId().toString().charAt(1)) {
			case 'A':

				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 4) {
						if (fourController.unhighlightLengthFourHorizontalTargetFourCaseA(button, target, 'B', 'C', 'D'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'B':

				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 4) {
						if (fourController.unhighlightLengthFourHorizontalTargetFour(button, target, 'A', 'C', 'D'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'C':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 4) {
						if (fourController.unhighlightLengthFourHorizontalTargetFour(button, target, 'B', 'D', 'E'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'D':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 4) {
						if (fourController.unhighlightLengthFourHorizontalTargetFour(button, target, 'C', 'E', 'F'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'E':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 4) {
						if (fourController.unhighlightLengthFourHorizontalTargetFour(button, target, 'D', 'F', 'G'))
							button.setStyle("-fx-background-color:blue");
					}
				}
				break;
			case 'F':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 4) {
						if (fourController.unhighlightLengthFourHorizontalTargetFour(button, target, 'E', 'G', 'H'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'G':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 4) {
						if (fourController.unhighlightLengthFourHorizontalTargetFour(button, target, 'F', 'H', 'I'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'H':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 4) {
						if (fourController.unhighlightLengthFourHorizontalTargetFour(button, target, 'G', 'I', 'J'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'I':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 4) {
						if (fourController.unhighlightLengthFourHorizontalTargetFour(button, target, 'G', 'H', 'J'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'J':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 4) {
						if (fourController.unhighlightLengthFourHorizontalTargetFour(button, target, 'G', 'H', 'I'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			default:
				target.setStyle("-fx-background-color: blue");
				break;
			}
		}

	}
	/*
	 * Calls functions for unhighlighting buttons when image four is dragged
	 */
	private void unHighlightImageFour(Button target, DragEvent event) {
		if (orientation == "Horizontal") {
			unHightlightHorizontalFour(target, event);
		} else if (orientation == "Vertical") {
			unHightlightVerticalFour(target, event);
		}
	}

	
	/*
	 * Determines what buttons are unhighlighted when image  five is dragged horizontally
	 */
	private void unHighlightHorizontalFive(Button target, DragEvent event) {

		if (target.getId().toString().length() == 3) {
			switch (target.getId().toString().charAt(1)) {
			case 'A':

				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (fiveController.unhighlightLengthFiveHorizontalTargetThree(button, target, 'B', 'C', 'D', 'E'))
							button.setStyle("-fx-background-color: blue");
					}

				}
				break;
			case 'B':

				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (fiveController.unhighlightLengthFiveHorizontalTargetThree(button, target, 'A', 'C', 'D', 'E'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'C':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (fiveController.unhighlightLengthFiveHorizontalTargetThree(button, target, 'B', 'D', 'E', 'F'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'D':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (fiveController.unhighlightLengthFiveHorizontalTargetThree(button, target, 'C', 'E', 'F', 'G'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'E':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (fiveController.unhighlightLengthFiveHorizontalTargetThree(button, target, 'D', 'F', 'G', 'H'))
							button.setStyle("-fx-background-color:blue");
					}
				}
				break;
			case 'F':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (fiveController.unhighlightLengthFiveHorizontalTargetThree(button, target, 'E', 'G', 'H', 'I'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'G':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (fiveController.unhighlightLengthFiveHorizontalTargetThree(button, target, 'F', 'H', 'I', 'J'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'H':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (fiveController.unhighlightLengthFiveHorizontalTargetThree(button, target, 'F', 'G', 'I', 'J'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'I':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (fiveController.unhighlightLengthFiveHorizontalTargetThree(button, target, 'F', 'G', 'H', 'J'))
							button.setStyle("-fx-background-color:blue");
						
					}

				}
				break;
			case 'J':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (fiveController.unhighlightLengthFiveHorizontalTargetThree(button, target, 'F', 'G', 'H', 'I'))
							button.setStyle("-fx-background-color:blue");
						
					}

				}
				break;

			default:
				target.setStyle("-fx-background-color: blue");
				break;
			}
		} else if (target.getId().toString().length() == 4) {
			switch (target.getId().toString().charAt(1)) {
			case 'A':

				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 4) {
						if (fiveController.unhighlightLengthFiveHorizontalTargetFourSpecialCaseA(button, target, 'B', 'C', 'D', 'E'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'B':

				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 4) {
						if (fiveController.unhighlightLengthFiveHorizontalTargetFour(button, target, 'A', 'C', 'D', 'E'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'C':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 4) {
						if (fiveController.unhighlightLengthFiveHorizontalTargetFour(button, target, 'B', 'D', 'E', 'F'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'D':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 4) {
						if (fiveController.unhighlightLengthFiveHorizontalTargetFour(button, target, 'C', 'E', 'F', 'G'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'E':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 4) {
						if (fiveController.unhighlightLengthFiveHorizontalTargetFour(button, target, 'D', 'F', 'G', 'H'))
							button.setStyle("-fx-background-color:blue");
					}
				}
				break;
			case 'F':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 4) {
						if (fiveController.unhighlightLengthFiveHorizontalTargetFour(button, target, 'E', 'G', 'H', 'I'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'G':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 4) {
						if (fiveController.unhighlightLengthFiveHorizontalTargetFour(button, target, 'F', 'H', 'I', 'J'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'H':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 4) {
						if (fiveController.unhighlightLengthFiveHorizontalTargetFour(button, target, 'F', 'G', 'I', 'J'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'I':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 4) {
						if (fiveController.unhighlightLengthFiveHorizontalTargetFour(button, target, 'F', 'G', 'H', 'J'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case 'J':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 4) {
						if (fiveController.unhighlightLengthFiveHorizontalTargetFour(button, target, 'F', 'G', 'H', 'I'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			default:
				target.setStyle("-fx-background-color: blue");
				break;
			}
		}

	}
	/*
	 * Determines what buttons are unhighlighted when image  five is dragged vertically
	 */
	private void unHighlightVerticalFive(Button target, DragEvent event) {

		if (target.getId().toString().length() == 3) {
			switch (target.getId().toString().charAt(2)) {
			case '1':

				target.setStyle("-fx-background-color: blue");
				if (target.getId().toString().length() == 3) {
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (fiveController.unhighlightLengthFiveVerticalTargetThree(button, target, '2', '3', '4', '5'))
								button.setStyle("-fx-background-color:blue");
						}
					}
				}
				break;
			case '2':

				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (fiveController.unhighlightLengthFiveVerticalTargetThree(button, target, '1', '3', '4', '5'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case '3':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (fiveController.unhighlightLengthFiveVerticalTargetThree(button, target, '2', '4', '5', '6'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case '4':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (fiveController.unhighlightLengthFiveVerticalTargetThree(button, target, '3', '5', '6', '7'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case '5':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (fiveController.unhighlightLengthFiveVerticalTargetThree(button, target, '4', '6', '7', '8'))
							button.setStyle("-fx-background-color:blue");
					}
				}
				break;
			case '6':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (fiveController.unhighlightLengthFiveVerticalTargetThree(button, target, '5', '7', '8', '9'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case '7':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (fiveController.unhighlightLengthFiveVerticalTargetThree(button, target, '5', '6', '8', '9'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case '8':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (fiveController.unhighlightLengthFiveVerticalSpecialCaseNonTen(button, target, '6', '7', '9'))								
							button.setStyle("-fx-background-color:blue");
					} else if (button.getId().toString().length() == 4) {
						if (fiveController.unhighlightLengthFiveVerticalSpecialCaseTen(button, target, '1', '0'))
							button.setStyle("-fx-background-color:blue");
					}

				}
				break;
			case '9':
				target.setStyle("-fx-background-color: blue");
				for (int i = 0; i < 100; i++) {
					Node button = buttonList.get(i);
					if (button.getId().toString().length() == 3) {
						if (fiveController.unhighlightLengthFiveVerticalSpecialCaseNonTen(button, target, '6', '7', '8'))
							button.setStyle("-fx-background-color: blue");

					} else if (button.getId().toString().length() == 4) {
						if (fiveController.unhighlightLengthFiveVerticalSpecialCaseTen(button, target, '1', '0'))
							button.setStyle("-fx-background-color:blue");

					}
				}
				break;
			default:
				target.setStyle("-fx-background-color: blue");
				break;
			}
		} else if (target.getId().toString().length() == 4) {
			switch (target.getId().toString().charAt(2)) {
			case '1':

				target.setStyle("-fx-background-color: blue");
				if (target.getId().toString().length() == 4) {
					for (int i = 0; i < 100; i++) {
						Node button = buttonList.get(i);
						if (button.getId().toString().length() == 3) {
							if (fiveController.unhighlightLengthFiveVerticalTargetFour(button, target, '6', '7', '8' , '9'))
								button.setStyle("-fx-background-color:blue");
						}
					}
				}
				break;
			default:
				target.setStyle("-fx-background-color: blue");
				break;
			}
		}

	}
	/*
	 * Calls functions for unhighlighting buttons when image five is dragged
	 */
	private void unHighlightImageFive(Button target, DragEvent event) {
		if (orientation == "Horizontal") {
			unHighlightHorizontalFive(target, event);
		} else if (orientation == "Vertical") {
			unHighlightVerticalFive(target, event);
		}
	}


	/*
	 * Event handler for when mouse enters picture one (drag and drop initializiation)
	 */
	private EventHandler<MouseEvent> PictureOneClickEvent = new EventHandler<MouseEvent>() {
		@Override
		public void handle(MouseEvent e) {
			autoShips.setDisable(true);
			configureDragAndDrop(pictureOne);
			setDragLengthOne();
			configureDroppedImageOne();

		}
	};
	/*
	 * Event handler for when mouse enters picture two (drag and drop initializiation)
	 */
	private EventHandler<MouseEvent> PictureTwoClickEvent = new EventHandler<MouseEvent>() {
		@Override
		public void handle(MouseEvent e) {
			autoShips.setDisable(true);
			configureDragAndDrop(pictureTwo);
			setDragLengthTwo();
			configureDroppedImageTwo();

		}
	};
	/*
	 * Event handler for when mouse enters picture three (drag and drop initializiation)
	 */
	private EventHandler<MouseEvent> PictureThreeClickEvent = new EventHandler<MouseEvent>() {
		@Override
		public void handle(MouseEvent e) {
			autoShips.setDisable(true);
			configureDragAndDrop(pictureThree);
			setDragLengthThree();
			configureDroppedImageThree();
		}
	};
	/*
	 * Event handler for when mouse enters picture four (drag and drop initializiation)
	 */
	private EventHandler<MouseEvent> PictureFourClickEvent = new EventHandler<MouseEvent>() {
		@Override
		public void handle(MouseEvent e) {
			autoShips.setDisable(true);
			configureDragAndDrop(pictureFour);
			setDragLengthFour();
			configureDroppedImageFour();
		}
	};
	/*
	 * Event handler for when mouse enters picture five (drag and drop initializiation)
	 */
	private EventHandler<MouseEvent> PictureFiveClickEvent = new EventHandler<MouseEvent>() {
		@Override
		public void handle(MouseEvent e) {
			autoShips.setDisable(true);
			configureDragAndDrop(pictureFive);
			setDragLengthFive();
			configureDroppedImageFive();
		}
	};
	/*
	 * Event handler for automating the placement of ships
	 */
	private EventHandler<MouseEvent> automateArmada = new EventHandler<MouseEvent>() {
		@Override
		public void handle(MouseEvent e) {
			pictureOne.setDisable(true);
			pictureTwo.setDisable(true);
			pictureThree.setDisable(true);
			pictureFour.setDisable(true);
			pictureFive.setDisable(true);
			automateArmadaPlacement();
			// armada.DisplayArmadaPosition();
			armada.DisplayArmadaPosition();
			autoShips.setDisable(true);
		}
	};
}
