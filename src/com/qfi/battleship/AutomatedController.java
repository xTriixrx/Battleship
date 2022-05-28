package com.qfi.battleship;

import java.util.List;
import java.util.ArrayList;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

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
	private Object turnMutex = null;
	private Observer observer = null;
	private boolean shutdown = false;
	private boolean shipFocus = false;
	private boolean myTurnFlag = false;
	private SecureRandom random = null;
	private boolean isShipsSet = false;
	private boolean isCarrierSunk = false;
	private boolean isCruiserSunk = false;
	private boolean isSubmarineSunk = false;
	private boolean isDestroyerSunk = false;
	private boolean isBattleshipSunk = false;
	private ArmadaAutomator automator = null;
	private List<String> hitShipPositions = null;
	private List<String> guessedPositions = null;
	private Logger logger = LogManager.getLogger(AutomatedController.class);
	
	private static final int clientTurn = 1;
	private static final int serverTurn = 2;
	private static final String HIT = "Hit";
	private static final String MISS = "Miss";
	private static final int UP_DIRECTION = 2;
	private static final char clientSymbol = 'Z';
	private static final char serverSymbol = 'X';
	private static final int LEFT_DIRECTION = -1;
	private static final int RIGHT_DIRECTION = 1;
	private static final int DOWN_DIRECTION = -2;
	
	/**
	 * 
	 * @param whoami
	 */
	AutomatedController(int whoami)
	{
		armada = new Armada();
		turnMutex = new Object();
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
	 * 
	 */
	public void run()
	{
		int sentAmmo = 0;
		
		while (!shutdown)
		{
			if (getCurrentTurn() == myTurn && isShipsSet && !myTurnStatus())
			{
				makeGuess(sentAmmo++);
				setTurnStatus(true);
			}
			
			try
			{
				Thread.sleep(500);
			}
			catch (Exception e)
			{
				logger.error(e, e);
			}
		}
	}
	
	public void makeGuess(int ammo)
	{
		logger.info("AUTOMATEDCONTROLLER: GUESSED POSITIONS: {}", guessedPositions);
		
		if (isShipInFocus())
		{
			String position = "";
			String lastPosition = latestGuess;
			logger.info("HIT SHIP POSITIONS: " + hitShipPositions);
			
			if (hitShipPositions.contains(lastPosition) && hitShipPositions.size() > 1) // hit multiple sections
			{
				logger.info("AUTOMATEDCONTROLLER: LAST POSITION HIT & MULTIPLE HITS");
				String prevHitPosition = hitShipPositions.get(hitShipPositions.indexOf(lastPosition) - 1);
				
				position = getNextPosition(lastPosition,
					calculateDirection(lastPosition, prevHitPosition));
				
				if (guessedPositions.contains(position))
				{
					String firstHitPosition = hitShipPositions.get(0);
					String nextHitPosition = hitShipPositions.get(hitShipPositions.indexOf(firstHitPosition) + 1);
					
					position = getNextPosition(firstHitPosition,
						calculateDirection(firstHitPosition, nextHitPosition));
				}
			}
			else if (!hitShipPositions.contains(lastPosition) && hitShipPositions.size() > 1) // ran off
			{
				logger.info("AUTOMATEDCONTROLLER: LAST POSITION MISSED & MULTIPLE HITS");
				
				String firstHitPosition = hitShipPositions.get(0);
				String nextHitPosition = hitShipPositions.get(hitShipPositions.indexOf(firstHitPosition) + 1);
				
				position = getNextPosition(firstHitPosition,
					calculateDirection(firstHitPosition, nextHitPosition));
				
				if (guessedPositions.contains(position))
				{
					position = getCrossPosition(firstHitPosition);
				}
			}
			else
			{
				logger.info("AUTOMATEDCONTROLLER: CROSS POSITION");
				position = getCrossPosition(hitShipPositions.get(0));
			}

			latestGuess = position;
			guessedPositions.add(latestGuess);
			logger.info("AUTOMATEDCONTROLLER: GUESS: {}", latestGuess);
			observer.update(latestGuess);
			
			return;
		}
		
		performRandomPositionProtocol();
	}
	
	private String getNextPosition(String contextPosition, int direction)
	{
		String position = "";
		char columnChar = 'a';
		contextPosition = contextPosition.substring(1, contextPosition.length() - 1);
		
		int column = (int) (contextPosition.charAt(0) - 64);
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
		
		columnChar = (char) (column + 64);
		
		position = "0";
		position += columnChar;
		position += row;
		position += mySymbol;
		
		return position;
	}
	
	private int calculateDirection(String p1, String p2)
	{
		int direction = 0;
		p1 = p1.substring(1, p1.length() - 1);
		p2 = p2.substring(1, p2.length() - 1);
		
		int c1 = (int) (p1.charAt(0) - 64);
		int c2 = (int) (p2.charAt(0) - 64);
		int r1 = Integer.parseInt(p1.substring(1));
		int r2 = Integer.parseInt(p2.substring(1));
		
		if (c1 > c2)
		{
			direction = RIGHT_DIRECTION;
		}
		else if (c2 > c1)
		{
			direction = LEFT_DIRECTION;
		}
		else if (r1 > r2)
		{
			direction = UP_DIRECTION;
		}
		else if (r2 > r1)
		{
			direction = DOWN_DIRECTION;
		}
		
		logger.info("AUTOMATEDCONTROLLER: DIRECTION: {}", direction);
		
		return direction;
	}
	
	private String getCrossPosition(String focusPosition)
	{
		String position = "";
		focusPosition = focusPosition.substring(1, focusPosition.length() - 1);
		
		do
		{
			int column = (int) (focusPosition.charAt(0) - 64);
			int row = Integer.parseInt(focusPosition.substring(1));
			
			logger.info("AUTOMATEDCONTROLLER: Captured Row: {}.", row);
			logger.info("AUTOMATEDCONTROLLER: Captured column: {}.", column);
			
			int upOrDown = random.nextInt(2) + 1;
			int verticalOrHorizontal = random.nextInt(2) + 1;
			
			if (verticalOrHorizontal == 1) // vertical
			{
				if (upOrDown == 1) // up
				{
					row++;
				}
				else // down
				{
					row--;
				}
			}
			else // horizontal
			{
				if (upOrDown == 1) // left column
				{
					column--;
				}
				else // right column
				{
					column++;
				}
			}
			
			char columnChar = (char) (column + 64);
			
			position = "O";
			position += columnChar;
			position += row;
			position += mySymbol;
			logger.info("Attempting random position: {}", position);
		} while (guessedPositions.contains(position));
		
		return position;
	}
	
	private void performRandomPositionProtocol()
	{
		latestGuess = getRandomPosition();
		guessedPositions.add(latestGuess);
		logger.info("AUTOMATEDCONTROLLER: GUESS: {}", latestGuess);
		observer.update(latestGuess);
	}
	
	private String getRandomPosition()
	{
		int row = 0;
		int column = 0;
		String position = "";
		char columnChar = 'a';
		
		do
		{
			row = random.nextInt(10) + 1;
			column = random.nextInt(10) + 1;
			columnChar = (char) (column + 64);
			
			position = "O";
			position += columnChar;
			position += row;
			position += mySymbol;
			logger.trace("Attempting random position: {}", position);
		} while (guessedPositions.contains(position));
		
		return position;
	}
	
	@Override
	public void update(String update)
	{
		logger.info("AUTOMATEDCONTROLLER {}: Received {}.", getID(), update);
		
		if (update.equals("SET"))
		{
			logger.info("AUTOMATEDCONTROLLER: SET");
			isShipsSet = true;
		}
		else if (update.equals(Armada.CARRIER_NAME))
		{
			logger.info("AUTOMATEDCONTROLLER: {}", Armada.CARRIER_NAME);
			hitShipPositions.clear();
		}
		else if (update.equals(Armada.BATTLESHIP_NAME))
		{
			logger.info("AUTOMATEDCONTROLLER: {}", Armada.BATTLESHIP_NAME);
			hitShipPositions.clear();
		}
		else if(update.equals(Armada.CRUISER_NAME))
		{
			logger.info("AUTOMATEDCONTROLLER: {}", Armada.CRUISER_NAME);
			hitShipPositions.clear();
		}
		else if(update.equals(Armada.SUBMARINE_NAME))
		{
			logger.info("AUTOMATEDCONTROLLER: {}", Armada.SUBMARINE_NAME);
			hitShipPositions.clear();
		}
		else if(update.equals(Armada.DESTROYER_NAME))
		{
			logger.info("AUTOMATEDCONTROLLER: {}", Armada.DESTROYER_NAME);
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
				isCarrierSunk |= checkShipUpdate((!isCarrierSunk & armada.isCarrierSunk()), Armada.CARRIER_NAME);
				isCruiserSunk |= checkShipUpdate((!isCruiserSunk & armada.isCruiserSunk()), Armada.CRUISER_NAME);
				isSubmarineSunk |= checkShipUpdate((!isSubmarineSunk & armada.isSubmarineSunk()), Armada.SUBMARINE_NAME);
				isDestroyerSunk |= checkShipUpdate((!isDestroyerSunk & armada.isDestroyerSunk()), Armada.DESTROYER_NAME);
				isBattleshipSunk |= checkShipUpdate((!isBattleshipSunk & armada.isBattleshipSunk()), Armada.BATTLESHIP_NAME);
			}
			
			observer.update(HorM);
			setTurnStatus(false);
			setCurrentTurn(myTurn);
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
				logger.info("{} SUNK", shipName);
				observer.update(shipName);
			}
			
			return true;
		}
		
		return false;
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
		observer.update("SHUTDOWN");
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
