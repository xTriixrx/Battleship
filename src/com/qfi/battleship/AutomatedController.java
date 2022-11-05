package com.qfi.battleship;

import java.util.List;
import java.util.ArrayList;
import java.nio.ByteBuffer;
import org.apache.log4j.Logger;
import java.security.SecureRandom;
import org.apache.log4j.LogManager;

/**
 * 
 * @author Vincent.Nigro
 * @version 1.0.0
 */
public class AutomatedController implements Runnable, Observer, Observable, Controller
{
	private int myTurn = 0;
	private char mySymbol = 'A';
	private int currentTurn = 0;
	private int opponentTurn = 0;
	private Armada armada = null;
	private boolean shipSunk = false;
	private String latestGuess = "";
	private Observer observer = null;
	private boolean shutdown = false;
	private boolean shipFocus = false;
	private boolean connected = false;
	private boolean isShipsSet = false;
	private boolean myTurnFlag = false;
	private SecureRandom random = null;
	private boolean isCarrierSunk = false;
	private boolean isCruiserSunk = false;
	private boolean isSubmarineSunk = false;
	private boolean isDestroyerSunk = false;
	private boolean isBattleshipSunk = false;
	private ArmadaAutomator automator = null;
	private List<String> hitShipPositions = null;
	private List<String> guessedPositions = null;
	private final Object turnMutex = new Object();
	private final Object turnSignal = new Object();
	private final Object shipSetSignal = new Object();
	private final Object connectionSignal = new Object();

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
	 * 
	 * @param type
	 */
	AutomatedController(int type)
	{
		armada = new Armada();
		guessedPositions = new ArrayList<>();
		hitShipPositions = new ArrayList<>();

		automator = new ArmadaAutomator(armada);
		automator.automateArmadaPlacement();
		armada.logArmadaPosition();

		// byte seed for the SecureRandom object
		byte[] seed = ByteBuffer.allocate(Long.SIZE / Byte.SIZE)
			.putLong(System.currentTimeMillis()).array();
		
		// Instantiate random object
		random = new SecureRandom(seed);
		
		if (type == 1) // client
		{
			myTurn = CLIENT_TURN;
			mySymbol = CLIENT_SYMBOL;
			opponentTurn = SERVER_TURN;
		}
		else if (type == 2) // server
		{
			myTurn = SERVER_TURN;
			mySymbol = SERVER_SYMBOL;
			opponentTurn = CLIENT_TURN;
		}
	}

	/**
	 * 
	 */
	public void run()
	{
		waitForConnection();
		logger.debug(AUTOMATED_CONTROLLER_LOG_HEADER + ": Submitting SHIPS flag to observer to signify automated ship placement has completed.");
		observer.update(Message.SHIPS.getMsg());
		waitForShipSet();

		while (!shutdown)
		{
			if (getCurrentTurn() == myTurn && !myTurnStatus())
			{
				makeGuess();
				setTurnStatus(true);
			}

			waitForTurn();
		}
	}
	
	public void makeGuess()
	{
		logger.info(AUTOMATED_CONTROLLER_LOG_HEADER + ": GUESSED POSITIONS: " + getGuessedPositions());

		if (isShipInFocus())
		{
			shipSunk = false;

			String position = "";
			List<String> availablePositions = null;
			
			String lastPosition = latestGuess;
			
			logger.info("HIT SHIP POSITIONS: " + hitShipPositions);
			
			if (hitShipPositions.contains(lastPosition) && hitShipPositions.size() > 1) // hit multiple sections
			{
				logger.info(AUTOMATED_CONTROLLER_LOG_HEADER + ": LAST POSITION HIT & MULTIPLE HITS");
				String prevHitPosition = hitShipPositions.get(hitShipPositions.indexOf(lastPosition) - 1);
				
				position = getNextPosition(lastPosition,
					calculateDirection(lastPosition, prevHitPosition));

				// If next position in direction is already guessed, we've run off the hit ship
				if (isGuessedPosition(position) || position.isEmpty())
				{
					String firstHitPosition = hitShipPositions.get(0);
					String secondHitPosition = hitShipPositions.get(1);

					position = performDirectionPositionProtocol(firstHitPosition, secondHitPosition, firstHitPosition);
				}
			}
			else if (!hitShipPositions.contains(lastPosition) && hitShipPositions.size() > 1) // ran off
			{
				logger.info(AUTOMATED_CONTROLLER_LOG_HEADER + ": LAST POSITION MISSED & MULTIPLE HITS");

				String firstHitPosition = hitShipPositions.get(0);
				String secondHitPosition = hitShipPositions.get(1);

				position = performDirectionPositionProtocol(firstHitPosition, firstHitPosition, secondHitPosition);
			}
			else
			{
				logger.info(AUTOMATED_CONTROLLER_LOG_HEADER + ": CROSS POSITION");
				position = performCrossPositionProtocol(hitShipPositions.get(0));
			}

			latestGuess = position;
			submitAndAddGuess(latestGuess);
			
			return;
		}

		latestGuess = getRandomPosition();
		submitAndAddGuess(latestGuess);

		synchronized (turnSignal)
		{
			turnSignal.notifyAll();
		}
	}

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

	private void submitAndAddGuess(String guess)
	{
		logger.info(AUTOMATED_CONTROLLER_LOG_HEADER + ": GUESS: " + guess);

		// Add guess and submit to opponent
		addGuessedPosition(guess);
		observer.update(guess);
	}


	private String performCrossPositionProtocol(String lastPosition)
	{
		String position;
		List<String> availablePositions = getCrossPositions(lastPosition);

		position = availablePositions.get(random.nextInt(availablePositions.size()));

		if (position.isEmpty())
		{
			return getRandomPosition();
		}

		return position;
	}
	
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
	
	private String makePosition(char column, int row)
	{
		String position = "O";

		position += column;
		position += row;
		position += mySymbol;

		return position;
	}

	/**
	 * 
	 * @return String
	 */
	private String getRandomPosition()
	{
		int row = 0;
		int column = 0;
		char columnChar;
		String position = "";
		
		do
		{
			row = random.nextInt(10) + 1;
			column = random.nextInt(10) + 1;
			columnChar = (char) (column + 64);
			position = makePosition(columnChar, row);
			logger.trace(AUTOMATED_CONTROLLER_LOG_HEADER + ": Attempting to generate a random position: " + position + ".");
		} while (isGuessedPosition(position));
		
		return position;
	}

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
			hitShipPositions.remove(pos);
			logger.trace(AUTOMATED_CONTROLLER_LOG_HEADER + ": Removed: " + pos + " from hit ship positions map.");
		}
	}

	@Override
	public void update(String update)
	{
		logger.info(AUTOMATED_CONTROLLER_LOG_HEADER + " " + getID() + ": Received " + update + ".");

		if (update.equals(Message.SET.getMsg()))
		{
			logger.debug(AUTOMATED_CONTROLLER_LOG_HEADER + ": Player ships have been set!");

			synchronized (shipSetSignal)
			{
				isShipsSet = true;
				shipSetSignal.notifyAll();
			}
		}
		else if (update.equals(Message.CONNECTED.getMsg()))
		{
			synchronized (connectionSignal)
			{
				connected = true;
				connectionSignal.notifyAll();
			}
		}
		else if (update.contains(Message.CARRIER.getMsg()))
		{
			shipSunk = true;
			logger.info(AUTOMATED_CONTROLLER_LOG_HEADER + ": Opponent's " + Armada.CARRIER_NAME + " has sunk!");
			logger.debug(AUTOMATED_CONTROLLER_LOG_HEADER + ": " + Armada.CARRIER_NAME + ": " + update);
			updateHitPositions(update);
			logger.debug(AUTOMATED_CONTROLLER_LOG_HEADER + ": Current hit ships mapping: " + hitShipPositions);
		}
		else if (update.contains(Message.BATTLESHIP.getMsg()))
		{
			shipSunk = true;
			logger.info(AUTOMATED_CONTROLLER_LOG_HEADER + ": Opponent's " + Armada.BATTLESHIP_NAME + " has sunk!");
			logger.debug(AUTOMATED_CONTROLLER_LOG_HEADER + ": " + Armada.BATTLESHIP_NAME + ": " + update);
			updateHitPositions(update);
			logger.debug(AUTOMATED_CONTROLLER_LOG_HEADER + ": Current hit ships mapping: " + hitShipPositions);
		}
		else if (update.contains(Message.CRUISER.getMsg()))
		{
			shipSunk = true;
			logger.info(AUTOMATED_CONTROLLER_LOG_HEADER + ": Opponent's " + Armada.CRUISER_NAME + " has sunk!");
			logger.debug(AUTOMATED_CONTROLLER_LOG_HEADER + ": " + Armada.CRUISER_NAME + ": " + update);
			updateHitPositions(update);
			logger.debug(AUTOMATED_CONTROLLER_LOG_HEADER + ": Current hit ships mapping: " + hitShipPositions);
		}
		else if(update.contains(Message.SUBMARINE.getMsg()))
		{
			shipSunk = true;
			logger.info(AUTOMATED_CONTROLLER_LOG_HEADER + ": Opponent's " + Armada.SUBMARINE_NAME + " has sunk!");
			logger.debug(AUTOMATED_CONTROLLER_LOG_HEADER + ": " + Armada.SUBMARINE_NAME + ": " + update);
			updateHitPositions(update);
			logger.debug(AUTOMATED_CONTROLLER_LOG_HEADER + ": Current hit ships mapping: " + hitShipPositions);
		}
		else if (update.contains(Message.DESTROYER.getMsg()))
		{
			shipSunk = true;
			logger.info(AUTOMATED_CONTROLLER_LOG_HEADER + ": Opponent's " + Armada.DESTROYER_NAME + " has sunk!");
			logger.debug(AUTOMATED_CONTROLLER_LOG_HEADER + ": " + Armada.DESTROYER_NAME + ": " + update);
			updateHitPositions(update);
			logger.debug(AUTOMATED_CONTROLLER_LOG_HEADER + ": Current hit ships mapping: " + hitShipPositions);
		}
		else if (getCurrentTurn() == myTurn && myTurnStatus())
		{
			waitForTurn();

			if (update.equalsIgnoreCase(Message.HIT.getMsg()) && !isShipInFocus())
			{
				setShipFocus(true);
			}
			else if (isShipInFocus() && hitShipPositions.isEmpty())
			{
				setShipFocus(false);
				shipSunk = false;
			}

			if (update.equalsIgnoreCase(Message.HIT.getMsg()) && isShipInFocus() && !shipSunk)
			{
				logger.debug("Adding position " + latestGuess + " to hit positions.");
				hitShipPositions.add(latestGuess);
			}

			setCurrentTurn(opponentTurn);
		}
		else if (getCurrentTurn() == opponentTurn)
		{
			String hitOrMissMessage = "";
			StringBuilder boardPosition = new StringBuilder(update);

			// Receive a message such as 'OD5X', only need the position between the message.
			boardPosition.deleteCharAt(0);
			if (boardPosition.length() == 3)
			{
				boardPosition.deleteCharAt(2);
			}
			else if (boardPosition.length() == 4)
			{
				boardPosition.deleteCharAt(3);
			}

			logger.debug(AUTOMATED_CONTROLLER_LOG_HEADER + ": opponent has fired at position: " + boardPosition);

			boolean isHit = armada.calculateHit(boardPosition.toString());
			armada.updateArmada(boardPosition.toString());
			armada.logArmadaPosition();

			if (isHit)
			{
				hitOrMissMessage = Message.HIT.getMsg();

				// Cases: 1) F |= (T & F) ... 2) F |= (T & T) ... 3) T |= (F & T)
				isCarrierSunk |= checkShipUpdate((!isCarrierSunk && armada.isCarrierSunk()), Armada.CARRIER_NAME);
				isCruiserSunk |= checkShipUpdate((!isCruiserSunk && armada.isCruiserSunk()), Armada.CRUISER_NAME);
				isSubmarineSunk |= checkShipUpdate((!isSubmarineSunk && armada.isSubmarineSunk()), Armada.SUBMARINE_NAME);
				isDestroyerSunk |= checkShipUpdate((!isDestroyerSunk && armada.isDestroyerSunk()), Armada.DESTROYER_NAME);
				isBattleshipSunk |= checkShipUpdate((!isBattleshipSunk && armada.isBattleshipSunk()), Armada.BATTLESHIP_NAME);
			}
			else
			{
				hitOrMissMessage = Message.MISS.getMsg();
			}

			//
			observer.update(hitOrMissMessage);
			setTurnStatus(false);
			setCurrentTurn(myTurn);

			synchronized (turnSignal)
			{
				turnSignal.notifyAll();
			}
		}
	}
	
	private boolean checkShipUpdate(boolean shipSunk, String shipName)
	{
		if (shipSunk)
		{
			// If armada has sunk, the game is over
			if (armada.isArmadaSunk())
			{
				observer.update(Message.OVER.getMsg());
			}
			else
			{
				logger.info(AUTOMATED_CONTROLLER_LOG_HEADER + ": " + shipName + " has been sunk by the opponent!");
				observer.update(shipName);
			}
			
			return true;
		}
		
		return false;
	}

	private void waitForShipSet()
	{
		synchronized (shipSetSignal)
		{
			try
			{
				while (!isShipsSet)
				{
					shipSetSignal.wait();
				}
			}
			catch (InterruptedException e)
			{
				logger.error(e, e);
				Thread.currentThread().interrupt();
			}
		}
	}

	private void waitForConnection()
	{
		synchronized (connectionSignal)
		{
			try
			{
				while (!connected)
				{
					connectionSignal.wait();
				}
			}
			catch (InterruptedException e)
			{
				logger.error(e, e);
				Thread.currentThread().interrupt();
			}
		}
	}

	private void waitForTurn()
	{
		synchronized (turnSignal)
		{
			try
			{
				while (getCurrentTurn() != myTurn)
				{
					turnSignal.wait();
				}
			}
			catch (InterruptedException e)
			{
				logger.error(e, e);
				Thread.currentThread().interrupt();
			}
		}
	}
	
	private void setShipFocus(boolean focus)
	{
		synchronized (turnMutex)
		{
			shipFocus = focus;
		}
	}
	
	private boolean isShipInFocus()
	{
		boolean focus = false;
		
		synchronized (turnMutex)
		{
			focus = shipFocus;
		}
		
		return focus;
	}

	private void setTurnStatus(boolean t)
	{
		synchronized (turnMutex)
		{
			myTurnFlag = t;
		}
	}
	
	private List<String> getGuessedPositions()
	{
		List<String> copy = null;
		
		synchronized (turnMutex)
		{
			copy = new ArrayList<>(guessedPositions);
		}
		
		return copy;
	}
	
	private void addGuessedPosition(String pos)
	{
		synchronized (turnMutex)
		{
			guessedPositions.add(pos);
		}
	}
	
	private boolean isGuessedPosition(String pos)
	{
		boolean guessed = false;

		synchronized (turnMutex)
		{
			guessed = guessedPositions.contains(pos);
		}

		return guessed;
	}

	private boolean myTurnStatus()
	{
		boolean t = false;

		synchronized (turnMutex)
		{
			t = myTurnFlag;
		}

		return t;
	}
	
	@Override
	public int getID()
	{
		return System.identityHashCode(this);
	}

	@Override
	public void shutdown()
	{
		shutdown = true;
	}

	@Override
	public int getCurrentTurn()
	{
		synchronized (turnMutex)
		{
			return currentTurn;
		}
	}

	@Override
	public void setCurrentTurn(int turn)
	{
		synchronized (turnMutex)
		{
			currentTurn = turn;
		}
	}

	@Override
	public void register(Observer observer)
	{
		this.observer = observer;
	}
}
