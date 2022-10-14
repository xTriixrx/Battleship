package com.qfi.battleship;

import java.net.URL;
import java.util.Map;
import javafx.fxml.FXML;
import java.util.HashMap;
import javafx.scene.Node;
import javafx.scene.Cursor;
import javafx.scene.text.Text;
import javafx.scene.paint.Color;
import java.util.ResourceBundle;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.input.Dragboard;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.input.TransferMode;
import org.apache.logging.log4j.Logger;
import javafx.collections.ObservableList;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.scene.input.ClipboardContent;
import org.apache.logging.log4j.LogManager;
import com.qfi.battleship.Armada.ArmadaType;

/**
 * 
 * @author Vincent.Nigro
 * @version 1.0.0
 */
public class BoardController implements Initializable, Observer, Observable, Controller
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

	private String toSend;
	private int myTurn = 0;
	private Observer observer;
	private char mySymbol = 'A';
	private int currentTurn = 0;
	private int opponentTurn = 0;
	private Armada armada = null;
	private Object turnMutex = null;
	private boolean myTurnFlag = true;
	private boolean isShipsSet = false;
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
	private static final String OVER_MSG = "OVER";
	private static final Color SUNK_COLOR = Color.RED;
	private static final String VERTICAL = "Vertical";
	private static final String HORIZONTAL = "Horizontal";
	private static final String BUTTON_HIT_STYLE = "-fx-background-color: red";
	private static final String AUTO_SHIPS_STYLE = "-fx-background-color: white";
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
		turnMutex = new Object();
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
	 * Called by stage when one player closes their respective window.
	 */
	public void shutdown()
	{
		Platform.exit();
		observer.update("SHUTDOWN");
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
				node.setDisable(true);
			}
		}
		
		dragDropController = new DragDropController(buttonList, armada, stylesMap);
		
		autoShips.setStyle(AUTO_SHIPS_STYLE);
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

	@Override
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

	@Override
	public void setCurrentTurn(int t)
	{
		synchronized (turnMutex)
		{
			currentTurn = t;
		}
	}
	
	public int getCurrentTurn()
	{
		int turn = 0;
		
		synchronized (turnMutex)
		{
			turn = currentTurn;
		}
		
		return turn;
	}

	public int getID()
	{
		return System.identityHashCode(this);
	}
	

	@Override
	public void register(Observer observer)
	{
		this.observer = observer;
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
		
		setCurrentTurn(opponentTurn);
	}
	
	/**
	 * 
	 * @param shipText
	 * @param isShipSunk
	 * @return boolean
	 */
	public boolean update(Text shipText, boolean isShipSunk)
	{
		boolean justSunk = false;
		
		// If the shipText has not been striked and the ship has sunk, we have an update
		if (!shipText.isStrikethrough() && isShipSunk)
		{
			justSunk = true;
			setSunkShipText(shipText);
			
			logger.debug("{} SUNK", shipText.getText());
			
			// If armada has sunk, the game is over
			if (armada.isArmadaSunk())
			{
				observer.update(OVER_MSG);
			}
			else // notify observer to share sunk ship detail to opponent
			{
				observer.update(shipText.getText().toUpperCase());
			}
		}
		
		return justSunk;
	}

	/**
	 * 
	 * @param pos
	 * @param HM
	 */
	public void updatePlayerGrid(String pos, String HM)
	{
		for (Node node : playerGrid.getChildren())
		{
			if (node.getId() != null)
			{
				if (node.getId().equals(pos))
				{
					if (HM.equals(HIT))
					{
						boolean updated = false;
						node.setMouseTransparent(false);
						node.setStyle(BUTTON_HIT_STYLE);
						
						updated = update(playerCruiser, armada.isCruiserSunk());
					
						if (!updated)
						{
							updated = update(playerCarrier, armada.isCarrierSunk());
						}
						
						if (!updated)
						{
							updated = update(playerDestroyer, armada.isDestroyerSunk());
						}
					
						if (!updated)
						{
							updated = update(playerSubmarine, armada.isSubmarineSunk());
						}
					
						if (!updated)
						{
							updated = update(playerBattleship, armada.isBattleshipSunk());
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
	public void update(String s)
	{
		logger.info("Controller {}: Received {}.", getID(), s);

		if (s.equals("SET"))
		{
			isShipsSet = true;
			for (Node node : opponentGrid.getChildren())
			{
				if (node.getId() != null)
				{
					node.setDisable(false);
				}
			}
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
		else if (getCurrentTurn() == myTurn && myTurnFlag)
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
		else if (getCurrentTurn() == opponentTurn)
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
			{
				HorM = HIT;
			}
			else
			{
				HorM = MISS;
			}

			updatePlayerGrid(t, HorM);

			setCurrentTurn(myTurn);
			myTurnFlag = false;
			observer.update(HorM);
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
						highlightImage((Button) target, dragStyle, shipSize);
					}

					event.consume();
				});
				
				//
				target.setOnDragExited((event) ->
				{
					unHighlightImage((Button) target);
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
	 * @param target
	 * @param dragStyle
	 * @param size
	 */
	private void highlightImage(Button target, String dragStyle, int size)
	{

		if (orientation.equalsIgnoreCase(HORIZONTAL))
		{
			dragDropController.highlightHorizontal(target, dragStyle, size);
		}
		else if (orientation.equalsIgnoreCase(VERTICAL))
		{
			dragDropController.highlightVertical(target, dragStyle, size);
		}
	}
	
	/**
	 * 
	 * @param target
	 */
	private void unHighlightImage(Button target)
	{
		if (orientation.equalsIgnoreCase(HORIZONTAL))
		{
			dragDropController.unhighlightHorizontal(target);
		}
		else if (orientation.equalsIgnoreCase(VERTICAL))
		{
			dragDropController.unhighlightVertical(target);
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
		logger.info("Controller {}: current turn is: {}", getID(), getCurrentTurn());

		if (getCurrentTurn() == myTurn && isShipsSet)
		{
			toSend = ((Node) event.getTarget()).getId();
			toSend = new StringBuilder(toSend).append(mySymbol).toString();
			((Button) event.getTarget()).setDisable(true);
			((Button) event.getTarget()).setMouseTransparent(false);
			logger.info("Controller {}: sending {} to opponent.", getID(), toSend);
			myTurnFlag = true;
			observer.update(toSend);
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
	
	/**
	 * 
	 */
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
		observer.update("SHIPS");
	};
}
