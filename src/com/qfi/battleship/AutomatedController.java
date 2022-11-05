package com.qfi.battleship;

import java.util.List;
import java.util.ArrayList;
import java.nio.ByteBuffer;
import org.apache.log4j.Logger;
import java.security.SecureRandom;
import org.apache.log4j.LogManager;

/**
 * The AutomatedController object is an automated opponent which manages its own armada and response back
 * to an opponent through its own Player object instance. The AutomatedController and Player instances
 * will communicate through the game submitting each other messages about status of the game and relay
 * messages to and from the opponent.
 *
 * @author Vincent.Nigro
 * @version 1.0.0
 */
public class AutomatedController implements Runnable, Observer, Observable, Controller
{
	private int m_myTurn = 0;
	private final Armada m_armada;
	private char m_mySymbol = 'A';
	private int m_currentTurn = 0;
	private int m_opponentTurn = 0;
	private String m_latestGuess = "";
	private boolean m_shipSunk = false;
	private Observer m_observer = null;
	private boolean m_shutdown = false;
	private final SecureRandom m_random;
	private boolean m_shipFocus = false;
	private boolean m_connected = false;
	private boolean m_isShipsSet = false;
	private boolean m_myTurnFlag = false;
	private boolean m_isCarrierSunk = false;
	private boolean m_isCruiserSunk = false;
	private boolean m_isSubmarineSunk = false;
	private boolean m_isDestroyerSunk = false;
	private boolean m_isBattleshipSunk = false;
	private final List<String> m_hitShipPositions;
	private final List <String> m_guessedPositions;
	private final Object m_turnMutex = new Object();
	private final Object m_turnSignal = new Object();
	private final Object m_shipSetSignal = new Object();
	private final Object m_connectionSignal = new Object();

	private static final int MIN_ROW = 1;
	private static final int MAX_ROW = 10;
	private static final int CLIENT_TURN = 1;
	private static final int SERVER_TURN = 2;
	private static final int UP_DIRECTION = 2;
	private static final char MAX_COLUMN = 'J';
	private static final char MIN_COLUMN = 'A';
	private static final int LEFT_DIRECTION = -1;
	private static final int RIGHT_DIRECTION = 1;
	private static final int DOWN_DIRECTION = -2;
	private static final char CLIENT_SYMBOL = 'Z';
	private static final char SERVER_SYMBOL = 'X';
	private static final String AUTOMATED_CONTROLLER_LOG_HEADER = "AUTOMATEDCONTROLLER";
	private static final Logger logger = LogManager.getLogger(AutomatedController.class);
	
	/**
	 * AutomatedController constructor.
	 *
	 * @param type - The type of player this controller is associated with.
	 */
	AutomatedController(int type)
	{
		m_armada = new Armada();
		m_guessedPositions = new ArrayList<>();
		m_hitShipPositions = new ArrayList<>();
		ArmadaAutomator automator = new ArmadaAutomator(m_armada);

		automator.automateArmadaPlacement();
		m_armada.logArmadaPosition();

		// byte seed for the SecureRandom object
		byte[] seed = ByteBuffer.allocate(Long.SIZE / Byte.SIZE)
			.putLong(System.currentTimeMillis()).array();
		
		// Instantiate m_random object
		m_random = new SecureRandom(seed);
		
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
	 * The main automated game loop that will continue making guesses until the game is over.
	 */
	public void run()
	{
		// Wait for "CONNECTED" message from observer
		waitForConnection();

		logger.debug(AUTOMATED_CONTROLLER_LOG_HEADER + ": Submitting SHIPS flag to m_observer to signify automated ship placement has completed.");
		m_observer.update(Message.SHIPS.getMsg());

		// Wait for opponents "SET" message representing ships have been set.
		waitForShipSet();

		// While the game is active, continue making guesses and wait until it is the automated controllers' turn.
		while (!m_shutdown)
		{
			if (getCurrentTurn() == m_myTurn && !myTurnStatus())
			{
				makeGuess();
				setTurnStatus(true);
			}

			waitForTurn();
		}
	}

	/**
	 * Submits a guess to the opponent and adds the guess to internal tracking.
	 *
	 * @param guess - A position representing some guess that will be submitted to the opponent.
	 */
	private void submitAndAddGuess(String guess)
	{
		logger.info(AUTOMATED_CONTROLLER_LOG_HEADER + ": GUESS: " + guess);

		// Add guess and submit to opponent
		addGuessedPosition(guess);
		m_observer.update(guess);
	}

	/**
	 * This method generates and submits a guess to the opponent based off of the current state being tracked
	 * by the automated controller.
	 */
	public void makeGuess()
	{
		logger.info(AUTOMATED_CONTROLLER_LOG_HEADER + ": GUESSED POSITIONS: " + getGuessedPositions());

		if (isShipInFocus())
		{
			String position;
			m_shipSunk = false;
			
			String lastPosition = m_latestGuess;
			
			logger.debug("HIT SHIP POSITIONS: " + m_hitShipPositions);

			// If multiple sections have been hit, attempt to go down that direction
			if (m_hitShipPositions.contains(lastPosition) && m_hitShipPositions.size() > 1)
			{
				logger.debug(AUTOMATED_CONTROLLER_LOG_HEADER + ": LAST POSITION HIT & MULTIPLE HITS");
				String prevHitPosition = m_hitShipPositions.get(m_hitShipPositions.indexOf(lastPosition) - 1);
				
				position = getNextPosition(lastPosition,
					calculateDirection(lastPosition, prevHitPosition));

				// If next position in direction is already guessed or empty, we've run off the hit ship
				if (isGuessedPosition(position) || position.isEmpty())
				{
					position = runOffProtocol();
				}
			}
			// If last guess was missed with multiple hits, we have ran off the target
			else if (!m_hitShipPositions.contains(lastPosition) && m_hitShipPositions.size() > 1) // ran off
			{
				logger.debug(AUTOMATED_CONTROLLER_LOG_HEADER + ": LAST POSITION MISSED & MULTIPLE HITS");
				position = runOffProtocol();
			}
			else
			{
				// Only one position has been hit, perform a cross pattern guess
				logger.debug(AUTOMATED_CONTROLLER_LOG_HEADER + ": CROSS POSITION");
				position = performCrossPositionProtocol(m_hitShipPositions.get(0));
			}

			m_latestGuess = position;
			submitAndAddGuess(m_latestGuess);
			
			return;
		}

		// Perform a random guess
		m_latestGuess = getRandomPosition();
		submitAndAddGuess(m_latestGuess);

		synchronized (m_turnSignal)
		{
			m_turnSignal.notifyAll();
		}
	}

	/**
	 * An interface method for the player to notify the controller about game status & submit opponent responses.
	 *
	 * @param observableMessage - A string based message that contains a message from either the player instance or opponent.
	 */
	@Override
	public void update(String observableMessage)
	{
		logger.info(AUTOMATED_CONTROLLER_LOG_HEADER + " " + getID() + ": Received " + observableMessage + ".");

		if (observableMessage.equals(Message.SET.getMsg()))
		{
			logger.debug(AUTOMATED_CONTROLLER_LOG_HEADER + ": Player ships have been set!");

			synchronized (m_shipSetSignal)
			{
				m_isShipsSet = true;
				m_shipSetSignal.notifyAll();
			}
		}
		else if (observableMessage.equals(Message.CONNECTED.getMsg()))
		{
			synchronized (m_connectionSignal)
			{
				m_connected = true;
				m_connectionSignal.notifyAll();
			}
		}
		else if (observableMessage.contains(Message.CARRIER.getMsg()) ||
				 observableMessage.contains(Message.CRUISER.getMsg()) ||
				 observableMessage.contains(Message.DESTROYER.getMsg()) ||
				 observableMessage.contains(Message.SUBMARINE.getMsg()) ||
				 observableMessage.contains(Message.BATTLESHIP.getMsg()))
		{
			m_shipSunk = true;
			String shipName = observableMessage.split("\\s+")[0];
			logger.info(AUTOMATED_CONTROLLER_LOG_HEADER + ": Opponent's " + shipName + " has sunk!");
			logger.debug(AUTOMATED_CONTROLLER_LOG_HEADER + ": " + shipName + ": " + observableMessage);
			updateHitPositions(observableMessage);
			logger.debug(AUTOMATED_CONTROLLER_LOG_HEADER + ": Current hit ships mapping: " + m_hitShipPositions);
		}
		else if (getCurrentTurn() == m_myTurn && myTurnStatus())
		{
			automatedPlayerProtocol(observableMessage);
		}
		else if (getCurrentTurn() == m_opponentTurn)
		{
			opponentGuessProtocol(observableMessage);
		}
	}

	/**
	 * After the automated player has submitted its own guess, the player instance managing the automated
	 * controller will receive an update from the opponent, and if it is the automated controller's turn, then
	 * it must be either a "HIT" or a "MISS" message based on the automated guess. Based on that information,
	 * the automated controller will update its internal tracking of hit positions and state tracking to help
	 * with the next automated guess.
	 *
	 * @param opponentMessage - A string based message "HIT" or "MISS" sent by the opponent.
	 */
	private void automatedPlayerProtocol(String opponentMessage)
	{
		waitForTurn();

		// Set the ship focus if you just hit a position
		if (opponentMessage.equalsIgnoreCase(Message.HIT.getMsg()) && !isShipInFocus())
		{
			setShipFocus(true);
		}
		else if (isShipInFocus() && m_hitShipPositions.isEmpty()) // turn off ship focus if no more ships are tracked
		{
			setShipFocus(false);
			m_shipSunk = false;
		}

		// If a ship has been hit and not sunk, add the hit to the hit tracking list
		if (opponentMessage.equalsIgnoreCase(Message.HIT.getMsg()) && isShipInFocus() && !m_shipSunk)
		{
			logger.debug("Adding position " + m_latestGuess + " to hit positions.");
			m_hitShipPositions.add(m_latestGuess);
		}

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
		String opponentGuess;
		String hitOrMissMessage;
		StringBuilder opponentBoardGuess = new StringBuilder(opponentMessage);

		// Receive a message such as 'OD5X', only need the position between the message.
		opponentBoardGuess.deleteCharAt(0);
		if (opponentBoardGuess.length() == 3)
		{
			opponentBoardGuess.deleteCharAt(2);
		}
		else if (opponentBoardGuess.length() == 4)
		{
			opponentBoardGuess.deleteCharAt(3);
		}

		opponentGuess = opponentBoardGuess.toString();
		logger.debug(AUTOMATED_CONTROLLER_LOG_HEADER + ": opponent has fired at position: " + opponentGuess);

		// Determine if opponent guess was a hit or miss
		boolean isHit = m_armada.calculateHit(opponentGuess);
		m_armada.updateArmada(opponentGuess);
		m_armada.logArmadaPosition();

		if (isHit)
		{
			hitOrMissMessage = Message.HIT.getMsg();

			// Submits a message to the observer/opponent if a ship has sunk
			m_isCarrierSunk |= checkShipUpdate((!m_isCarrierSunk && m_armada.isCarrierSunk()), Armada.CARRIER_NAME);
			m_isCruiserSunk |= checkShipUpdate((!m_isCruiserSunk && m_armada.isCruiserSunk()), Armada.CRUISER_NAME);
			m_isSubmarineSunk |= checkShipUpdate((!m_isSubmarineSunk && m_armada.isSubmarineSunk()), Armada.SUBMARINE_NAME);
			m_isDestroyerSunk |= checkShipUpdate((!m_isDestroyerSunk && m_armada.isDestroyerSunk()), Armada.DESTROYER_NAME);
			m_isBattleshipSunk |= checkShipUpdate((!m_isBattleshipSunk && m_armada.isBattleshipSunk()), Armada.BATTLESHIP_NAME);
		}
		else
		{
			hitOrMissMessage = Message.MISS.getMsg();
		}

		// Submit hit or miss to opponent.
		m_observer.update(hitOrMissMessage);
		setTurnStatus(false);
		setCurrentTurn(m_myTurn);

		synchronized (m_turnSignal)
		{
			m_turnSignal.notifyAll();
		}
	}

	/**
	 * This protocol gets the next guess based on the opposite direction when determined to have run off a multiple
	 * hit scenario.
	 *
	 * @return String - Some guess position based on the opposite direction of the initial run.
	 */
	private String runOffProtocol()
	{
		String firstHitPosition = m_hitShipPositions.get(0);
		String secondHitPosition = m_hitShipPositions.get(1);

		// Get the position from the opposite direction from the first position context but inverted direction
		return performDirectionPositionProtocol(firstHitPosition, secondHitPosition, firstHitPosition);
	}

	/**
	 * This protocol gets the next guess based on the direction information provided.
	 *
	 * @param posContext - The current context for the position.
	 * @param startPos - A position representing the start point.
	 * @param endPos - A position representing the end point.
	 * @return - Some position to perform the next guess.
	 */
	private String performDirectionPositionProtocol(String posContext, String startPos, String endPos)
	{
		String position = getNextPosition(posContext,
				calculateDirection(startPos, endPos));

		if (isGuessedPosition(position) || position.isEmpty())
		{
			position = performCrossPositionProtocol(endPos);
		}

		return position;
	}

	/**
	 * This protocol gets the possible cross position guesses and chooses one of the positions at random.
	 *
	 * @param lastPosition - The last guessed position that was a hit.
	 * @return String - Some position to perform the next guess.
	 */
	private String performCrossPositionProtocol(String lastPosition)
	{
		String position;
		List<String> availablePositions = getCrossPositions(lastPosition);

		position = availablePositions.get(m_random.nextInt(availablePositions.size()));

		if (position.isEmpty())
		{
			return getRandomPosition();
		}

		return position;
	}

	/**
	 * This method will generate a random guess for some position that has not already been guessed.
	 *
	 * @return String - Returns some randomized position within the grid.
	 */
	private String getRandomPosition()
	{
		int row = 0;
		int column = 0;
		char columnChar;
		String position = "";

		do
		{
			row = m_random.nextInt(10) + 1;
			column = m_random.nextInt(10) + 1;
			columnChar = (char) (column + 64);
			position = makePosition(columnChar, row);
			logger.trace(AUTOMATED_CONTROLLER_LOG_HEADER + ": Attempting to generate a m_random position: " + position + ".");
		} while (isGuessedPosition(position));

		return position;
	}

	/**
	 * This method generates a position guess based on the context position provided and the direction.
	 *
	 * @param contextPosition - The position for where the next guess should be based off of.
	 * @param direction - The direction of where the next guess should be.
	 * @return String - A position representing the next guess based on the direction and context position.
	 */
	private String getNextPosition(String contextPosition, int direction)
	{
		contextPosition = contextPosition.substring(1, contextPosition.length() - 1);

		String position;
		int column = (contextPosition.charAt(0) - 64);
		int row = Integer.parseInt(contextPosition.substring(1));

		if (direction == LEFT_DIRECTION)
		{
			column--;
		}
		else if (direction == RIGHT_DIRECTION)
		{
			column++;
		}
		else if (direction == UP_DIRECTION)
		{
			row++;
		}
		else if (direction == DOWN_DIRECTION)
		{
			row--;
		}

		position = makePosition((char) (column + 64), row);

		if (outBoundsPosition(position))
		{
			return "";
		}

		return position;
	}

	/**
	 * Calculates the direction (UP, DOWN, LEFT, RIGHT) of a given start to end position.
	 *
	 * @param start - A position representing the start point.
	 * @param end - A position representing the end point.
	 * @return int - An integer representation of the direction.
	 */
	private int calculateDirection(String start, String end)
	{
		int direction = 0;
		end = end.substring(1, end.length() - 1);
		start = start.substring(1, start.length() - 1);
		
		int endColumn = (end.charAt(0) - 64);
		int startColumn = (start.charAt(0) - 64);
		int endRow = Integer.parseInt(end.substring(1));
		int startRow = Integer.parseInt(start.substring(1));
		
		if (startColumn > endColumn)
		{
			direction = RIGHT_DIRECTION;
		}
		else if (endColumn > startColumn)
		{
			direction = LEFT_DIRECTION;
		}
		else if (startRow > endRow)
		{
			direction = UP_DIRECTION;
		}
		else if (endRow > startRow)
		{
			direction = DOWN_DIRECTION;
		}
		
		logger.info(AUTOMATED_CONTROLLER_LOG_HEADER + ": DIRECTION: " + direction);
		
		return direction;
	}

	/**
	 * This method will return a list of available positions for guess selection based on some focus position.
	 *
	 * @param focusPosition - The position on the board that is in focus.
	 * @return {@code List<String>} - A list of valid positions that are within the cross pattern.
	 */
	private List<String> getCrossPositions(String focusPosition)
	{
		List<String> positions = new ArrayList<>();
		focusPosition = focusPosition.substring(1, focusPosition.length() - 1);

		int column = (focusPosition.charAt(0) - 64);
		int row = Integer.parseInt(focusPosition.substring(1));
		
		logger.debug(AUTOMATED_CONTROLLER_LOG_HEADER + ": Calculating cross positions for position: " +
				((char) (column + 64)) + row + ".");
		
		// If all positions are available will return in order of 
		positions.add(makePosition((char) (column + 64), row + 1));
		positions.add(makePosition((char) (column + 64), row - 1));
		positions.add(makePosition((char) ((column + 1) + 64), row));
		positions.add(makePosition((char) ((column - 1) + 64), row));
		
		positions.removeAll(getGuessedPositions());
		positions.removeAll(outBoundsPositions(positions));

		logger.debug(AUTOMATED_CONTROLLER_LOG_HEADER + ": Available cross positions: " + positions + ".");
		
		return positions;
	}

	/**
	 * This method will iterate through each possible position, determine if it is out of bounds, and if so add
	 * it to a list of out of bound positions. This list of positions will be returned to the caller.
	 *
	 * @param possiblePositions - A set of possible positions that may be out of bounds.
	 * @return {@code List<String>} - A list of all the positions that are out of bounds from the set of possible positions.
	 */
	private List<String> outBoundsPositions(List<String> possiblePositions)
	{
		List<String> outboundPositions = new ArrayList<>();

		for (String pos : possiblePositions)
		{
			if (outBoundsPosition(pos))
			{
				outboundPositions.add(pos);
			}
		}

		return outboundPositions;
	}

	/**
	 * A boolish method to determine if a given position is out of bounds or not.
	 *
	 * @param position - A full board position containing some column and row.
	 * @return boolean - A flag representing whether the provided position is out of bounds.
	 */
	private boolean outBoundsPosition(String position)
	{
		int row;
		char column = position.charAt(1);

		// If row position is a row between 1-9 inclusively
		if (position.length() == 4)
		{
			row = Integer.parseInt(position.substring(2, 3));
		}
		else
		{
			row = Integer.parseInt(position.substring(2, 4));
		}

		logger.trace(AUTOMATED_CONTROLLER_LOG_HEADER + ": Possible Out of Bounds Position: " + column + row);

		if (row > MAX_ROW || row < MIN_ROW ||
				column > MAX_COLUMN || column < MIN_COLUMN)
		{
			logger.trace(AUTOMATED_CONTROLLER_LOG_HEADER + ": Position " + position + " is out of bounds!");
			return true;
		}

		return false;
	}

	/**
	 * Creates an opponent's position based on some column and row.
	 *
	 * @param column - A character representing some column.
	 * @param row - A number representing some row.
	 * @return String - The positions representing an opponents' position.
	 */
	private String makePosition(char column, int row)
	{
		String position = "O";

		position += column;
		position += row;
		position += m_mySymbol;

		return position;
	}

	/**
	 * This method is called during the update method if an opponents' ship has been determined to be sunk.
	 * This method will use the positions returned by the opponent to update the internal hit ship positions
	 * list in order to remove sunken positions.
	 *
	 * @param positionsContext - Some message containing positions of a sunken ship.
	 */
	private void updateHitPositions(String positionsContext)
	{
		// Removes the sunk ship name from the position context string update sent by opponent
		// Ex: CRUISER I2 I3 I4 -> [I2, I3, I4]
		String positionsText = positionsContext.substring(positionsContext.indexOf(" ") + 1);
		List<String> positions = List.of(positionsText.split("\\s+"));

		for (String position : positions)
		{
			int row;
			char column = position.charAt(0);

			// If row position is a row between 1-9 inclusively
			if (position.length() == 2)
			{
				row = Integer.parseInt(position.substring(1, 2));
			}
			else
			{
				row = Integer.parseInt(position.substring(1, 3));
			}

			String pos = makePosition(column, row);
			m_hitShipPositions.remove(pos);
			logger.trace(AUTOMATED_CONTROLLER_LOG_HEADER + ": Removed: " + pos + " from hit ship positions map.");
		}
	}

	/**
	 * Checks whether a ship has sunk or not and if so, the player instance is updated to send a message to the
	 * opponent signifying that the ship has sunk.
	 *
	 * @param shipSunk - A flag representing whether the ship has sunk or not.
	 * @param shipName - The name of the ship that may have sunk.
	 * @return boolean - A flag representing whether the ship sunk and a message was sent.
	 */
	private boolean checkShipUpdate(boolean shipSunk, String shipName)
	{
		if (shipSunk)
		{
			// If m_armada has sunk, the game is over
			if (m_armada.isArmadaSunk())
			{
				m_observer.update(Message.OVER.getMsg());
			}
			else
			{
				logger.info(AUTOMATED_CONTROLLER_LOG_HEADER + ": " + shipName + " has been sunk by the opponent!");
				m_observer.update(shipName);
			}
			
			return true;
		}
		
		return false;
	}

	/**
	 * A synchronizing method that will block until the "SET" message has been received signaling the opponents'
	 * ships have been set.
	 */
	private void waitForShipSet()
	{
		synchronized (m_shipSetSignal)
		{
			try
			{
				while (!m_isShipsSet)
				{
					m_shipSetSignal.wait();
				}
			}
			catch (InterruptedException e)
			{
				logger.error(e, e);
				Thread.currentThread().interrupt();
			}
		}
	}

	/**
	 * A synchronizing method that will block until the "CONNECTED" message has been received by the opponent.
	 */
	private void waitForConnection()
	{
		synchronized (m_connectionSignal)
		{
			try
			{
				while (!m_connected)
				{
					m_connectionSignal.wait();
				}
			}
			catch (InterruptedException e)
			{
				logger.error(e, e);
				Thread.currentThread().interrupt();
			}
		}
	}

	/**
	 * A synchronizing method that will block until it is time for the automated controller to resume its turn.
	 */
	private void waitForTurn()
	{
		synchronized (m_turnSignal)
		{
			try
			{
				while (getCurrentTurn() != m_myTurn)
				{
					m_turnSignal.wait();
				}
			}
			catch (InterruptedException e)
			{
				logger.error(e, e);
				Thread.currentThread().interrupt();
			}
		}
	}

	/**
	 * Accessor that creates a copy of the currently guessed positions.
	 *
	 * @return {@code List<String>} - Returns a list of string based positions.
	 */
	private List<String> getGuessedPositions()
	{
		List<String> copy = null;
		
		synchronized (m_turnMutex)
		{
			copy = new ArrayList<>(m_guessedPositions);
		}
		
		return copy;
	}

	/**
	 * Sets whether a ship is in focus.
	 *
	 * @param focus - A boolean representing whether a ship is in focus.
	 */
	private void setShipFocus(boolean focus)
	{
		synchronized (m_turnMutex)
		{
			m_shipFocus = focus;
		}
	}

	/**
	 * A boolish function representing whether a ship is in focus or not.
	 *
	 * @return - A boolean representing whether a ship is in focus or not.
	 */
	private boolean isShipInFocus()
	{
		boolean focus = false;

		synchronized (m_turnMutex)
		{
			focus = m_shipFocus;
		}

		return focus;
	}

	/**
	 * Adds a position into the set of guessed positions.
	 *
	 * @param pos - A position that has been guessed by the automated controller.
	 */
	private void addGuessedPosition(String pos)
	{
		synchronized (m_turnMutex)
		{
			m_guessedPositions.add(pos);
		}
	}

	/**
	 * A boolish function representing whether the position has been guessed or not.
	 *
	 * @param pos - Some string representing a position.
	 * @return - A boolean representing whether the position has been guessed.
	 */
	private boolean isGuessedPosition(String pos)
	{
		boolean guessed = false;

		synchronized (m_turnMutex)
		{
			guessed = m_guessedPositions.contains(pos);
		}

		return guessed;
	}

	/**
	 * Sets the turns status flag.
	 *
	 * @param t - A boolean representing the status of whether the turn has been set or not.
	 */
	private void setTurnStatus(boolean t)
	{
		synchronized (m_turnMutex)
		{
			m_myTurnFlag = t;
		}
	}

	/**
	 * Gets the flag representing whether my turn status is tru or false.
	 *
	 * @return boolean - A flag representing whether my turn has been set or not.
	 */
	private boolean myTurnStatus()
	{
		boolean t = false;

		synchronized (m_turnMutex)
		{
			t = m_myTurnFlag;
		}

		return t;
	}

	/**
	 * Sets the current turn.
	 *
	 * @param turn - The turn for which the internal turn value should be set to.
	 */
	@Override
	public void setCurrentTurn(int turn)
	{
		synchronized (m_turnMutex)
		{
			m_currentTurn = turn;
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
		synchronized (m_turnMutex)
		{
			return m_currentTurn;
		}
	}

	/**
	 * Called to shut down the automated controller.
	 */
	@Override
	public void shutdown()
	{
		m_shutdown = true;
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
	@Override
	public int getID()
	{
		return System.identityHashCode(this);
	}
}
