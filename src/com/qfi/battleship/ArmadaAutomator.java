package com.qfi.battleship;

import java.util.List;
import java.util.Map;

import javafx.scene.Node;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.security.SecureRandom;
import org.apache.logging.log4j.Logger;

import com.qfi.battleship.Armada.ArmadaType;

import javafx.collections.ObservableList;
import org.apache.logging.log4j.LogManager;

/**
 * ArmadaAutomator is an Armada placement automator which is triggered when a user clicks on the 
 * 'autoShips' fx:id labeled button or by an automated player. This class will go through each ship in
 * the Armada starting for the largest to the smallest, and find a randomized placement on the player's
 * board where the ship will be placed for the game. Once all of the ships have been placed, the automator
 * will update the background highlighting of each button that is representing a ship's partial position
 * and disable the button.
 * 
 * @author Vincent.Nigro
 * @version 1.0.0
 */
public class ArmadaAutomator 
{
	private Armada armada = null;
	private SecureRandom random = null;
	private static final short VERTICAL = 2;
	private static final short HORIZONTAL = 1;
	private static final short UPPER_BOUND = 10;
	private static final short CHARACTER_SHIFT = 64;
	private static final short STANDARD_COL_POS = 1;
	private static final short STANDARD_ROW_POS = 2;
	private static final short STANDARD_ID_LENGTH = 3;
	private static final char OUT_OF_BOUNDS_COL = 'Z';
	private Logger logger = LogManager.getLogger(ArmadaAutomator.class);
	private static final String BUTTON_SET_STYLE = "-fx-background-color: green";
	
	// An interface to wrap lambda add functions for different ship types
	protected interface ArmadaAdd { void add(Armada armada, List<String> shipPos); }
	
	// Lambda for each type of ship add into the armada
	private ArmadaAdd carrierAdd = (armada, shipPos) -> armada.addToCarrier(shipPos);
	private ArmadaAdd cruiserAdd = (armada, shipPos) -> armada.addToCruiser(shipPos);
	private ArmadaAdd submarineAdd = (armada, shipPos) -> armada.addToSubmarine(shipPos);
	private ArmadaAdd destroyerAdd = (armada, shipPos) -> armada.addToDestroyer(shipPos);
	private ArmadaAdd battleshipAdd = (armada, shipPos) -> armada.addToBattleship(shipPos);
	
	/**
	 * ArmadaAutomator constructor.
	 * 
	 * @param armada An Armada object provided by the parent controller.
	 */
	ArmadaAutomator(Armada armada)
	{
		// byte seed for the SecureRandom object
		byte[] seed = ByteBuffer.allocate(Long.SIZE / Byte.SIZE)
			.putLong(System.currentTimeMillis()).array();
		
		// Instantiate random object
		random = new SecureRandom(seed);
		
		this.armada = armada;
	}
	
	/**
	 * Main public method to be called by an automated controller in order to automate the Armada placement.
	 * 
	 * @param buttonList An unsorted observable list of nodes containing button's.
	 * @param styles A map of styles based on the type of ship that is being placed.
	 */
	public void automateArmadaPlacement()
	{
		List<String> usedButtons = new ArrayList<>();
		
		// Place ships onto player's board
		placeShip(carrierAdd, usedButtons, Armada.CARRIER_SIZE);
		placeShip(battleshipAdd, usedButtons, Armada.BATTLESHIP_SIZE);
		placeShip(cruiserAdd, usedButtons, Armada.CRUISER_SIZE);
		placeShip(submarineAdd, usedButtons, Armada.SUBMARINE_SIZE);
		placeShip(destroyerAdd, usedButtons, Armada.DESTROYER_SIZE);
	}
	
	/**
	 * Main public method to be called by the controller in order to automate the Armada placement.
	 * 
	 * @param buttonList An unsorted observable list of nodes containing button's.
	 * @param styles A map of styles based on the type of ship that is being placed.
	 */
	public void automateArmadaPlacement(ObservableList<Node> buttonList, Map<ArmadaType, String> styles)
	{
		List<String> usedButtons = new ArrayList<>();
		
		// Place ships onto player's board
		placeShip(carrierAdd, usedButtons, Armada.CARRIER_SIZE);
		placeShip(battleshipAdd, usedButtons, Armada.BATTLESHIP_SIZE);
		placeShip(cruiserAdd, usedButtons, Armada.CRUISER_SIZE);
		placeShip(submarineAdd, usedButtons, Armada.SUBMARINE_SIZE);
		placeShip(destroyerAdd, usedButtons, Armada.DESTROYER_SIZE);
		
		// Highlight all players ships that were placed by automator
		highlightPlacement(buttonList, usedButtons, styles);
	}
	
	/**
	 * This function is a generic placement function that will add any sized ship once a valid position
	 * is found in either the horizontal or vertical mode into the Armada using the ArmadaAdd interface.
	 * 
	 * @param adder ArmadaAdd interface object that will add unique ship position into Armada.
	 * @param used A list of existing used positions that cannot be reused.
	 * @param size A short representing the size of the ship being added.
	 */
	private void placeShip(ArmadaAdd adder, List<String> used, short size)
	{
		List<String> shipPos = null;
		
		int alignment = random.nextInt(2) + 1;

		if (alignment == HORIZONTAL) // 1
		{
			shipPos = placeHorizontal(used, size);
		}
		else if (alignment == VERTICAL) // 2
		{
			shipPos = placeVertical(used, size);
		}
		
		adder.add(armada, shipPos);
		used.addAll(shipPos);
	}
	
	/**
	 * A generic vertical placement function which will randomly attempt a start position row and
	 * attempt to stride deeper into the available rows. If a position in the placement plan is already
	 * used or if the endRow position is greater than the available rows, then the placement function
	 * will continue to attempt new placement options until one is found.
	 * 
	 * @param used A list of existing used positions that cannot be reused.
	 * @param size A short representing the size of the ship being added.
	 * @return List<String>
	 */
	private List<String> placeVertical(List<String> used, short size)
	{
		int row = 0;
		int col = 0;
		int endRow = 0;
		char letter = 0;
		boolean placeable = false;
		StringBuilder sb = new StringBuilder();
		List<String> placement = new ArrayList<>();
		
		logger.debug("Attempting placement for ship of size: {}", size);
		
		while (!placeable)
		{
			row = random.nextInt(UPPER_BOUND) + 1;
			col = random.nextInt(UPPER_BOUND) + 1;
			
			endRow = row + (size - 1);
			letter = convertToColumnLetter(col);
			
			logger.trace("Attempting placement with starting position: {}{} and with ending position: {}{}.",
					letter, row, letter, endRow);
			
			// Iterate and generate each subposition for the attempted placement
			for (int i = 0; i < size; i++)
			{
				sb.append(letter);
				sb.append(row + i);
				placement.add(sb.toString());
				sb.setLength(0); // Clear out old string
			}
			
			logger.debug("Ship Placement Attempt: {}", placement);
			
			// If end row is within upper bound and placement is a unique set, its a placeable position
			if (endRow <= UPPER_BOUND && Collections.disjoint(used, placement))
			{
				placeable = true;
			}
			else
			{
				placement.clear();
			}
		}
		
		return placement;
	}
	
	/**
	 * A generic horizontal placement function which will randomly attempt a start position column and
	 * attempt to stride deeper into the available columns. If a position in the placement plan is already
	 * used or if the endLetter position is greater than the available column letters, then the placement
	 * function will continue to attempt new placement options until one is found.
	 * 
	 * @param used A list of existing used positions that cannot be reused.
	 * @param size A short representing the size of the ship being added.
	 * @return List<String>
	 */
	private List<String> placeHorizontal(List<String> used, short size)
	{
		int row = 0;
		int col = 0;
		char endLetter = 0;
		char startLetter = 0;
		boolean placeable = false;
		StringBuilder sb = new StringBuilder();
		List<String> placement = new ArrayList<>();
		
		logger.debug("Attempting placement for ship of size: {}", size);
		
		while (!placeable)
		{
			row = random.nextInt(UPPER_BOUND) + 1;
			col = random.nextInt(UPPER_BOUND) + 1;
			
			startLetter = convertToColumnLetter(col);
			endLetter = convertToColumnLetter(col + (size - 1));

			logger.trace("Attempting placement with starting position: {}{} and with ending position: {}{}.",
					startLetter, row, endLetter, row);

			// Iterate and generate each subposition for the attempted placement 
			for (int i = 0; i < size; i++)
			{
				sb.append((char) (startLetter + i));
				sb.append(row);
				placement.add(sb.toString());
				sb.setLength(0); // Clear out old string
			}
			
			logger.debug("Ship Placement Attempt: {}", placement);
			
			// If end column letter is not out of bounds and placement is a unique set, its a placeable position
			if (endLetter != OUT_OF_BOUNDS_COL && Collections.disjoint(used, placement))
			{
				placeable = true;
			}
			else
			{
				placement.clear();
			}
		}
		
		return placement;
	}
	
	/**
	 * Highlighting function which will iterate over each ship position that exists and iterate over
	 * each Node in the buttonList looking for the matching node id that needs to be highlighted. Once
	 * a match is found, the button node will be highlighted and disabled.
	 * 
	 * @param buttonList An unordered list of nodes which contains buttons.
	 * @param shipPos A list of all ship positions that were generated.
	 * @param styles A map of styles based on the type of ship that is being placed.
	 */
	private void highlightPlacement(ObservableList<Node> buttonList, List<String> shipPos, Map<ArmadaType, String> styles)
	{
		for (String pos : shipPos)
		{
			for (Node node : buttonList)
			{
				// If node id is matched with the current position, highlight the node
				if (matchNodeID(node, pos))
				{
					String style = determineStyle(pos, styles);
					highlightNode(node, style);
				}
			}
		}
	}
	
	/**
	 * Will determine the type of style that needs to be applied for the given position.
	 * 
	 * @param position A matched position that needs to be highlighted.
	 * @param styles A map of styles based on the type of ship that is being placed.
	 * @return String
	 */
	private String determineStyle(String position, Map<ArmadaType, String> styles)
	{
		String style = "";
		
		if (armada.getDestroyer().contains(position))
		{
			style = styles.get(ArmadaType.DESTROYER);
		}
		else if (armada.getSubmarine().contains(position))
		{
			style = styles.get(ArmadaType.SUBMARINE);
		}
		else if (armada.getCruiser().contains(position))
		{
			style = styles.get(ArmadaType.CRUISER);
		}
		else if (armada.getBattleship().contains(position))
		{
			style = styles.get(ArmadaType.BATTLESHIP);
		}
		else if (armada.getCarrier().contains(position))
		{
			style = styles.get(ArmadaType.CARRIER);
		}
		
		return style;
	}
	
	/**
	 * Attempts to match a node id and returns the boolean representation of whether a match was
	 * found or not for this node id and position pair.
	 * 
	 * @param node An individual node that is on the players board.
	 * @param pos A position that represents some button id.
	 * @return boolean
	 */
	private boolean matchNodeID(Node node, String pos)
	{
		if (node.getId() != null)
		{
			if (node.getId().length() == STANDARD_ID_LENGTH &&
					pos.length() == (STANDARD_ID_LENGTH - 1))
			{
				return nodeMatchLength3(node.getId(), pos);
				
			}
			else if (node.getId().length() == (STANDARD_ID_LENGTH + 1) &&
						pos.length() == (STANDARD_ID_LENGTH))
			{
				return nodeMatchLength4(node.getId(), pos);
			}
		}
		
		return false;
	}
	
	/**
	 * Converts an integer representing some column character on a battleship grid into a character.
	 * 
	 * @param col An integer representing some column character on a battleship grid.
	 * @return char
	 */
	private char convertToColumnLetter(int col)
	{
		char letter;
		
		// If column is greater than 10, the column DNE and is out of bounds
		if (col > UPPER_BOUND)
		{
			// Returns 'Z' character to symbolize out of bounds column
			return OUT_OF_BOUNDS_COL;
		}
		
		// If column is in bounds, calculate letter by shifting column to appropriate capital letter
		letter = (char) (col + CHARACTER_SHIFT);
		
		return letter;
	}
	
	/**
	 * Boolean function that will return true if the provided node id is a match off of the provided
	 * position.
	 * 
	 * @param id A unique JavaFX ID to be compared to the provided position.
	 * @param pos A position to check off of the provided node's id.
	 * @return boolean
	 */
	private boolean nodeMatchLength3(String id, String pos)
	{
		return (id.charAt(STANDARD_COL_POS) == pos.charAt(STANDARD_COL_POS - 1) &&
				id.charAt(STANDARD_ROW_POS) == pos.charAt(STANDARD_ROW_POS - 1));
	}
	
	/**
	 * Boolean function that will return true if the provided node id is a match off of the provided
	 * position.
	 * 
	 * @param id A unique JavaFX ID to be compared to the provided position.
	 * @param pos A position to check off of the provided node's id.
	 * @return boolean
	 */
	private boolean nodeMatchLength4(String id, String pos)
	{
		return (id.charAt(STANDARD_COL_POS) == pos.charAt(STANDARD_COL_POS - 1) &&
				id.charAt(STANDARD_ROW_POS) == pos.charAt(STANDARD_ROW_POS - 1) &&
				id.charAt(STANDARD_ROW_POS + 1) == pos.charAt(STANDARD_ROW_POS));
	}
	
	/**
	 * Highlights a button to a color and disables the button.
	 * 
	 * @param node A matching node that needs to be highlighted and disabled.
	 */
	private void highlightNode(Node node, String style)
	{
		if (!style.isEmpty())
		{
			node.setStyle(style);
		}
		else
		{
			node.setStyle(BUTTON_SET_STYLE);
		}
	}
}
