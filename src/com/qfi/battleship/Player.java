package com.qfi.battleship;

import java.net.Socket;
import java.nio.ByteBuffer;
import java.net.InetAddress;
import java.net.ServerSocket;
import javax.swing.JOptionPane;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.security.SecureRandom;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * 
 * @author Vincent.Nigro
 * @version 1.0.0
 */
public class Player implements Runnable, Observable, Observer
{
	private int port = 0;
	private int myID = 0;
	private int lineCount = 0;
	private String myName = "";
	private String address = "";
	private int currentTurn = 0;
	private boolean over = false;
	private Socket socket = null;
	private Observer observer = null;
	private boolean ShipsSet = false;
	private DataInputStream in = null;
	private SecureRandom random = null;
	private ServerSocket server = null;
	private boolean CarrierSet = false;
	private boolean CruiserSet = false;
	private DataOutputStream out = null;
	private boolean SubmarineSet = false;
	private boolean DestroyerSet = false;
	private boolean BattleshipSet = false;
	private BoardController controller = null;
	private Logger logger = LogManager.getLogger(Player.class);

	public static final int CLIENT_ID = 1;
	public static final int SERVER_ID = 2;
	public static final int MAX_PLAYERS = 2;
	public static final String SERVER = "server";
	public static final String CLIENT = "client";

	/**
	 * 
	 * @param whoami
	 * @param address
	 * @param port
	 */
	public Player(int whoami, String address, int port)
	{
		if (whoami == 1) // client
		{
			myID = CLIENT_ID;
			myName = toUppercase(CLIENT);
		}
		else if (whoami == 2) // server
		{
			myID = SERVER_ID;
			myName = toUppercase(SERVER);
		}

		this.port = port;
		this.address = address;
		
		// 
		controller = new BoardController(myID);
		controller.register((Observer)this);
		register(controller);

		// byte seed for the SecureRandom object
		byte[] seed = ByteBuffer.allocate(Long.SIZE / Byte.SIZE)
			.putLong(System.currentTimeMillis()).array();
		
		// Instantiate random object
		random = new SecureRandom(seed);
		
		if (myID == SERVER_ID)
		{
			currentTurn = random.nextInt(MAX_PLAYERS) + 1;
			controller.setCurrentTurn(currentTurn);
		}

		logger.debug("Controller ID: " + controller.getID());
	}

	/**
	 * 
	 */
	@Override
	public void run()
	{
		initializeConnection();
		initializeStreams();

		// string to read message from input 
		String message = "";

		while (!over)
		{
			try
			{
				// If no lines have been read, the initial handshake needs to take place
				if (lineCount == 0)
				{
					performHandshake();
				}
				else // Process message by opponent
				{
					message = in.readUTF();
					logger.debug("Player received '{}' message from opponent.", message);
					received(message);
					observer.update(message);
				}
			}
			catch (Exception e)
			{
				logger.error(e, e);
			}
		}

		destroy();
	}
	
	/**
	 * 
	 */
	private void performHandshake()
	{
		if (myID == SERVER_ID) // server
		{
			performServerHandshakeProtocol();
		}
		else if (myID == CLIENT_ID) // client
		{
			performClientHandshakeProtocol();
		}
	}
	
	/**
	 * 
	 */
	private void performServerHandshakeProtocol()
	{
		String line = "";
		
		try
		{
			out.writeInt(currentTurn);
			out.flush();
			line = in.readUTF(); 
			logger.info("Server received: {} from client.", line);
			lineCount++;
			isShipsSet(false);
			ShipsSet = true;
		}
		catch (Exception e)
		{
			logger.error(e, e);
		}
	}
	
	/**
	 * 
	 */
	private void performClientHandshakeProtocol()
	{
		try
		{
			currentTurn = in.readInt();
			out.writeUTF("Client received " + currentTurn + " from Server...");
			out.flush();
			controller.setCurrentTurn(currentTurn);
			lineCount++;
			isShipsSet(false);
			ShipsSet = true;
		}
		catch (Exception e)
		{
			logger.error(e, e);
		}
	}
	
	/**
	 * 
	 */
	private void initializeConnection()
	{
		if (myID == SERVER_ID)
		{
			initializeServerSocket();
		}
		else if (myID == CLIENT_ID)
		{
			connectToServer();
		}
	}
	
	/**
	 * 
	 */
	private void initializeStreams()
	{
		try
		{
			// takes input from the client socket 
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
		}
		catch (Exception e)
		{
			logger.error(e, e);
		}
	}
	
	/**
	 * 
	 */
	private void initializeServerSocket()
	{
		try
		{
			server = new ServerSocket(port);
			logger.info("Local Address: {}", ServerAddress());
			logger.info("Waiting for a client to connect.");
			socket = server.accept(); 
			logger.info("Client accepted."); 
		}
		catch (Exception e)
		{
			logger.error(e, e);
		}
	}
	
	/**
	 * 
	 */
	private void connectToServer()
	{
		try
		{
			socket = new Socket(address, port); 
			logger.info("Connected"); 
		}
		catch (Exception e)
		{
			logger.error(e, e);
		}
	}

	@Override
	public void update(String s)
	{
		logger.info("Received {} from observable controller.", s);
		StringBuilder t = new StringBuilder(s);
		updateResponse(s, t);
	}

	/**
	 * 
	 * @param l
	 */
	public void received(String opponentMessage)
	{
		if (opponentMessage.equalsIgnoreCase("SHUTDOWN"))
		{
			System.exit(0);
		}
		else if(opponentMessage.equals("OVER"))
		{
			infoBox("You Won! (:", "Player " + myID);
			
			try
			{
				Thread.sleep(1000);
				System.exit(0);
			}
			catch (Exception e)
			{
				logger.error(e, e);
			}
		}
		else if(opponentMessage.equals("CARRIER"))
		{
			infoBox("You sunk your opponents Carrier!", "Player " + myID);
		}
		else if(opponentMessage.equals("BATTLESHIP"))
		{
			infoBox("You sunk your opponents Battleship!", "Player " + myID);
		}
		else if(opponentMessage.equals("CRUISER"))
		{
			infoBox("You sunk your opponents Cruiser!", "Player " + myID);
		}
		else if(opponentMessage.equals("SUBMARINE"))
		{
			infoBox("You sunk your opponents Submarine!", "Player " + myID);
		}
		else if(opponentMessage.equals("DESTROYER"))
		{
			infoBox("You sunk your opponents Destroyer!", "Player " + myID);
		}
	}

	/**
	 * 
	 * @param str
	 * @param build
	 */
	public void updateResponse(String str, StringBuilder build)
	{
		if (str.equalsIgnoreCase("SHUTDOWN"))
		{
			try
			{
				out.writeUTF(str);
				out.flush();
			}
			catch (Exception e)
			{

			}

			System.exit(0);
		}
		else if (str.equals("SHIPS"))
		{
			ShipsSet = true;
			CarrierSet = true;
			CruiserSet = true;
			SubmarineSet = true;
			DestroyerSet = true;
			BattleshipSet = true;
		}
		else if (str.equals("OVER"))
		{
			infoBox("You lost :(", "Player " + myID);
			over = true;
		}
		else if (str.equals("CARRIER"))
		{
			infoBox("Your Carrier has been sunk!","Player " + myID);
		}
		else if (str.equals("BATTLESHIP"))
		{
			infoBox("Your Battleship has been sunk!","Player " + myID);
		}
		else if (str.equals("CRUISER"))
		{
			infoBox("Your Cruiser has been sunk!","Player " + myID);
		}
		else if (str.equals("SUBMARINE"))
		{
			infoBox("Your Submarine has been sunk!","Player " + myID);
		}
		else if (str.equals("DESTROYER"))
		{
			infoBox("Your Destroyer has been sunk!","Player " + myID);
		}
		else if (build.length() == 4)
		{
			if (build.charAt(3) == myID)
			{
				build.deleteCharAt(3);
				str = build.toString();
				currentTurn = myID;
			}
		}
		else if (build.length() == 5)
		{
			if (build.charAt(4) == myID)
			{
				build.deleteCharAt(4);
				str = build.toString();
				currentTurn = myID;
			}
		}

		try
		{
			if (!str.equals("SHIPS"))
			{
				out.writeUTF(str);
				out.flush();
			}
		}
		catch (Exception e)
		{
			logger.error(e, e);
		}
	}

	/**
	 * 
	 * @param isSet
	 */
	public void isShipsSet(boolean isSet)
	{
		while (!isSet)
		{
			try
			{
				Thread.sleep(500);
			}
			catch (Throwable t)
			{

			}
			
			if (controller.getArmada().isCarrierSet() && !CarrierSet)
			{
				CarrierSet = true;
				logger.info("Carrier is Set.");
			}

			if (controller.getArmada().isBattleshipSet() && !BattleshipSet)
			{
				BattleshipSet = true;
				logger.info("Battleship is Set.");
			}

			if (controller.getArmada().isCruiserSet() && !CruiserSet)
			{
				CruiserSet = true;
				logger.info("Cruiser is Set.");
			}

			if (controller.getArmada().isSubmarineSet() && !SubmarineSet)
			{
				SubmarineSet = true;
				logger.info("Submarine is Set.");
			}

			if (controller.getArmada().isDestroyerSet() && !DestroyerSet)
			{
				DestroyerSet = true;
				logger.info("Destroyer is Set.");
			}

			if (CarrierSet && BattleshipSet && CruiserSet &&
					SubmarineSet && DestroyerSet)
			{
				logger.info("All ships are set!");
				isSet = true;
				observer.update("SET");
				controller.getArmada().logArmadaPosition();
				infoBox("Ships are Set!", "Player " + myID);
			}
		}
	}

	/**
	 * 
	 * @return String
	 */
	public String ServerAddress()
	{
		String host = "";
		InetAddress temp = null;

		try
		{
			temp = InetAddress.getByName(InetAddress.getLocalHost().getHostName());
		}
		catch (Exception e)
		{
			logger.error(e, e);
		}

		if (temp != null)
		{
			String[] hostPieces = temp.toString().split("/");
			host = hostPieces[1];
		}

		return host;
	}
	
	/**
	 * 
	 */
	private void destroy()
	{
		// close the connections
		try
		{ 
			in.close(); 
			out.close(); 
			socket.close();
			
			if (server != null)
			{
				server.close();
			}
		}
		catch (Exception e)
		{ 
			logger.error(e, e); 
		}
	}

	/**
	 * 
	 * @param observer
	 */
	@Override
	public void register(Observer observer)
	{
		this.observer = observer;
	}
	
	/**
	 * 
	 * @return
	 */
	public BoardController getController()
	{
		return controller;
	}
	
	/**
	 * 
	 * @return String
	 */
	public String getName()
	{
		return myName;
	}
	
	/**
	 * 
	 * @param string
	 * @return String
	 */
	public String toUppercase(String string)
	{
		return string.substring(0, 1).toUpperCase() + string.substring(1);
	}

	/**
	 * An Java Swing pop up info box for showing basic pop up information to the user during the game.
	 * 
	 * @param infoMessage The message to be inserted into the pop up message.
	 * @param titleBar The partial title of the pop up box.
	 */
	public void infoBox(String infoMessage, String titleBar)
	{
		JOptionPane.showMessageDialog(null, infoMessage, "Server: " + titleBar, JOptionPane.INFORMATION_MESSAGE);
	}
}
