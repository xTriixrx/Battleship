package com.qfi.battleship;

import javafx.scene.Node;
import javafx.scene.control.Button;
import org.apache.logging.log4j.Logger;
import javafx.collections.ObservableList;
import org.apache.logging.log4j.LogManager;
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
	private ObservableList<Node> buttonList = null;
	private Logger logger = LogManager.getLogger(DragDropController.class);
	
	protected static final int TOTAL_BUTTONS = 100;
	protected static final short UPPER_BOUND = 10;
	protected static final short CHARACTER_SHIFT = 64;
	protected static final short STANDARD_COL_POS = 1;
	protected static final short STANDARD_ROW_POS = 2;
	protected static final char OUT_OF_BOUNDS_COL = 'Z';
	private static final String BACKGROUND_BUTTON_STYLE = "-fx-background-color: blue";
	
	/**
	 * DragDropController constructor.
	 * 
	 * @param buttonList An unsorted observable list of nodes containing button's.
	 * @param armada An Armada object provided by the parent controller.
	 */
	DragDropController(ObservableList<Node> buttonList, Armada armada)
	{
		this.armada = armada;
		this.buttonList = buttonList;
	}
	
	/**
	 * Public interface function for highlighting the appropriate horizontal buttons when dragging a ship
	 * across the player board during the placement stage.
	 * 
	 * @param type An ArmadaType object representing what type of ship is being moved.
	 * @param target The current contextual target button, is where the mouse is currently.
	 * @param style The custom style to be applied to button.
	 * @param size The size of the ship represented by ArmadaType.
	 */
	public void highlightHorizontal(ArmadaType type, Button target, String style, int size)
	{
		char startPos = target.getId().charAt(STANDARD_COL_POS);
		char endPos = convertToColumnLetter((startPos - CHARACTER_SHIFT) + (size - 1));
		
		logger.info("Highlighting all button's within the horizontal range.");
		
		// Iterate through each button and if on horizontal range, set the background to be highlighted
		for (int i = 0; i < TOTAL_BUTTONS; i++)
		{
			Node button = buttonList.get(i);
			if (inHorizontalRange(button.getId(), target.getId(), startPos, endPos))
			{
				button.setStyle(style);
				logger.trace("buttonID: {} is in horizontal range.", button.getId());
			}
		}
	}
	
	/**
	 * Public interface function for highlighting the appropriate vertical buttons when dragging a ship
	 * across the player board during the placement stage.
	 * 
	 * @param type An ArmadaType object representing what type of ship is being moved.
	 * @param target The current contextual target button, is where the mouse is currently.
	 * @param style The custom style to be applied to button.
	 * @param size The size of the ship represented by ArmadaType.
	 */
	public void highlightVertical(ArmadaType type, Button target, String style, int size)
	{
		int startRow = Integer.parseInt(target.getId().substring(STANDARD_ROW_POS));
		int endRow = startRow + (size - 1);
		
		logger.info("Highlighting all button's within the vertical range.");
		
		// Iterate through each button and if on vertical range, set the background to be highlighted
		for (int i = 0; i < TOTAL_BUTTONS; i++)
		{
			Node button = buttonList.get(i);
			if (inVerticalRange(button.getId(), target.getId(), startRow, endRow))
			{
				button.setStyle(style);
				logger.trace("buttonID: {} is in vertical range.", button.getId());
			}
		}
	}
	
	/**
	 * Public interface function for unhighlighting the appropriate horizontal buttons when dragging a ship
	 * across the player board during the placement stage.
	 * 
	 * @param target The current contextual target button, is where the mouse is currently.
	 * @param size The size of the ship being unhighlighted.
	 */
	public void unhighlightHorizontal(Button target, int size)
	{
		char startPos = target.getId().charAt(STANDARD_COL_POS);
		char endPos = startPos;
		
		logger.info("Unhighlighting all button's within the horizontal range.");
		
		// Sets the target node to be unhighlighted
		target.setStyle(BACKGROUND_BUTTON_STYLE);
		
		// Iterate through each button and if on horizontal range, set the background to be unhighlighted
		for (int i = 0; i < TOTAL_BUTTONS; i++)
		{
			Node button = buttonList.get(i);
			if (!inHorizontalRange(button.getId(), target.getId(), startPos, endPos))
			{
				button.setStyle(BACKGROUND_BUTTON_STYLE);
				logger.trace("buttonID: {} is not in horizontal range.", button.getId());
			}
		}
	}
	
	/**
	 * Public interface function for unhighlighting the appropriate vertical buttons when dragging a ship
	 * across the player board during the placement stage.
	 * 
	 * @param target The current contextual target button, is where the mouse is currently.
	 * @param size The size of the ship being unhighlighted.
	 */
	public void unhighlightVertical(Button target, int size)
	{
		int startRow = Integer.parseInt(target.getId().substring(STANDARD_ROW_POS));
		int endRow = startRow;
		
		logger.info("Unhighlighting all button's within the vertical range.");
		
		// Sets the target node to be unhighlighted
		target.setStyle(BACKGROUND_BUTTON_STYLE);
		
		// Iterate through each button and if on vertical range, set the background to be unhighlighted
		for (int i = 0; i < TOTAL_BUTTONS; i++)
		{
			Node button = buttonList.get(i);
			if (!inVerticalRange(button.getId(), target.getId(), startRow, endRow))
			{
				button.setStyle(BACKGROUND_BUTTON_STYLE);
				logger.trace("buttonID: {} is not in vertical range.", button.getId());
			}
		}
	}
	
	/**
	 * Public interface function for placing the appropriate horizontal buttons when dropping a ship
	 * on the player board during the placement stage.
	 * 
	 * @param type An ArmadaType object representing what type of ship is being moved.
	 * @param target The current contextual target button, is where the mouse is currently.
	 * @param style The custom style to be applied to button.
	 * @param size The size of the ship represented by ArmadaType.
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
				
				button.setDisable(true);
				button.setStyle(style);
				logger.trace("buttonID: {} is in horizontal range.", button.getId());
			}
		}
	}
	
	/**
	 * Public interface function for placing the appropriate vertical buttons when dropping a ship
	 * on the player board during the placement stage.
	 * 
	 * @param type An ArmadaType object representing what type of ship is being moved.
	 * @param target The current contextual target button, is where the mouse is currently.
	 * @param style The custom style to be applied to button.
	 * @param size The size of the ship represented by ArmadaType.
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
				
				button.setDisable(true);
				button.setStyle(style);
				logger.trace("buttonID: {} is in vertical range.", button.getId());
			}
		}
	}
	
	/**
	 * Iterative range finder that returns a boolean representing whether or not a provided button is in 
	 * the horizontal range or not.
	 * 
	 * @param buttonID String representing the current button to be checked.
	 * @param targetID String representing the current target context button to check based on.
	 * @param startPos A character representing the starting column to search.
	 * @param endPos A character representing the last column to search.
	 * @return boolean
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
			return inRange;
		}

		// while not in range and our stride has not passed the ending horizontal column position of the range
		while (charStride <= endPos && !inRange)
		{
			// Create ID string for button being strided over in range
			sb.append(targetID.charAt(0)); // Inserts initial character from the fx:id associated with target
			sb.append(charStride++); // Inserts current column being strided over and increments stride
			sb.append(targetID.substring(STANDARD_ROW_POS)); // appends the target's row value (1-10) to the end of the strideID
			
			strideID = sb.toString();
			
			logger.trace("Comparing buttonID: {} to strideID: {}.", buttonID, strideID);
			inRange = (buttonID.equalsIgnoreCase(strideID));
			
			sb.setLength(0); // Clear out old string
		}
		
		return inRange;
	}
	
	/**
	 * Iterative range finder that returns a boolean representing whether or not a provided button is in 
	 * the vertical range or not.
	 *  
	 * @param buttonID String representing the current button to be checked.
	 * @param targetID String representing the current target context button to check based on.
	 * @param startRow Integer representing the starting row to search.
	 * @param endRow Integer representing the last row to search.
	 * @return boolean
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
			return inRange;
		}
		
		// while not in range and our stride has not passed the ending vertical column position of the range
		while (rowStride <= endRow && !inRange)
		{
			// Create ID string for button being strided over in rage
			sb.append(targetID.charAt(0)); // Inserts initial character from the fx:id associated with target
			sb.append(targetID.charAt(STANDARD_COL_POS)); // Inserts the column character from the fx:id associated with target
			sb.append(rowStride++); // Inserts current row being strided over and increments stride
			
			strideID = sb.toString();
			
			logger.trace("Comparing buttonID: {} to strideID: {}.", buttonID, strideID);
			inRange = (buttonID.equalsIgnoreCase(strideID));
			
			sb.setLength(0); // Clear out old string
		}
		
		return inRange;
	}
	
	/**
	 * Adds the provided button ID into the appropriate ship inside of the armada.
	 * 
	 * @param type The type of ship in the Armada to add the id to.
	 * @param buttonID A string representing the button id.
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
	 * Converts an integer representing some column character on a battleship grid into a character.
	 * 
	 * @param col An integer representing some column character on a battleship grid.
	 * @return char
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
