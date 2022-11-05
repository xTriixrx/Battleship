package com.qfi.battleship;

import java.net.URL;
import java.util.Map;
import javafx.fxml.FXML;
import java.util.HashMap;
import javafx.scene.Node;
import javafx.event.Event;
import javafx.scene.Cursor;
import javafx.scene.text.Text;
import org.apache.log4j.Logger;
import javafx.scene.paint.Color;
import java.util.ResourceBundle;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.application.Platform;
import org.apache.log4j.LogManager;
import javafx.scene.control.Button;
import javafx.scene.input.Dragboard;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.input.TransferMode;
import javafx.collections.ObservableList;
import javafx.scene.input.ClipboardContent;
import com.qfi.battleship.Armada.ArmadaType;

/**
 * The BoardController object is a JavaFX GUI controller managing the Armada and board positions
 * of the player instance this controller is instantiated for. The BoardController and Player instances
 * will communicate throughout the game submitting each other messages about status of the game and relaying
 * messages to and from the opponent.
 *
 * @author Vincent.Nigro
 * @version 1.0.0
 */
public class BoardController implements Initializable, Observer, Observable, Controller
{
	@SuppressWarnings("unused")
	@FXML
	private GridPane opponentGrid;

	@SuppressWarnings("unused")
	@FXML
	private GridPane playerGrid;

	@SuppressWarnings("unused")
	@FXML
	private ImageView destroyerImage;

	@SuppressWarnings("unused")
	@FXML
	private ImageView submarineImage;

	@SuppressWarnings("unused")
	@FXML
	private ImageView cruiserImage;

	@SuppressWarnings("unused")
	@FXML
	private ImageView battleshipImage;

	@SuppressWarnings("unused")
	@FXML
	private ImageView carrierImage;

	@SuppressWarnings("unused")
	@FXML
	private AnchorPane anchorPane;

	@SuppressWarnings("unused")
	@FXML
	private Text opponentCarrier;

	@SuppressWarnings("unused")
	@FXML
	private Text opponentBattleship;

	@SuppressWarnings("unused")
	@FXML
	private Text opponentCruiser;

	@SuppressWarnings("unused")
	@FXML
	private Text opponentSubmarine;

	@SuppressWarnings("unused")
	@FXML
	private Text opponentDestroyer;

	@SuppressWarnings("unused")
	@FXML
	private Text playerCarrier;

	@SuppressWarnings("unused")
	@FXML
	private Text playerBattleship;

	@SuppressWarnings("unused")
	@FXML
	private Text playerCruiser;

	@SuppressWarnings("unused")
	@FXML
	private Text playerSubmarine;

	@SuppressWarnings("unused")
	@FXML
	private Text playerDestroyer;

	@SuppressWarnings("unused")
	@FXML
	private Button autoShips;

	@SuppressWarnings("unused")
	@FXML
	private Text connectedText;

	@SuppressWarnings("unused")
	@FXML
	private ChoiceBox<String> OrientationChoice;

	private Armada m_armada;
	private String m_toSend;
	private int m_myTurn = 0;
	private Observer m_observer;
	private char m_mySymbol = 'A';
	private int m_currentTurn = 0;
	private int m_opponentTurn = 0;
	private final Armada m_hitArmada;
	private ArmadaAutomator m_automator;
	private boolean m_connected = false;
	private boolean m_myTurnFlag = true;
	private boolean m_isShipsSet = false;
	private String m_orientation = HORIZONTAL;
	private final Object m_turnMutex = new Object();
	private ObservableList<Node> m_buttonList = null;
	private Map<ArmadaType, String> m_stylesMap = null;
	private final Object m_connectedMutex = new Object();
	private DragDropController m_dragDropController = null;
	private static final Logger m_logger = LogManager.getLogger(BoardController.class);
	
	private static final int CLIENT_TURN = 1;
	private static final int SERVER_TURN = 2;
	private static final char CLIENT_SYMBOL = 'Z';
	private static final char SERVER_SYMBOL = 'X';
	private static final Color SUNK_COLOR = Color.RED;
	private static final String VERTICAL = "Vertical";
	private static final String CONNECTED = "Connected";
	private static final String HORIZONTAL = "Horizontal";
	private static final Color CONNECTED_COLOR = Color.BLACK;
	private static final String DISCONNECTED = "Disconnected";
	private static final String BUTTON_HIT_STYLE = "-fx-background-color: red";
	private static final String AUTO_SHIPS_STYLE = "-fx-background-color: white";
	private static final String CRUISER_SET_STYLE = "-fx-background-color: aqua";
	private static final String BUTTON_MISS_STYLE = "-fx-background-color: white";
	private static final String CARRIER_SET_STYLE = "-fx-background-color: orange";
	private static final String DESTROYER_SET_STYLE = "-fx-background-color: black";
	private static final String SUBMARINE_SET_STYLE = "-fx-background-color: green";
	private static final String BATTLESHIP_SET_STYLE = "-fx-background-color: #ff007f";

	/**
	 * BoardController constructor.
	 *
	 * @param type - The type of player this controller is associated with.
	 */
	BoardController(int type)
	{
		m_armada = new Armada();
		m_hitArmada = new Armada();
		m_automator = new ArmadaAutomator(m_armada);
		
		populateStyles();
		
		if (type == 1) // client
		{
			m_myTurn = CLIENT_TURN;
			m_mySymbol = CLIENT_SYMBOL;
			m_opponentTurn = SERVER_TURN;
		}
		else if (type == 2) // server
		{
			m_myTurn = SERVER_TURN;
			m_mySymbol = SERVER_SYMBOL;
			m_opponentTurn = CLIENT_TURN;
		}
	}

	/**
	 * Initialization of the JavaFX GUI interface called through the JavaFX runtime.
	 *
	 * @param location - The URL where the FXML layout is being referenced.
	 * @param resources - The resources bundle if any were provided.
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		m_buttonList = playerGrid.getChildren();
		m_dragDropController = new DragDropController(m_buttonList, m_armada, m_stylesMap);
		autoShips.setStyle(AUTO_SHIPS_STYLE);

		// Initialize each opponent button to be clickable & disable until game starts
		for (Node node : opponentGrid.getChildren())
		{
			if (node.getId() != null)
			{
				initMouseEvent((Button) node);
				node.setDisable(true);
			}
		}

		// If not connected to opponent, set text to disconnected
		disconnectedProtocol();

		// Add event hander's for each drag and drop image & auto ships button
		destroyerImage.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_ENTERED, destroyerClickEvent);
		submarineImage.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_ENTERED, submarineClickEvent);
		cruiserImage.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_ENTERED, cruiserClickEvent);
		battleshipImage.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_ENTERED, battleshipClickEvent);
		carrierImage.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_ENTERED, carrierClickEvent);
		autoShips.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_CLICKED, automateArmada);

		// Add drop down menu items & have HORIZONTAL set as first
		OrientationChoice.getItems().addAll(HORIZONTAL, VERTICAL);
		OrientationChoice.getSelectionModel().selectFirst();

		// Set the m_orientation value based on what is selected
		OrientationChoice.getSelectionModel().selectedItemProperty()
				.addListener((observable, oldValue, newValue) -> m_orientation = newValue);
	}

	/**
	 * An interface method for the player to notify the controller about game status & submit opponent responses.
	 *
	 * @param observableMessage - A string based message that contains a message from either the player instance or opponent.
	 */
	@Override
	public void update(String observableMessage)
	{
		m_logger.info("Controller " + getID() + ": Received " + observableMessage + ".");

		if (observableMessage.equals(Message.CONNECTED.getMsg()))
		{
			connectedProtocol();
		}
		else if (observableMessage.equals(Message.SET.getMsg()))
		{
			m_isShipsSet = true;
			for (Node node : opponentGrid.getChildren())
			{
				if (node.getId() != null)
				{
					node.setDisable(false);
				}
			}
		}
		else if (observableMessage.contains(Message.CARRIER.getMsg()))
		{
			setSunkShipText(opponentCarrier);
		}
		else if (observableMessage.contains(Message.BATTLESHIP.getMsg()))
		{
			setSunkShipText(opponentBattleship);
		}
		else if (observableMessage.contains(Message.CRUISER.getMsg()))
		{
			setSunkShipText(opponentCruiser);
		}
		else if (observableMessage.contains(Message.SUBMARINE.getMsg()))
		{
			setSunkShipText(opponentSubmarine);
		}
		else if (observableMessage.contains(Message.DESTROYER.getMsg()))
		{
			setSunkShipText(opponentDestroyer);
		}
		else if (getCurrentTurn() == m_myTurn && m_myTurnFlag)
		{
			playerGuessProtocol(observableMessage);
		}
		else if (getCurrentTurn() == m_opponentTurn)
		{
			opponentGuessProtocol(observableMessage);
		}
	}

	/**
	 * When a button is selected on the GUI for a player, the guess is sent to the opponent.
	 * If player instance receives an update from the opponent, and it is the players' turn,
	 * then it must be either a "HIT" or "MISS" message based on the players' guess. Based
	 * on that information, the player will update its opponents board and set the turn
	 * back to the opponent.
	 *
	 * @param opponentMessage - A string based message "HIT" or "MISS" sent by the opponent.
	 */
	private void playerGuessProtocol(String opponentMessage)
	{
		StringBuilder boardPosition = new StringBuilder(m_toSend);

		if (boardPosition.length() == 4)
		{
			boardPosition.deleteCharAt(3);
		}
		else if (boardPosition.length() == 5)
		{
			boardPosition.deleteCharAt(4);
		}

		updateOpponentGrid(boardPosition.toString(), opponentMessage);
		setCurrentTurn(m_opponentTurn);
	}

	/**
	 * This protocol is called only when the current turn is set to being the opponents turn. During this
	 * scenario, the opponent will send a guess of a position and the controller must determine if it was
	 * a hit or a miss. Based on that information, the protocol will submit a message to the opponent
	 * (indirectly through the player) if the game is over with all ships being sunk, or a ship has sunk.
	 * If the game proceeds, the controller will at least always send a message back to the opponent
	 * on whether the guessed position was a "HIT" or a "MISS".
	 *
	 * @param opponentMessage - A string based message sent by the opponent.
	 */
	private void opponentGuessProtocol(String opponentMessage)
	{
		String ship = "";
		String opponentGuess;
		String hitOrMissMessage;
		StringBuilder opponentBoardGuess = new StringBuilder(opponentMessage);

		// Remove prepended & appended characters: OA1X -> A1
		opponentBoardGuess.deleteCharAt(0);
		if (opponentBoardGuess.length() == 3)
		{
			opponentBoardGuess.deleteCharAt(2);
		}
		else if (opponentBoardGuess.length() == 4)
		{
			opponentBoardGuess.deleteCharAt(3);
		}

		// Determine if opponent guess was a hit or miss
		opponentGuess = opponentBoardGuess.toString();
		boolean isHit = m_armada.calculateHit(opponentGuess);

		if (isHit)
		{
			hitOrMissMessage = Message.HIT.getMsg();
			ship = m_armada.updateArmada(opponentGuess);
			addHitShipPosition(ship, opponentGuess);
		}
		else
		{
			hitOrMissMessage = Message.MISS.getMsg();
		}

		updatePlayerGrid(opponentGuess, hitOrMissMessage);

		// If a ship has sunk, or the armada is lost, the controller will submit an update message
		handleSunkShip(ship);

		setCurrentTurn(m_myTurn);
		m_myTurnFlag = false;
		m_observer.update(hitOrMissMessage);
	}

	/**
	 * An updater method which will update a button in the player portion of the grid based on if the
	 * position guessed by the opponent was a hit or a miss.
	 *
	 * @param pos - Some position on the player grid that has either been hit or missed.
	 * @param hitOrMiss - The flag representing whether the opponent hit or missed at the given position.
	 */
	private void updatePlayerGrid(String pos, String hitOrMiss)
	{
		String playerPosition = "P" + pos;

		for (Node node : playerGrid.getChildren())
		{
			if (node.getId() != null && node.getId().equals(playerPosition))
			{
				if (hitOrMiss.equals(Message.HIT.getMsg()))
				{
					node.setMouseTransparent(false);
					node.setStyle(BUTTON_HIT_STYLE);
				}
				else if (hitOrMiss.equals(Message.MISS.getMsg()))
				{
					node.setMouseTransparent(false);
					node.setStyle(BUTTON_MISS_STYLE);
				}

				break;
			}
		}
	}

	/**
	 * An updater method which will update a button in the opponent portion of the grid based on if the
	 * position guessed was a hit or a miss.
	 *
	 * @param pos - Some position on the opponent grid that has either been hit or missed.
	 * @param hitOrMiss - The flag representing whether the player hit or missed at the given position.
	 */
	private void updateOpponentGrid(String pos, String hitOrMiss)
	{
		for (Node node : opponentGrid.getChildren())
		{
			if (node.getId() != null && node.getId().equals(pos))
			{
				if (hitOrMiss.equals(Message.HIT.getMsg()))
				{
					node.setStyle(BUTTON_HIT_STYLE);
				}
				else if (hitOrMiss.equals(Message.MISS.getMsg()))
				{
					node.setStyle(BUTTON_MISS_STYLE);
				}

				break;
			}
		}
	}

	/**
	 * If a ship has been determined to be hit, this method will be called in order to test whether the
	 * ship has sunk or not.
	 *
	 * @param ship - The string representing the ship that has been hit by the opponent.
	 */
	private void handleSunkShip(String ship)
	{
		switch (ship)
		{
			case Armada.CRUISER_NAME:
				updateSunkShip(playerCruiser, m_armada.isCruiserSunk());
				break;
			case Armada.CARRIER_NAME:
				updateSunkShip(playerCarrier, m_armada.isCarrierSunk());
				break;
			case Armada.DESTROYER_NAME:
				updateSunkShip(playerDestroyer, m_armada.isDestroyerSunk());
				break;
			case Armada.SUBMARINE_NAME:
				updateSunkShip(playerSubmarine, m_armada.isSubmarineSunk());
				break;
			case Armada.BATTLESHIP_NAME:
				updateSunkShip(playerBattleship, m_armada.isBattleshipSunk());
				break;
			default:
				m_logger.error("Received unknown ship of type: " + ship + ".");
		}
	}

	/**
	 * A method for updating the text board of available ships if a ship has sunk. When a ship has sunk, the
	 * player instance will receive a message regarding which ship sunk and the positions of the sunken ship.
	 * If the armada has completely sunk, then the player instance will receive a message of "OVER". Both
	 * messages to the player instance will be relayed back to the opponent.
	 *
	 * @param shipText - A text object to check if the ship has sunk.
	 * @param isShipSunk - A flag representing whether the armada believes the ship has sunk or not.
	 */
	public void updateSunkShip(Text shipText, boolean isShipSunk)
	{
		// If the shipText has not been struck out and the ship has sunk, we have an update
		if (!shipText.isStrikethrough() && isShipSunk)
		{
			setSunkShipText(shipText);

			m_logger.debug(shipText.getText() + " has sunk.");

			String positions = "";
			switch (shipText.getText().toUpperCase())
			{
				case Armada.CARRIER_NAME:
					positions = m_hitArmada.getCarrierPositions();
					break;
				case Armada.CRUISER_NAME:
					positions = m_hitArmada.getCruiserPositions();
					break;
				case Armada.DESTROYER_NAME:
					positions = m_hitArmada.getDestroyerPositions();
					break;
				case Armada.SUBMARINE_NAME:
					positions = m_hitArmada.getSubmarinePositions();
					break;
				case Armada.BATTLESHIP_NAME:
					positions = m_hitArmada.getBattleshipPositions();
					break;
				default:
					m_logger.error("Received unknown ship of type: " + shipText.getText().toUpperCase() + ".");
			}

			// If m_armada has sunk, the game is over
			if (m_armada.isArmadaSunk())
			{
				m_observer.update(Message.OVER.getMsg());
			}
			else
			{
				m_observer.update(shipText.getText().toUpperCase() + " " + positions);
			}
		}
	}

	/**
	 * Sets the drag highlighting and un-highlighting for buttons.
	 *
	 * @param shipSize - The size of the ship being dragged.
	 * @param dragStyle - The style associated with dragged ship.
	 */
	private void setDrag(int shipSize, String dragStyle)
	{
		for (Node target : playerGrid.getChildren())
		{
			if (target.getId() != null)
			{
				// Enables target to accept transfer on drop during drag
				target.setOnDragOver(event ->
				{
					// accept drag over the target if it is not dragged from the same node and if it has data
					if (event.getGestureSource() != target && event.getDragboard().hasString())
					{
						event.acceptTransferModes(TransferMode.ANY);
					}

					event.consume();
				});
				
				// When target is entered, highlight the current stride
				target.setOnDragEntered(event ->
				{
					//
					if (isValidStride(target, shipSize))
					{
						playerGrid.setCursor(Cursor.DEFAULT);
						highlightImage((Button) target, dragStyle, shipSize);
					}

					event.consume();
				});
				
				// When target is left, unhighlight the current stride
				target.setOnDragExited(event ->
				{
					unHighlightImage((Button) target);
					event.consume();
				});
			}
		}
	}

	/**
	 * Configures & enables the passed image to be dragged across the GUI board.
	 *
	 * @param image - Some ImageView object representing a drag and droppable ship.
	 */
	private void configureDragAndDrop(ImageView image)
	{
		image.setOnDragDetected(event ->
		{
			// Enables image to be dragged
			Dragboard db = image.startDragAndDrop(TransferMode.ANY);

			ClipboardContent content = new ClipboardContent();
			content.putString("");
			db.setContent(content);

			event.consume();
		});

		image.setOnDragDone(Event::consume);
	}
	
	/**
	 * Configures drop event where if the stride is valid, the ship is placed. If the entire armada is also
	 * set, then the controller will update the player instance.
	 *
	 * @param image - Some ImageView object representing a drag and droppable ship.
	 * @param type - The type of ship represented by the image.
	 * @param size - The size of the ship.
	 * @param style - The style to be applied to the ship on drop.
	 */
	private void configureDroppedImage(ImageView image, ArmadaType type, int size, String style)
	{
		for (Node target : playerGrid.getChildren())
		{
			if (target.getId() != null)
			{
				target.setOnDragDropped(event ->
				{
					// If the drop occurs on a valid stride
					if (isValidStride(target, size))
					{
						dropImage(type, (Button) target, style, size);
						image.setDisable(true);

						if (m_armada.isCruiserSet() && m_armada.isCarrierSet() &&
							m_armada.isSubmarineSet() && m_armada.isDestroyerSet() &&
							m_armada.isBattleshipSet())
						{
							autoShips.setDisable(true);
							m_armada.logArmadaPosition();
							m_observer.update(Message.SHIPS.getMsg());
						}
					}
					
					// Finish the event
					event.setDropCompleted(true);
					event.consume();
				});
			}
		}
	}
	
	/**
	 * A boolish function returning whether the stride is a valid set of positions in some orientation.
	 *
	 * @param target - The button for which the focus (center) is on.
	 * @param size - The size of the ship.
	 * @return boolean - A flag representing whether the stride for a ship is valid set position.
	 */
	private boolean isValidStride(Node target, int size)
	{
		boolean valid = false;
		
		if (m_orientation.equalsIgnoreCase(HORIZONTAL))
		{
			valid = m_dragDropController.freeStrideHorizontal(target, size);
		}
		else if (m_orientation.equalsIgnoreCase(VERTICAL))
		{
			valid = m_dragDropController.freeStrideVertical(target, size);
		}
		
		return valid;
	}
	
	/**
	 * Calls to the drag and drop controller to highlight some ship drag on GUI in some orientation.
	 *
	 * @param target - The button for which the focus (center) is on.
	 * @param dragStyle - The style to be applied to the set of buttons where this ship is dragged onto.
	 * @param size - The size of the ship.
	 */
	private void highlightImage(Button target, String dragStyle, int size)
	{

		if (m_orientation.equalsIgnoreCase(HORIZONTAL))
		{
			m_dragDropController.highlightHorizontal(target, dragStyle, size);
		}
		else if (m_orientation.equalsIgnoreCase(VERTICAL))
		{
			m_dragDropController.highlightVertical(target, dragStyle, size);
		}
	}
	
	/**
	 * Calls to the drag and drop controller to unhighlight some ship drag on GUI in some orientation.
	 *
	 * @param target - The button for which the focus (center) is on.
	 */
	private void unHighlightImage(Button target)
	{
		if (m_orientation.equalsIgnoreCase(HORIZONTAL))
		{
			m_dragDropController.unhighlightHorizontal(target);
		}
		else if (m_orientation.equalsIgnoreCase(VERTICAL))
		{
			m_dragDropController.unhighlightVertical(target);
		}
	}
	
	/**
	 * Calls to the drag and drop controller to drop some ship on GUI in some orientation.
	 *
	 * @param type The type of ship within the Armada to be dropped.
	 * @param target - The button for which the focus (center) is on.
	 * @param dropStyle - The style to be applied to the set of buttons where this ship should be dropped.
	 * @param size - The size of the ship.
	 */
	private void dropImage(ArmadaType type, Button target, String dropStyle, int size)
	{
		if (m_orientation.equalsIgnoreCase(HORIZONTAL))
		{
			m_dragDropController.dropHorizontal(type, target, dropStyle, size);
		}
		else if (m_orientation.equalsIgnoreCase(VERTICAL))
		{
			m_dragDropController.dropVertical(type, target, dropStyle, size);
		}
	}

	/**
	 * Adds some position that was determined to be hit on the internal armada object to another armada
	 * object in order to track hit positions for later use for when the ship is sunk.
	 *
	 * @param ship - The name of the ship that was hit.
	 * @param boardPosition - The position on the board where the ship was hit.
	 */
	private void addHitShipPosition(String ship, String boardPosition)
	{
		switch (ship)
		{
			case Armada.CARRIER_NAME:
				m_hitArmada.addToCarrier(boardPosition);
				break;
			case Armada.CRUISER_NAME:
				m_hitArmada.addToCruiser(boardPosition);
				break;
			case Armada.DESTROYER_NAME:
				m_hitArmada.addToDestroyer(boardPosition);
				break;
			case Armada.SUBMARINE_NAME:
				m_hitArmada.addToSubmarine(boardPosition);
				break;
			case Armada.BATTLESHIP_NAME:
				m_hitArmada.addToBattleship(boardPosition);
				break;
		}
	}

	/**
	 * If the "CONNECTED" message has not been received upon GUI initialization, then perform the
	 * disconnected protocol setting the GUI to signify the opponent is not online yet.
	 */
	private void disconnectedProtocol()
	{
		synchronized (m_connectedMutex)
		{
			if (!m_connected)
			{
				connectedText.setText(DISCONNECTED);
				connectedText.setFill(SUNK_COLOR);

				destroyerImage.setDisable(true);
				submarineImage.setDisable(true);
				cruiserImage.setDisable(true);
				battleshipImage.setDisable(true);
				carrierImage.setDisable(true);
				autoShips.setDisable(true);
			}
			else
			{
				connectedText.setText(CONNECTED);
				connectedText.setFill(CONNECTED_COLOR);
			}
		}
	}

	/**
	 * If the "CONNECTED" message is received, then the connected protocol updating the players'
	 * board must be performed.
	 */
	private void connectedProtocol()
	{
		synchronized (m_connectedMutex)
		{
			m_connected = true;

			if (destroyerImage != null)
			{
				connectedText.setText(CONNECTED);
				connectedText.setFill(CONNECTED_COLOR);

				destroyerImage.setDisable(false);
				submarineImage.setDisable(false);
				cruiserImage.setDisable(false);
				battleshipImage.setDisable(false);
				carrierImage.setDisable(false);
				autoShips.setDisable(false);
			}
		}
	}
	
	/**
	 * EventHandler for whenever a players' guess is selected (button click on grid) the guess will be
	 * submitted to the player in order for the player to pass the message to the opponent.
	 */
	private final EventHandler<MouseEvent> mouseClickEvent = event ->
	{
		m_logger.debug("Controller " + getID() + ": current turn is: " + getCurrentTurn() + ".");

		if (getCurrentTurn() == m_myTurn && m_isShipsSet)
		{
			m_toSend = ((Node) event.getTarget()).getId();
			m_toSend = m_toSend + m_mySymbol;

			((Button) event.getTarget()).setDisable(true);
			((Button) event.getTarget()).setMouseTransparent(false);

			m_logger.debug("Controller " + getID() + ": sending " + m_toSend + " to opponent.");

			m_myTurnFlag = true;
			m_observer.update(m_toSend);
		}
	};

	/**
	 * EventHandler for when the destroyerImage has been entered to be drag and dropped.
	 */
	private final EventHandler<MouseEvent> destroyerClickEvent = event ->
	{
		configureDragAndDrop(destroyerImage);
		setDrag(Armada.DESTROYER_SIZE, DESTROYER_SET_STYLE);
		configureDroppedImage(destroyerImage, ArmadaType.DESTROYER, Armada.DESTROYER_SIZE, DESTROYER_SET_STYLE);
	};
	
	/**
	 * EventHandler for when the submarineImage has been entered to be drag and dropped.
	 */
	private final EventHandler<MouseEvent> submarineClickEvent = event ->
	{
		configureDragAndDrop(submarineImage);
		setDrag(Armada.SUBMARINE_SIZE, SUBMARINE_SET_STYLE);
		configureDroppedImage(submarineImage, ArmadaType.SUBMARINE, Armada.SUBMARINE_SIZE, SUBMARINE_SET_STYLE);
	};
	
	/**
	 * EventHandler for when the cruiserImage has been entered to be drag and dropped.
	 */
	private final EventHandler<MouseEvent> cruiserClickEvent = event ->
	{
		configureDragAndDrop(cruiserImage);
		setDrag(Armada.CRUISER_SIZE, CRUISER_SET_STYLE);
		configureDroppedImage(cruiserImage, ArmadaType.CRUISER, Armada.CRUISER_SIZE, CRUISER_SET_STYLE);
	};
	
	/**
	 * EventHandler for when the battleshipImage has been entered to be drag and dropped.
	 */
	private final EventHandler<MouseEvent> battleshipClickEvent = event ->
	{
		configureDragAndDrop(battleshipImage);
		setDrag(Armada.BATTLESHIP_SIZE, BATTLESHIP_SET_STYLE);
		configureDroppedImage(battleshipImage, ArmadaType.BATTLESHIP, Armada.BATTLESHIP_SIZE, BATTLESHIP_SET_STYLE);
	};
	
	/**
	 * EventHandler for when the carrierImage has been entered to be drag and dropped.
	 */
	private final EventHandler<MouseEvent> carrierClickEvent = event ->
	{
		configureDragAndDrop(carrierImage);
		setDrag(Armada.CARRIER_SIZE, CARRIER_SET_STYLE);
		configureDroppedImage(carrierImage, ArmadaType.CARRIER, Armada.CARRIER_SIZE, CARRIER_SET_STYLE);
	};
	
	/**
	 * EventHandler for automating the Armada during a mouse click on the associated button.
	 */
	private final EventHandler<MouseEvent> automateArmada = event ->
	{
		autoShips.setDisable(true);
		destroyerImage.setDisable(true);
		submarineImage.setDisable(true);
		cruiserImage.setDisable(true);
		battleshipImage.setDisable(true);
		carrierImage.setDisable(true);

		// Automate the placement of the armada & log positions
		m_automator.automateArmadaPlacement(m_buttonList, m_stylesMap);
		m_armada.logArmadaPosition();

		// Signals player instance that the ships have been placed.
		m_observer.update(Message.SHIPS.getMsg());
	};

	/**
	 * Populates the styles map.
	 */
	private void populateStyles()
	{
		m_stylesMap = new HashMap<>();
		m_stylesMap.put(ArmadaType.DESTROYER, DESTROYER_SET_STYLE);
		m_stylesMap.put(ArmadaType.SUBMARINE, SUBMARINE_SET_STYLE);
		m_stylesMap.put(ArmadaType.CRUISER, CRUISER_SET_STYLE);
		m_stylesMap.put(ArmadaType.BATTLESHIP, BATTLESHIP_SET_STYLE);
		m_stylesMap.put(ArmadaType.CARRIER, CARRIER_SET_STYLE);
	}

	/**
	 * Sets the current turn.
	 *
	 * @param t - The turn for which the internal turn value should be set to.
	 */
	@Override
	public void setCurrentTurn(int t)
	{
		synchronized (m_turnMutex)
		{
			m_currentTurn = t;
		}
	}

	/**
	 * Returns an integer representing the current turn.
	 *
	 * @return int - The value of the current turn.
	 */
	@Override
	public int getCurrentTurn()
	{
		int turn = 0;

		synchronized (m_turnMutex)
		{
			turn = m_currentTurn;
		}

		return turn;
	}

	/**
	 * Initializes the passed button to add the mouseClickEvent event to its event handler.
	 *
	 * @param b - A button object to initialize and add a mouse event to.
	 */
	private void initMouseEvent(Button b)
	{
		b.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_CLICKED, mouseClickEvent);
	}

	/**
	 * Called by stage when one player closes their respective window.
	 */
	@Override
	public void shutdown()
	{
		Platform.exit();
		m_observer.update(Message.SHUTDOWN.getMsg());
	}

	/**
	 * When a ship has sunk, this method is called in order to set some Text object to the color RED and
	 * with a strikethrough.
	 *
	 * @param ship - The Text object to update.
	 */
	private void setSunkShipText(Text ship)
	{
		ship.setFill(SUNK_COLOR);
		ship.setStrikethrough(true);
	}

	/**
	 * Registers the controllers Observer reference to the passed object.
	 *
	 * @param observer An Observer instance that the Observable instance needs to update.
	 */
	@Override
	public void register(Observer observer)
	{
		m_observer = observer;
	}

	/**
	 * Returns some hash id representing this controller instance.
	 *
	 * @return int - A unique hash code integer representing this controller.
	 */
	public int getID()
	{
		return System.identityHashCode(this);
	}
}
