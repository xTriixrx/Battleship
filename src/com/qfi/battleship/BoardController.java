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
	 * 
	 * @param type
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
	 * 
	 * @param location
	 * @param resources
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
	 * 
	 * @param observableMessage - A string based message that contains a message from either the player instance or opponent.
	 */
	@Override
	public void update(String observableMessage)
	{
		m_logger.info("Controller " + getID() + ": Received " + observableMessage + ".");

		if (observableMessage.equals(Message.CONNECTED.getMsg()))
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
		else if(observableMessage.contains(Message.CARRIER.getMsg()))
		{
			setSunkShipText(opponentCarrier);
		}
		else if(observableMessage.contains(Message.BATTLESHIP.getMsg()))
		{
			setSunkShipText(opponentBattleship);
		}
		else if(observableMessage.contains(Message.CRUISER.getMsg()))
		{
			setSunkShipText(opponentCruiser);
		}
		else if(observableMessage.contains(Message.SUBMARINE.getMsg()))
		{
			setSunkShipText(opponentSubmarine);
		}
		else if(observableMessage.contains(Message.DESTROYER.getMsg()))
		{
			setSunkShipText(opponentDestroyer);
		}
		else if (getCurrentTurn() == m_myTurn && m_myTurnFlag)
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

			updateOpponentGrid(boardPosition.toString(), observableMessage);
			setCurrentTurn(m_opponentTurn);
		}
		else if (getCurrentTurn() == m_opponentTurn)
		{
			opponentGuessProtocol(observableMessage);
		}
	}

	/**
	 *
	 * @param opponentMessage
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
		handleSunkShip(ship);

		setCurrentTurn(m_myTurn);
		m_myTurnFlag = false;
		m_observer.update(hitOrMissMessage);
	}

	/**
	 *
	 * @param pos
	 * @param hitOrMiss
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
	 *
	 * @param pos
	 * @param hitOrMiss
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
	 *
	 * @param shipText
	 * @param isShipSunk
	 * @return boolean
	 */
	public void updateSunkShip(Text shipText, boolean isShipSunk)
	{
		// If the shipText has not been struck out and the ship has sunk, we have an update
		if (!shipText.isStrikethrough() && isShipSunk)
		{
			setSunkShipText(shipText);

			m_logger.debug(shipText.getText() + " has sunk.");

			// If m_armada has sunk, the game is over
			if (m_armada.isArmadaSunk())
			{
				m_observer.update(Message.OVER.getMsg());
			}
			else // notify m_observer to share sunk ship detail to opponent
			{
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

				m_observer.update(shipText.getText().toUpperCase() + " " + positions);
			}
		}
	}

	/**
	 *
	 * @param shipSize
	 * @param dragStyle
	 */
	private void setDrag(int shipSize, String dragStyle)
	{
		for (Node target : playerGrid.getChildren())
		{
			if (target.getId() != null)
			{
				//
				target.setOnDragOver(event ->
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
				
				//
				target.setOnDragExited(event ->
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
		image.setOnDragDetected(event ->
		{
			Dragboard db = image.startDragAndDrop(TransferMode.ANY);

			ClipboardContent content = new ClipboardContent();
			content.putString("");
			db.setContent(content);

			event.consume();
		});
		
		// Start source drag done
		image.setOnDragDone(Event::consume);
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
				target.setOnDragDropped(event ->
				{
					//
					if (isValidStride(target, size))
					{
						dropImage(type, (Button) target, style, size);
						image.setDisable(true);

						if (m_armada.isCruiserSet() && m_armada.isCarrierSet() &&
							m_armada.isSubmarineSet() && m_armada.isDestroyerSet() &&
							m_armada.isBattleshipSet())
						{
							m_armada.logArmadaPosition();
							m_observer.update(Message.SHIPS.getMsg());
						}
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
	 *
	 * @param target
	 * @param dragStyle
	 * @param size
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
	 * 
	 * @param target
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
	 * 
	 * @param type
	 * @param target
	 * @param dropStyle
	 * @param size
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
	 *
	 * @param ship
	 * @param boardPosition
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
	 * 
	 */
	private final EventHandler<MouseEvent> mouseClickEvent = event ->
	{
		m_logger.info("Controller " + getID() + ": current turn is: " + getCurrentTurn() + ".");

		if (getCurrentTurn() == m_myTurn && m_isShipsSet)
		{
			m_toSend = ((Node) event.getTarget()).getId();
			m_toSend = m_toSend + m_mySymbol;

			((Button) event.getTarget()).setDisable(true);
			((Button) event.getTarget()).setMouseTransparent(false);
			m_logger.info("Controller " + getID() + ": sending " + m_toSend + " to opponent.");
			m_myTurnFlag = true;
			m_observer.update(m_toSend);
			destroyerImage.setDisable(true);
			submarineImage.setDisable(true);
			cruiserImage.setDisable(true);
			battleshipImage.setDisable(true);
			carrierImage.setDisable(true);
			autoShips.setDisable(true);
		}
	};

	/**
	 *
	 */
	private final EventHandler<MouseEvent> destroyerClickEvent = event ->
	{
		autoShips.setDisable(true);
		configureDragAndDrop(destroyerImage);
		setDrag(Armada.DESTROYER_SIZE, DESTROYER_SET_STYLE);
		configureDroppedImage(destroyerImage, ArmadaType.DESTROYER, Armada.DESTROYER_SIZE, DESTROYER_SET_STYLE);
	};
	
	/**
	 *
	 */
	private final EventHandler<MouseEvent> submarineClickEvent = event ->
	{
		autoShips.setDisable(true);
		configureDragAndDrop(submarineImage);
		setDrag(Armada.SUBMARINE_SIZE, SUBMARINE_SET_STYLE);
		configureDroppedImage(submarineImage, ArmadaType.SUBMARINE, Armada.SUBMARINE_SIZE, SUBMARINE_SET_STYLE);
	};
	
	/**
	 *
	 */
	private final EventHandler<MouseEvent> cruiserClickEvent = event ->
	{
		autoShips.setDisable(true);
		configureDragAndDrop(cruiserImage);
		setDrag(Armada.CRUISER_SIZE, CRUISER_SET_STYLE);
		configureDroppedImage(cruiserImage, ArmadaType.CRUISER, Armada.CRUISER_SIZE, CRUISER_SET_STYLE);
	};
	
	/**
	 * 
	 */
	private final EventHandler<MouseEvent> battleshipClickEvent = event ->
	{
		autoShips.setDisable(true);
		configureDragAndDrop(battleshipImage);
		setDrag(Armada.BATTLESHIP_SIZE, BATTLESHIP_SET_STYLE);
		configureDroppedImage(battleshipImage, ArmadaType.BATTLESHIP, Armada.BATTLESHIP_SIZE, BATTLESHIP_SET_STYLE);
	};
	
	/**
	 * 
	 */
	private final EventHandler<MouseEvent> carrierClickEvent = event ->
	{
		autoShips.setDisable(true);
		configureDragAndDrop(carrierImage);
		setDrag(Armada.CARRIER_SIZE, CARRIER_SET_STYLE);
		configureDroppedImage(carrierImage, ArmadaType.CARRIER, Armada.CARRIER_SIZE, CARRIER_SET_STYLE);
	};
	
	/**
	 * 
	 */
	private final EventHandler<MouseEvent> automateArmada = event ->
	{
		autoShips.setDisable(true);
		destroyerImage.setDisable(true);
		submarineImage.setDisable(true);
		cruiserImage.setDisable(true);
		battleshipImage.setDisable(true);
		carrierImage.setDisable(true);

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
