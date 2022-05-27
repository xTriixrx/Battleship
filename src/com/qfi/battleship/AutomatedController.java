package com.qfi.battleship;

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
	private boolean myTurnFlag = true;
	private SecureRandom random = null;
	private ArmadaAutomator automator = null;
	private Logger logger = LogManager.getLogger(AutomatedController.class);
	
	private static final int clientTurn = 1;
	private static final int serverTurn = 2;
	private static final String HIT = "Hit";
	private static final String MISS = "Miss";
	private static final char clientSymbol = 'Z';
	private static final char serverSymbol = 'X';
	
	/**
	 * 
	 * @param whoami
	 */
	AutomatedController(int whoami)
	{
		armada = new Armada();
		turnMutex = new Object();
		automator = new ArmadaAutomator(armada);
		
		automator.automateArmadaPlacement();
		
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
		while (!shutdown)
		{
			if (getCurrentTurn() == myTurn && myTurnFlag)
			{
				makeGuess();
			}
			
			try
			{
				Thread.sleep(1000);
			}
			catch (Exception e)
			{
				logger.error(e, e);
			}
		}
	}
	
	public void makeGuess()
	{
		String guess = "O";
		int row = random.nextInt(10) + 1;
		int column = random.nextInt(10) + 1;
		char columnChar = (char) (column + 64);
		
		guess += columnChar;
		guess += row;
		guess += mySymbol;
		
		latestGuess = guess;
		
		logger.info("AUTOMATEDCONTROLLER: GUESS: {}", guess);
		
		myTurnFlag = false;
		observer.update(guess);
	}
	
	@Override
	public void update(String update)
	{
		logger.info("AUTOMATEDCONTROLLER {}: Received {}.", getID(), update);
		
		if (update.equals("SET"))
		{
			logger.info("AUTOMATEDCONTROLLER: SET");
		}
		else if (update.equals(Armada.CARRIER_NAME))
		{
			logger.info("AUTOMATEDCONTROLLER: {}", Armada.CARRIER_NAME);
		}
		else if (update.equals(Armada.BATTLESHIP_NAME))
		{
			logger.info("AUTOMATEDCONTROLLER: {}", Armada.BATTLESHIP_NAME);
		}
		else if(update.equals(Armada.CRUISER_NAME))
		{
			logger.info("AUTOMATEDCONTROLLER: {}", Armada.CRUISER_NAME);
		}
		else if(update.equals(Armada.SUBMARINE_NAME))
		{
			logger.info("AUTOMATEDCONTROLLER: {}", Armada.SUBMARINE_NAME);
		}
		else if(update.equals(Armada.DESTROYER_NAME))
		{
			logger.info("AUTOMATEDCONTROLLER: {}", Armada.DESTROYER_NAME);
		}
		else if (getCurrentTurn() == myTurn)
		{
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
			
			observer.update(Integer.toString(opponentTurn));
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
			
			if (isHit)
			{
				HorM = HIT;
			}
			else
			{
				HorM = MISS;
			}
			
			observer.update(HorM);
			setCurrentTurn(myTurn);
			myTurnFlag = true;
		}
		
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
