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

	private static final String HIT = "Hit";
	private static final int CLIENT_TURN = 1;
	private static final int SERVER_TURN = 2;
	private static final String MISS = "Miss";
	private static final int UP_DIRECTION = 2;
	private static final int LEFT_DIRECTION = -1;
	private static final int RIGHT_DIRECTION = 1;
	private static final int DOWN_DIRECTION = -2;
	private static final char CLIENT_SYMBOL = 'Z';
	private static final char SERVER_SYMBOL = 'X';
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
		logger.debug("AUTOMATEDCONTROLLER: Submitting SHIPS flag to observer to signify automated ship placement has completed.");
		observer.update("SHIPS");
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
		logger.info("AUTOMATEDCONTROLLER: GUESSED POSITIONS: " + getGuessedPositions());
		
		if (isShipInFocus())
		{
			String position = "";
			List<String> availablePositions = null;
			
			String lastPosition = latestGuess;
			
			logger.info("HIT SHIP POSITIONS: " + hitShipPositions);
			
			if (hitShipPositions.contains(lastPosition) && hitShipPositions.size() > 1) // hit multiple sections
			{
				logger.info("AUTOMATEDCONTROLLER: LAST POSITION HIT & MULTIPLE HITS");
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
				logger.info("AUTOMATEDCONTROLLER: LAST POSITION MISSED & MULTIPLE HITS");

				String firstHitPosition = hitShipPositions.get(0);
				String secondHitPosition = hitShipPositions.get(1);

				position = performDirectionPositionProtocol(firstHitPosition, firstHitPosition, secondHitPosition);
			}
			else
			{
				logger.info("AUTOMATEDCONTROLLER: CROSS POSITION");
				position = performCrossPositionProtocol(hitShipPositions.get(0));
			}

			latestGuess = position;
			submitAndAddGuess(latestGuess);
			
			return;
		}
		
		latestGuess = getRandomPosition();
		submitAndAddGuess(latestGuess);
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
		logger.info("AUTOMATEDCONTROLLER: GUESS: " + guess);

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
		
		if (column > 10 || row > 10)
		{
			return "";
		}
		
		return makePosition((char) (column + 64), row);
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
		
		logger.info("AUTOMATEDCONTROLLER: DIRECTION: {}" + direction);
		
		return direction;
	}
	
	private List<String> getCrossPositions(String focusPosition)
	{
		List<String> positions = new ArrayList<>();
		focusPosition = focusPosition.substring(1, focusPosition.length() - 1);

		int column = (focusPosition.charAt(0) - 64);
		int row = Integer.parseInt(focusPosition.substring(1));
		
		logger.debug("AUTOMATEDCONTROLLER: Captured Row: " + row + ".");
		logger.debug("AUTOMATEDCONTROLLER: Captured column: " + column + ".");
		
		// If all positions are available will return in order of 
		positions.add(makePosition((char) (column + 64), row + 1));
		positions.add(makePosition((char) (column + 64), row - 1));
		positions.add(makePosition((char) ((column + 1) + 64), row));
		positions.add(makePosition((char) ((column - 1) + 64), row));
		
		positions.removeAll(getGuessedPositions());

		logger.debug("AUTOMATEDCONTROLLER: Available cross position: " + positions + ".");
		
		return positions;
	}
	
	private String makePosition(char column, int row) {
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
			logger.trace("Attempting random position: " + position + ".");
		} while (isGuessedPosition(position));
		
		return position;
	}
	
	@Override
	public void update(String update)
	{
		logger.info("AUTOMATEDCONTROLLER " + getID() + ": Received " + update + ".");

		if (update.equals("SET"))
		{
			logger.debug("AUTOMATEDCONTROLLER: Player ships have been set!");

			synchronized (shipSetSignal)
			{
				isShipsSet = true;
				shipSetSignal.notifyAll();
			}
		}
		else if (update.equals("CONNECTED"))
		{
			synchronized (connectionSignal)
			{
				connected = true;
				connectionSignal.notifyAll();
			}
		}
		else if (update.equals(Armada.CARRIER_NAME))
		{
			logger.info("AUTOMATEDCONTROLLER: Opponent's " + Armada.CARRIER_NAME + "has sunk!");
			hitShipPositions.clear();
		}
		else if (update.equals(Armada.BATTLESHIP_NAME))
		{
			logger.info("AUTOMATEDCONTROLLER: Opponent's " + Armada.BATTLESHIP_NAME + "has sunk!");
			hitShipPositions.clear();
		}
		else if(update.equals(Armada.CRUISER_NAME))
		{
			logger.info("AUTOMATEDCONTROLLER: Opponent's " + Armada.CRUISER_NAME + "has sunk!");
			hitShipPositions.clear();
		}
		else if(update.equals(Armada.SUBMARINE_NAME))
		{
			logger.info("AUTOMATEDCONTROLLER: Opponent's " + Armada.SUBMARINE_NAME + "has sunk!");
			hitShipPositions.clear();
		}
		else if(update.equals(Armada.DESTROYER_NAME))
		{
			logger.info("AUTOMATEDCONTROLLER: Opponent's " + Armada.DESTROYER_NAME + "has sunk!");
			hitShipPositions.clear();
		}
		else if (getCurrentTurn() == myTurn && myTurnStatus())
		{
			if (update.equalsIgnoreCase(HIT) && !isShipInFocus())
			{
				setShipFocus(true);
			}
			else if (update.equalsIgnoreCase(HIT) && isShipInFocus() && hitShipPositions.isEmpty())
			{
				setShipFocus(false);
			}
			
			if (update.equalsIgnoreCase(HIT) && isShipInFocus())
			{
				hitShipPositions.add(latestGuess);
			}
			
			StringBuilder temp = new StringBuilder(latestGuess);

			String t = "";
			if (temp.length() == 4) {
				temp.deleteCharAt(3);
				t = temp.toString();
			} else if (temp.length() == 5) {
				temp.deleteCharAt(4);
				t = temp.toString();
			}
			
			// update internal picture of opponent board

			setCurrentTurn(opponentTurn);
		}
		else if (getCurrentTurn() == opponentTurn)
		{
			StringBuilder temp = new StringBuilder(update);
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
			armada.logArmadaPosition();
			
			if (isHit)
			{
				HorM = HIT;
			}
			else
			{
				HorM = MISS;
			}
			
			if (isHit)
			{
				// Cases: 1) F |= (T & F) ... 2) F |= (T & T) ... 3) T |= (F & T) 
				isCarrierSunk |= checkShipUpdate((!isCarrierSunk && armada.isCarrierSunk()), Armada.CARRIER_NAME);
				isCruiserSunk |= checkShipUpdate((!isCruiserSunk && armada.isCruiserSunk()), Armada.CRUISER_NAME);
				isSubmarineSunk |= checkShipUpdate((!isSubmarineSunk && armada.isSubmarineSunk()), Armada.SUBMARINE_NAME);
				isDestroyerSunk |= checkShipUpdate((!isDestroyerSunk && armada.isDestroyerSunk()), Armada.DESTROYER_NAME);
				isBattleshipSunk |= checkShipUpdate((!isBattleshipSunk && armada.isBattleshipSunk()), Armada.BATTLESHIP_NAME);
			}
			
			observer.update(HorM);
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
				observer.update("OVER");
			}
			else
			{
				logger.info(shipName + " SUNK");
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
	public Armada getArmada()
	{
		return armada;
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
