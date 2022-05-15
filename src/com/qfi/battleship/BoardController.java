package com.qfi.battleship;

import java.net.URL;
import java.util.Map;
import javafx.fxml.FXML;
import java.util.HashMap;
import javafx.scene.Node;
import javafx.scene.Cursor;
import javafx.scene.text.Text;
import java.util.ResourceBundle;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.input.Dragboard;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.input.TransferMode;
import org.apache.logging.log4j.Logger;
import javafx.collections.ObservableList;
import javafx.beans.value.ObservableValue;
import javafx.scene.input.ClipboardContent;
import org.apache.logging.log4j.LogManager;
import com.qfi.battleship.Armada.ArmadaType;

/**
 * 
 * @author Vincent.Nigro
 * @version 1.0.0
 */
public class BoardController implements Initializable, Observer, Observable
{
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

	private Observer sobs;
	private String toSend;
	private int myTurn = 0;
	private char mySymbol = 'A';
	private int currentTurn = 0;
	private int opponentTurn = 0;
	private Armada armada = null;
	private boolean myTurnFlag = true;
	private boolean isShipsSet = false;
	private boolean CarrierSunk = false;
	private boolean CruiserSunk = false;
	private boolean SubmarineSunk = false;
	private boolean DestroyerSunk = false;
	private boolean BattleshipSunk = false;
	private String orientation = HORIZONTAL;
	private ArmadaAutomator automator = null;
	private ObservableList<Node> buttonList = null;
	private Map<ArmadaType, String> stylesMap = null;
	private DragDropController dragDropController = null;
	private Logger logger = LogManager.getLogger(BoardController.class);
	
	private static final int clientTurn = 1;
	private static final int serverTurn = 2;
	private static final String HIT = "Hit";
	private static final String MISS = "Miss";
	private static final char clientSymbol = 'Z';
	private static final char serverSymbol = 'X';
	private static final Color SUNK_COLOR = Color.RED;
	private static final String VERTICAL = "Vertical";
	private static final String HORIZONTAL = "Horizontal";
	private static final String BUTTON_HIT_STYLE = "-fx-background-color: red";
	private static final String CRUISER_SET_STYLE = "-fx-background-color: aqua";
	private static final String BUTTON_MISS_STYLE = "-fx-background-color: white";
	private static final String CARRIER_SET_STYLE = "-fx-background-color: orange";
	private static final String DESTROYER_SET_STYLE = "-fx-background-color: black";
	private static final String SUBMARINE_SET_STYLE = "-fx-background-color: green";
	private static final String BATTLESHIP_SET_STYLE = "-fx-background-color: #ff007f";

	/**
	 * 
	 * @param whoami
	 */
	BoardController(int whoami)
	{
		armada = new Armada();
		automator = new ArmadaAutomator(armada);
		
		populateStyles();
		
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
	
	/**
	 * Populates the styles map.
	 */
	private void populateStyles()
	{
		stylesMap = new HashMap<>();
		stylesMap.put(ArmadaType.DESTROYER, DESTROYER_SET_STYLE);
		stylesMap.put(ArmadaType.SUBMARINE, SUBMARINE_SET_STYLE);
		stylesMap.put(ArmadaType.CRUISER, CRUISER_SET_STYLE);
		stylesMap.put(ArmadaType.BATTLESHIP, BATTLESHIP_SET_STYLE);
		stylesMap.put(ArmadaType.CARRIER, CARRIER_SET_STYLE);
	}

	/**
	 * 
	 * @param location
	 * @param resources
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		buttonList = playerGrid.getChildren();
		
		for (Node node : opponentGrid.getChildren())
		{
			if (node.getId() != null)
			{
				initMouseEvent((Button) node);
			}
		}
		
		dragDropController = new DragDropController(buttonList, armada, stylesMap);
		
		autoShips.setStyle("-fx-background-color: white");
		pictureOne.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_ENTERED, PictureOneClickEvent);
		pictureTwo.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_ENTERED, PictureTwoClickEvent);
		pictureThree.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_ENTERED, PictureThreeClickEvent);
		pictureFour.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_ENTERED, PictureFourClickEvent);
		pictureFive.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_ENTERED, PictureFiveClickEvent);
		autoShips.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_CLICKED, automateArmada);
		OrientationChoice.getItems().addAll(HORIZONTAL, VERTICAL);
		OrientationChoice.getSelectionModel().selectFirst();

		OrientationChoice.getSelectionModel().selectedItemProperty()
				.addListener((ObservableValue<? extends String> observable, String oldValue,
						String newValue) -> orientation = newValue);
	}
	
	private void setSunkShipText(Text ship)
	{
		ship.setFill(SUNK_COLOR);
		ship.setStrikethrough(true);
	}

	public void initMouseEvent(Button b)
	{
		b.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_CLICKED, mouseClickEvent);
	}

	public Armada getArmada()
	{
		return armada;
	}

	public void setIsShipsSet(boolean s)
	{
		isShipsSet = s;
	}

	public boolean getIsShipsSet()
	{
		return isShipsSet;
	}

	public int getCurrentTurn()
	{
		return currentTurn;
	}

	public void setCurrentTurn(int t)
	{
		currentTurn = t;
	}

	public int getID()
	{
		return System.identityHashCode(this);
	}
	

	@Override
	public void registerObserver(Observer s) {
		sobs = s;

	}
	
	@Override
	public void notifyObserver(String str)
	{
		sobs.update(str);
	}

	@Override
	public void removeObserver() {
		sobs = null;
	}

	/**
	 * 
	 * @param pos
	 * @param HM
	 */
	public void updateOpponentGrid(String pos, String HM)
	{
		for (Node node : opponentGrid.getChildren())
		{
			if (node.getId() != null)
			{
				if (node.getId().equals(pos))
				{
					if (HM.equals(HIT))
					{
						node.setStyle(BUTTON_HIT_STYLE);
					}
					else if (HM.equals(MISS))
					{
						node.setStyle(BUTTON_MISS_STYLE);
					}
					
					break;
				}
			}
		}
		
		notifyObserver(Integer.toString(opponentTurn));
		currentTurn = opponentTurn;
	}

	/**
	 * 
	 * @param pos
	 * @param HM
	 */
	public void updateplayerGrid(String pos, String HM)
	{
		for (Node node : playerGrid.getChildren())
		{
			if (node.getId() != null)
			{
				if (node.getId().equals(pos))
				{
					if (HM.equals(HIT))
					{
						node.setMouseTransparent(false);
						node.setStyle(BUTTON_HIT_STYLE);
						
						if (!CarrierSunk && armada.isCarrierSunk())
						{
							CarrierSunk = true;
							if(CarrierSunk && BattleshipSunk && CruiserSunk && 
									SubmarineSunk && DestroyerSunk)
							{
								System.out.println("CARRIER SUNK");
								setSunkShipText(playerCarrier);
								notifyObserver("OVER");
							}
							else
							{
								CarrierSunk = true;
								System.out.println("CARRIER SUNK");
								setSunkShipText(playerCarrier);
								notifyObserver("CARRIER");
							}
						}
						
						if (!BattleshipSunk && armada.isBattleshipSunk())
						{
							BattleshipSunk = true;
							if(CarrierSunk && BattleshipSunk && CruiserSunk && 
									SubmarineSunk && DestroyerSunk)
							{
								System.out.println("BATTLESHIP SUNK");
								setSunkShipText(playerBattleship);
								notifyObserver("OVER");
							}
							else
							{
								System.out.println("BATTLESHIP SUNK");
								setSunkShipText(playerBattleship);
								notifyObserver("BATTLESHIP");
							}
						}
						if (!CruiserSunk && armada.isCruiserSunk())
						{
							CruiserSunk = true;
							if(CarrierSunk && BattleshipSunk && CruiserSunk && 
									SubmarineSunk && DestroyerSunk)
							{
								System.out.println("CRUISER SUNK");
								setSunkShipText(playerCruiser);
								notifyObserver("OVER");
							}
							else
							{
								System.out.println("CRUISER SUNK");
								setSunkShipText(playerCruiser);
								notifyObserver("CRUISER");
							}
						}
						if (!SubmarineSunk && armada.isSubmarineSunk())
						{
							SubmarineSunk = true;
							if(CarrierSunk && BattleshipSunk && CruiserSunk && 
									SubmarineSunk && DestroyerSunk)
							{
								System.out.println("SUBMARINE SUNK");
								setSunkShipText(playerSubmarine);
								notifyObserver("OVER");
							}
							else
							{
								System.out.println("SUBMARINE SUNK");
								setSunkShipText(playerSubmarine);
								notifyObserver("SUBMARINE");
							}
						}
						if (!DestroyerSunk && armada.isDestroyerSunk())
						{
							DestroyerSunk = true;
							if(CarrierSunk && BattleshipSunk && CruiserSunk && 
									SubmarineSunk && DestroyerSunk)
							{
								System.out.println("DESTROYER SUNK");
								setSunkShipText(playerDestroyer);
								notifyObserver("OVER");
							}
							else
							{
								System.out.println("DESTROYER SUNK");
								setSunkShipText(playerDestroyer);
								notifyObserver("DESTROYER");
							}
						}
					}
					else if (HM.equals(MISS))
					{
						node.setMouseTransparent(false);
						node.setStyle(BUTTON_MISS_STYLE);
					}
					
					break;
				}
			}
		}
	}

	/**
	 * 
	 * @param s
	 */
	@Override
	public void update(String s) {
		System.out.println("SC: Received " + s + ".");

		if (s.equals("SET"))
		{
			isShipsSet = true;
		}
		else if(s.equals("CARRIER"))
		{
			setSunkShipText(opponentCarrier);
		}
		else if(s.equals("BATTLESHIP"))
		{
			setSunkShipText(opponentBattleship);
		}
		else if(s.equals("CRUISER"))
		{
			setSunkShipText(opponentCruiser);
		}
		else if(s.equals("SUBMARINE"))
		{
			setSunkShipText(opponentSubmarine);
		}
		else if(s.equals("DESTROYER"))
		{
			setSunkShipText(opponentDestroyer);
		}
		else if (currentTurn == myTurn && myTurnFlag)
		{
			StringBuilder temp = new StringBuilder(toSend);
			String t = "";
			if (temp.length() == 4) {
				temp.deleteCharAt(3);
				t = temp.toString();
			} else if (temp.length() == 5) {
				temp.deleteCharAt(4);
				t = temp.toString();
			}

			updateOpponentGrid(t, s);
		}
		else if (currentTurn == opponentTurn)
		{
			StringBuilder temp = new StringBuilder(s);
			String HorM = "";

			temp.setCharAt(0, 'P');
			String t = "";
			if (temp.length() == 4) {
				temp.deleteCharAt(3);
				t = temp.toString();
			} else if (temp.length() == 5) {
				temp.deleteCharAt(4);
				t = temp.toString();
			}
			
			String boardPos = t.substring(1); 
			boolean isHit = armada.calculateHit(boardPos);
			armada.updateArmada(boardPos);

			if (isHit)
				HorM = HIT;
			else
				HorM = MISS;

			updateplayerGrid(t, HorM);

			notifyObserver(HorM + Integer.toString(myTurn));
			currentTurn = myTurn;
			myTurnFlag = false;
		}
	}
	
	/**
	 * 
	 * @param type
	 * @param shipSize
	 * @param dragStyle
	 */
	private void setDrag(ArmadaType type, int shipSize, String dragStyle)
	{
		for (Node target : playerGrid.getChildren())
		{
			if (target.getId() != null)
			{
				//
				target.setOnDragOver((event) ->
				{
					/* data is dragged over the target */
					/*
					 * accept it only if it is not dragged from the same node and if it has a string
					 * data
					 */
					if (event.getGestureSource() != target && event.getDragboard().hasString())
					{
						/* allow for moving */
						event.acceptTransferModes(TransferMode.ANY);
					}

					event.consume();
				});
				
				//
				target.setOnDragEntered((event) ->
				{
					//
					if (isValidStride(target, shipSize))
					{
						playerGrid.setCursor(Cursor.DEFAULT);
						highlightImage(type, (Button) target, dragStyle, shipSize);
					}

					event.consume();
				});
				
				//
				target.setOnDragExited((event) ->
				{
					unHighlightImage((Button) target, shipSize);
					event.consume();
				});
			}
		}
	}

	/**
	 * 
	 * @param image
	 */
	private void configureDragAndDrop(ImageView image) {
		image.setOnDragDetected((event) ->
		{
			Dragboard db = image.startDragAndDrop(TransferMode.ANY);

			ClipboardContent content = new ClipboardContent();
			content.putString("");
			db.setContent(content);

			event.consume();
		});
		
		// Start source drag done
		image.setOnDragDone((event) ->
		{
			event.consume();
		});
		// End Source Drag Done
	}
	
	/**
	 * 
	 * @param image
	 * @param type
	 * @param size
	 * @param style
	 */
	private void configureDroppedImage(ImageView image, ArmadaType type, int size, String style)
	{
		for (Node target : playerGrid.getChildren())
		{
			if (target.getId() != null)
			{
				target.setOnDragDropped((event) ->
				{
					//
					if (isValidStride(target, size))
					{
						dropImage(type, (Button) target, style, size);
						image.setDisable(true);
					}
					
					//
					event.setDropCompleted(true);
					event.consume();
				});
			}
		}
	}
	
	/**
	 * 
	 * @param target
	 * @param size
	 * @return boolean
	 */
	private boolean isValidStride(Node target, int size)
	{
		boolean valid = false;
		
		if (orientation.equalsIgnoreCase(HORIZONTAL))
		{
			valid = dragDropController.freeStrideHorizontal(target, size);
		}
		else if (orientation.equalsIgnoreCase(VERTICAL))
		{
			valid = dragDropController.freeStrideVertical(target, size);
		}
		
		return valid;
	}
	
	/**
	 * 
	 * @param type
	 * @param target
	 * @param dragStyle
	 * @param size
	 */
	private void highlightImage(ArmadaType type, Button target, String dragStyle, int size)
	{

		if (orientation.equalsIgnoreCase(HORIZONTAL))
		{
			dragDropController.highlightHorizontal(type, target, dragStyle, size);
		}
		else if (orientation.equalsIgnoreCase(VERTICAL))
		{
			dragDropController.highlightVertical(type, target, dragStyle, size);
		}
	}
	
	/**
	 * 
	 * @param target
	 * @param size
	 */
	private void unHighlightImage(Button target, int size)
	{
		if (orientation.equalsIgnoreCase(HORIZONTAL))
		{
			dragDropController.unhighlightHorizontal(target, size);
		}
		else if (orientation.equalsIgnoreCase(VERTICAL))
		{
			dragDropController.unhighlightVertical(target, size);
		}
	}
	
	/**
	 * 
	 * @param type
	 * @param target
	 * @param dropStyle
	 * @param size
	 */
	private void dropImage(ArmadaType type, Button target, String dropStyle, int size)
	{
		if (orientation.equalsIgnoreCase(HORIZONTAL))
		{
			dragDropController.dropHorizontal(type, target, dropStyle, size);
		}
		else if (orientation.equalsIgnoreCase(VERTICAL))
		{
			dragDropController.dropVertical(type, target, dropStyle, size);
		}
	}
	
	/**
	 * 
	 */
	private EventHandler<MouseEvent> mouseClickEvent = (event) ->
	{
		System.out.println("CONTROLLER " + myTurn + ": current turn is: " + currentTurn);

		if (currentTurn == myTurn && isShipsSet)
		{
			toSend = ((Node) event.getTarget()).getId();
			toSend = new StringBuilder(toSend).append(mySymbol).toString();
			((Button) event.getTarget()).setDisable(true);
			((Button) event.getTarget()).setMouseTransparent(false);
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
	};

	/**
	 *
	 */
	private EventHandler<MouseEvent> PictureOneClickEvent = (event) ->
	{
		autoShips.setDisable(true);
		configureDragAndDrop(pictureOne);
		setDrag(ArmadaType.DESTROYER, Armada.DESTROYER_SIZE, DESTROYER_SET_STYLE);
		configureDroppedImage(pictureOne, ArmadaType.DESTROYER, Armada.DESTROYER_SIZE, DESTROYER_SET_STYLE);
	};
	
	/**
	 *
	 */
	private EventHandler<MouseEvent> PictureTwoClickEvent = (event) ->
	{
		autoShips.setDisable(true);
		configureDragAndDrop(pictureTwo);
		setDrag(ArmadaType.SUBMARINE, Armada.SUBMARINE_SIZE, SUBMARINE_SET_STYLE);
		configureDroppedImage(pictureTwo, ArmadaType.SUBMARINE, Armada.SUBMARINE_SIZE, SUBMARINE_SET_STYLE);
	};
	
	/**
	 *
	 */
	private EventHandler<MouseEvent> PictureThreeClickEvent = (event) ->
	{
		autoShips.setDisable(true);
		configureDragAndDrop(pictureThree);
		setDrag(ArmadaType.CRUISER, Armada.CRUISER_SIZE, CRUISER_SET_STYLE);
		configureDroppedImage(pictureThree, ArmadaType.CRUISER, Armada.CRUISER_SIZE, CRUISER_SET_STYLE);
	};
	
	/**
	 * 
	 */
	private EventHandler<MouseEvent> PictureFourClickEvent = (event) ->
	{
		autoShips.setDisable(true);
		configureDragAndDrop(pictureFour);
		setDrag(ArmadaType.BATTLESHIP, Armada.BATTLESHIP_SIZE, BATTLESHIP_SET_STYLE);
		configureDroppedImage(pictureFour, ArmadaType.BATTLESHIP, Armada.BATTLESHIP_SIZE, BATTLESHIP_SET_STYLE);
	};
	
	private EventHandler<MouseEvent> PictureFiveClickEvent = (event) -> 
	{
		autoShips.setDisable(true);
		configureDragAndDrop(pictureFive);
		setDrag(ArmadaType.CARRIER, Armada.CARRIER_SIZE, CARRIER_SET_STYLE);
		configureDroppedImage(pictureFive, ArmadaType.CARRIER, Armada.CARRIER_SIZE, CARRIER_SET_STYLE);
	};
	
	/**
	 * 
	 */
	private EventHandler<MouseEvent> automateArmada = (event) ->
	{
		pictureOne.setDisable(true);
		pictureTwo.setDisable(true);
		pictureThree.setDisable(true);
		pictureFour.setDisable(true);
		pictureFive.setDisable(true);
		automator.automateArmadaPlacement(buttonList, stylesMap);
		armada.logArmadaPosition();
		autoShips.setDisable(true);
		notifyObserver("SHIPS");
	};
}
