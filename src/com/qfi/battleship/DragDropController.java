package com.qfi.battleship;

import java.util.Map;
import javafx.scene.Node;
import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;
import javafx.scene.control.Button;
import javafx.collections.ObservableList;
import com.qfi.battleship.Armada.ArmadaType;

/**
 * DragDropController is drag and drop managing controller which contains the processing and handling of dragging
 * ships onto the player's board as well as dropping ships into position.
 * 
 * @author Vincent.Nigro
 * @version 1.0.0
 */
public class DragDropController
{
	private Armada armada = null;
	private Map<ArmadaType, String> styles = null;
	private ObservableList<Node> buttonList = null;
	
	private static final short UPPER_BOUND = 10;
	private static final int TOTAL_BUTTONS = 100;
	private static final short CHARACTER_SHIFT = 64;
	private static final short STANDARD_COL_POS = 1;
	private static final short STANDARD_ROW_POS = 2;
	private static final char OUT_OF_BOUNDS_COL = 'Z';
	private static final String BACKGROUND_BUTTON_STYLE = "-fx-background-color: blue";
	private static final Logger logger = LogManager.getLogger(DragDropController.class);

	/**
	 * DragDropController constructor.
	 * 
	 * @param buttonList - An unsorted observable list of nodes containing button's.
	 * @param armada - An Armada object provided by the parent controller.
	 * @param styles - A map of styles based on the type of ship that is being placed.
	 */
	DragDropController(ObservableList<Node> buttonList, Armada armada, Map<ArmadaType, String> styles)
	{
		this.styles = styles;
		this.armada = armada;
		this.buttonList = buttonList;
	}
	
	/**
	 * Public interface function for determining whether a horizontal block of buttons is not blocked by existing
	 * ships that have been placed. This function will return true if the set of blocks in the horizontal range are free
	 * to be highlighted/dropped by the user and false if an existing ship position is within the horizontal range.
	 * 
	 * @param target - The current contextual target button, is where the mouse is currently.
	 * @param size - The size of the ship that is being checked.
	 * @return boolean - Returns a boolean for if a horizontal stride is available or not.
	 */
	public boolean freeStrideHorizontal(Node target, int size)
	{
		boolean available = true;
		char startPos = target.getId().charAt(STANDARD_COL_POS);
		char endPos = convertToColumnLetter((startPos - CHARACTER_SHIFT) + (size - 1));
		
		// Iterate through each button and if on horizontal range, check to see if the button is in the armada
		for (int i = 0; i < TOTAL_BUTTONS; i++)
		{
			Node button = buttonList.get(i);
			if (inHorizontalRange(button.getId(), target.getId(), startPos, endPos) &&
				inArmada(button.getId().substring(STANDARD_COL_POS)))
			{
				available = false;
				break;
			}
		}
		
		return available;
	}
	
	/**
	 * Public interface function for determining whether a vertical block of buttons is not blocked by existing
	 * ships that have been placed. This function will return true if the set of blocks in the vertical range are free
	 * to be highlighted/dropped by the user and false if an existing ship position is within the vertical range.
	 * 
	 * @param target - The current contextual target button, is where the mouse is currently.
	 * @param size - The size of the ship that is being checked.
	 * @return boolean - Returns a boolean for if a vertical stride is available or not.
	 */
	public boolean freeStrideVertical(Node target, int size)
	{
		boolean available = true;
		int startRow = Integer.parseInt(target.getId().substring(STANDARD_ROW_POS));
		int endRow = startRow + (size - 1);
		
		// Iterate through each button and if on vertical range, check to see if the button is in the armada
		for (int i = 0; i < TOTAL_BUTTONS; i++)
		{
			Node button = buttonList.get(i);
			if (inVerticalRange(button.getId(), target.getId(), startRow, endRow) &&
				inArmada(button.getId().substring(STANDARD_COL_POS)))
			{
				available = false;
				break;
			}
		}
		
		return available;
	}
	
	/**
	 * Public interface function for highlighting the appropriate horizontal buttons when dragging a ship
	 * across the player board during the placement stage.
	 *
	 * @param target - The current contextual target button, is where the mouse is currently.
	 * @param style - The custom style to be applied to button.
	 * @param size - The size of the ship represented by ArmadaType.
	 */
	public void highlightHorizontal(Button target, String style, int size)
	{
		char startPos = target.getId().charAt(STANDARD_COL_POS);
		char endPos = convertToColumnLetter((startPos - CHARACTER_SHIFT) + (size - 1));
		
		logger.info("Highlighting all button's within the horizontal range.");
		
		// Iterate through each button and if on horizontal range, set the background to be highlighted
		for (int i = 0; i < TOTAL_BUTTONS; i++)
		{
			Node button = buttonList.get(i);
			if (inHorizontalRange(button.getId(), target.getId(), startPos, endPos) &&
				!inArmada(button.getId().substring(STANDARD_COL_POS)))
			{
				button.setStyle(style);
				logger.trace("buttonID: " + button.getId() + " is in horizontal range.");
			}
		}
	}
	
	/**
	 * Public interface function for highlighting the appropriate vertical buttons when dragging a ship
	 * across the player board during the placement stage.
	 *
	 * @param target - The current contextual target button, is where the mouse is currently.
	 * @param style - The custom style to be applied to button.
	 * @param size - The size of the ship represented by ArmadaType.
	 */
	public void highlightVertical(Button target, String style, int size)
	{
		int startRow = Integer.parseInt(target.getId().substring(STANDARD_ROW_POS));
		int endRow = startRow + (size - 1);
		
		logger.info("Highlighting all button's within the vertical range.");
		
		// Iterate through each button and if on vertical range, set the background to be highlighted
		for (int i = 0; i < TOTAL_BUTTONS; i++)
		{
			Node button = buttonList.get(i);
			if (inVerticalRange(button.getId(), target.getId(), startRow, endRow) &&
				!inArmada(button.getId().substring(STANDARD_COL_POS)))
			{
				button.setStyle(style);
				logger.trace("buttonID: " + button.getId() + " is in vertical range.");
			}
		}
	}
	
	/**
	 * Public interface function for un-highlighting the appropriate horizontal buttons when dragging a ship
	 * across the player board during the placement stage.
	 * 
	 * @param target - The current contextual target button, is where the mouse is currently.
	 */
	public void unhighlightHorizontal(Button target)
	{
		char startPos = target.getId().charAt(STANDARD_COL_POS);
		
		logger.info("Un-highlighting all button's within the horizontal range.");

		// If the target node is disabled and is in the armada, it has just been
		// dropped and needs to be recolored to the appropriate ship color.
		if (inArmada(target.getId().substring(STANDARD_COL_POS)))
		{
			target.setStyle(determineStyle(target.getId().substring(STANDARD_COL_POS)));
		}
		else
		{
			// Sets the target node to be un-highlighted
			target.setStyle(BACKGROUND_BUTTON_STYLE);
		}
		
		// Iterate through each button and if on horizontal range, set the background to be un-highlighted
		for (int i = 0; i < TOTAL_BUTTONS; i++)
		{
			Node button = buttonList.get(i);
			if (!inHorizontalRange(button.getId(), target.getId(), startPos, startPos))
			{
				// If the node is disabled and is in the armada, it has just been
				// dropped and needs to be recolored to the appropriate ship color.
				if (inArmada(button.getId().substring(STANDARD_COL_POS)))
				{
					button.setStyle(determineStyle(button.getId().substring(STANDARD_COL_POS)));
				}
				else
				{
					// Sets the target node to be un-highlighted
					button.setStyle(BACKGROUND_BUTTON_STYLE);
				}
				
				logger.trace("buttonID: " + button.getId() + " is not in horizontal range.");
			}
		}
	}
	
	/**
	 * Public interface function for un-highlighting the appropriate vertical buttons when dragging a ship
	 * across the player board during the placement stage.
	 * 
	 * @param target - The current contextual target button, is where the mouse is currently.
	 */
	public void unhighlightVertical(Button target)
	{
		int startRow = Integer.parseInt(target.getId().substring(STANDARD_ROW_POS));
		
		logger.info("Un-highlighting all button's within the vertical range.");

		// If the target node is disabled and is in the armada, it has just been
		// dropped and needs to be recolored to the appropriate ship color.
		if (inArmada(target.getId().substring(STANDARD_COL_POS)))
		{
			target.setStyle(determineStyle(target.getId().substring(STANDARD_COL_POS)));
		}
		else
		{
			// Sets the target node to be un-highlighted
			target.setStyle(BACKGROUND_BUTTON_STYLE);
		}
		
		// Iterate through each button and if on vertical range, set the background to be un-highlighted
		for (int i = 0; i < TOTAL_BUTTONS; i++)
		{
			Node button = buttonList.get(i);
			if (!inVerticalRange(button.getId(), target.getId(), startRow, startRow))
			{
				// If the node is disabled and is in the armada, it has just been
				// dropped and needs to be recolored to the appropriate ship color.
				if (inArmada(button.getId().substring(STANDARD_COL_POS)))
				{
					button.setStyle(determineStyle(button.getId().substring(STANDARD_COL_POS)));
				}
				else
				{
					// Sets the node to be unhighlighted
					button.setStyle(BACKGROUND_BUTTON_STYLE);
				}
				
				logger.trace("buttonID: " + button.getId() + " is not in vertical range.");
			}
		}
	}
	
	/**
	 * Public interface function for placing the appropriate horizontal buttons when dropping a ship
	 * on the player board during the placement stage.
	 * 
	 * @param type - An ArmadaType object representing what type of ship is being moved.
	 * @param target - The current contextual target button, is where the mouse is currently.
	 * @param style - The custom style to be applied to button.
	 * @param size - The size of the ship represented by ArmadaType.
	 */
	public void dropHorizontal(ArmadaType type, Button target, String style, int size)
	{
		char startPos = target.getId().charAt(STANDARD_COL_POS);
		char endPos = convertToColumnLetter((startPos - CHARACTER_SHIFT) + (size - 1));
		
		logger.info("Adding all button's within the horizontal range to the armada.");
		
		// Iterate through each button and if on horizontal range, add the button to the armada for ArmadaType
		for (int i = 0; i < TOTAL_BUTTONS; i++)
		{
			Node button = buttonList.get(i);
			if (inHorizontalRange(button.getId(), target.getId(), startPos, endPos))
			{
				addToArmada(type, button.getId().substring(STANDARD_COL_POS));
				
				button.setStyle(style);
				logger.trace("buttonID: " + button.getId() + " is in horizontal range.");
			}
		}
	}
	
	/**
	 * Public interface function for placing the appropriate vertical buttons when dropping a ship
	 * on the player board during the placement stage.
	 * 
	 * @param type - An ArmadaType object representing what type of ship is being moved.
	 * @param target - The current contextual target button, is where the mouse is currently.
	 * @param style - The custom style to be applied to button.
	 * @param size - The size of the ship represented by ArmadaType.
	 */
	public void dropVertical(ArmadaType type, Button target, String style, int size)
	{
		int startRow = Integer.parseInt(target.getId().substring(STANDARD_ROW_POS));
		int endRow = startRow + (size - 1);
		
		logger.info("Dropping all button's to within the vertical range to the armada.");
		
		// Iterate through each button and if on vertical range, add the button to the armada for ArmadaType
		for (int i = 0; i < TOTAL_BUTTONS; i++)
		{
			Node button = buttonList.get(i);
			if (inVerticalRange(button.getId(), target.getId(), startRow, endRow))
			{
				addToArmada(type, button.getId().substring(STANDARD_COL_POS));
				
				button.setStyle(style);
				logger.trace("buttonID: " + button.getId() + " is in vertical range.");
			}
		}
	}
	
	/**
	 * Iterative range finder that returns a boolean representing whether a provided button is in
	 * the horizontal range or not.
	 * 
	 * @param buttonID - String representing the current button to be checked.
	 * @param targetID - String representing the current target context button to check based on.
	 * @param startPos - A character representing the starting column to search.
	 * @param endPos - A character representing the last column to search.
	 * @return boolean - Returns a boolean for whether a provided button is in the horizontal range.
	 */
	protected boolean inHorizontalRange(String buttonID, String targetID, char startPos, char endPos)
	{
		String strideID = "";
		boolean inRange = false;
		char charStride = startPos;
		StringBuilder sb = new StringBuilder();
		
		// If endPos is 'Z' the buttonID is not in range
		if (endPos == OUT_OF_BOUNDS_COL)
		{
			return false;
		}

		// while not in range and our stride has not passed the ending horizontal column position of the range
		while (charStride <= endPos && !inRange)
		{
			// Create ID string for button being strided over in range
			sb.append(targetID.charAt(0)); // Inserts initial character from the fx:id associated with target
			sb.append(charStride++); // Inserts current column being strided over and increments stride
			sb.append(targetID.substring(STANDARD_ROW_POS)); // appends the target's row value (1-10) to the end of the strideID
			
			strideID = sb.toString();
			
			logger.trace("Comparing buttonID: " + buttonID + " to strideID: " + strideID + ".");
			inRange = (buttonID.equalsIgnoreCase(strideID));
			
			sb.setLength(0); // Clear out old string
		}
		
		return inRange;
	}
	
	/**
	 * Iterative range finder that returns a boolean representing whether a provided button is in
	 * the vertical range or not.
	 *  
	 * @param buttonID - String representing the current button to be checked.
	 * @param targetID - String representing the current target context button to check based on.
	 * @param startRow - Integer representing the starting row to search.
	 * @param endRow - Integer representing the last row to search.
	 * @return boolean - Returns a boolean for whether a provided button is in the vertical range.
	 */
	protected boolean inVerticalRange(String buttonID, String targetID, int startRow, int endRow)
	{
		String strideID = "";
		boolean inRange = false;
		int rowStride = startRow;
		StringBuilder sb = new StringBuilder();
		
		// If endRow is greater than 10 the buttonID is not in range
		if (endRow > UPPER_BOUND)
		{
			return false;
		}
		
		// while not in range and our stride has not passed the ending vertical column position of the range
		while (rowStride <= endRow && !inRange)
		{
			// Create ID string for button being strided over in rage
			sb.append(targetID.charAt(0)); // Inserts initial character from the fx:id associated with target
			sb.append(targetID.charAt(STANDARD_COL_POS)); // Inserts the column character from the fx:id associated with target
			sb.append(rowStride++); // Inserts current row being strided over and increments stride
			
			strideID = sb.toString();
			
			logger.trace("Comparing buttonID: " + buttonID + " to strideID: " + strideID + ".");
			inRange = (buttonID.equalsIgnoreCase(strideID));
			
			sb.setLength(0); // Clear out old string
		}
		
		return inRange;
	}
	
	/**
	 * Adds the provided button ID into the appropriate ship inside the armada.
	 * 
	 * @param type - The type of ship in the Armada to add the id to.
	 * @param buttonID - A string representing the button id.
	 */
	private void addToArmada(ArmadaType type, String buttonID)
	{
		if (type.equals(ArmadaType.DESTROYER))
		{
			armada.addToDestroyer(buttonID);
		}
		else if (type.equals(ArmadaType.SUBMARINE))
		{
			armada.addToSubmarine(buttonID);
		}
		else if (type.equals(ArmadaType.CRUISER))
		{
			armada.addToCruiser(buttonID);
		}
		else if (type.equals(ArmadaType.BATTLESHIP))
		{
			armada.addToBattleship(buttonID);
		}
		else if (type.equals(ArmadaType.CARRIER))
		{
			armada.addToCarrier(buttonID);
		}
	}
	
	/**
	 * Will determine if a position exists within the armada.
	 * 
	 * @param position - A matched position that needs to be found in the armada.
	 * @return String - Returns a boolean for if a position exists within the armada.
	 */
	private boolean inArmada(String position)
	{
		return armada.getCruiser().contains(position) ||
				armada.getCarrier().contains(position) ||
				armada.getDestroyer().contains(position) ||
				armada.getSubmarine().contains(position) ||
				armada.getBattleship().contains(position);
	}
	
	/**
	 * Will determine the type of style that needs to be applied for the given position.
	 * 
	 * @param position - A matched position that needs to be highlighted.
	 * @return String - Returns a String for the style required for a specific ship in the armada.
	 */
	private String determineStyle(String position)
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
	 * Converts an integer representing some column character on a battleship grid into a character.
	 * 
	 * @param col - An integer representing some column character on a battleship grid.
	 * @return char - Returns a character representing some column.
	 */
	protected char convertToColumnLetter(int col)
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
}
