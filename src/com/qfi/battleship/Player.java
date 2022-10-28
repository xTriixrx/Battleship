package com.qfi.battleship;

import java.net.Socket;
import java.nio.ByteBuffer;
import java.net.InetAddress;
import java.net.ServerSocket;
import javax.swing.JOptionPane;
import java.io.DataInputStream;
import org.apache.log4j.Logger;
import java.io.DataOutputStream;
import java.security.SecureRandom;
import org.apache.log4j.LogManager;

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
	private boolean automated = false;
	private DataInputStream in = null;
	private SecureRandom random = null;
	private ServerSocket server = null;
	private DataOutputStream out = null;
	private Controller controller = null;
	private final Object shipSetSignal = new Object();
	private Logger logger = LogManager.getLogger(Player.class);

	public static final int CLIENT_ID = 1;
	public static final int SERVER_ID = 2;
	public static final int MAX_PLAYERS = 2;
	public static final String SERVER = "server";
	public static final String CLIENT = "client";

	/**
	 * 
	 * @param playerType
	 * @param address
	 * @param port
	 */
	public Player(Controller controller, int playerType, String address, int port)
	{
		if (playerType == 1) // client
		{
			myID = CLIENT_ID;
			myName = capitalize(CLIENT);
		}
		else if (playerType == 2) // server
		{
			myID = SERVER_ID;
			myName = capitalize(SERVER);
		}

		this.port = port;
		this.address = address;
		this.controller = controller;
		
		((Observable) controller).register(this);
		register((Observer) controller);

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
	}

	/**
	 * 
	 */
	@Override
	public void run()
	{
		initializeConnection();
		initializeStreams();

		observer.update("CONNECTED");

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
					
					try
					{
						logger.debug("Player " + myID + " is sending READY to opponent.");
						out.writeUTF("READY");
						out.flush();
					}
					catch (Exception e)
					{

					}

					while (!(in.readUTF()).equals("READY"))
					{
						try
						{
							logger.debug("Player " + myID + " waiting for incoming READY from opponent.");
							Thread.sleep(500);
						}
						catch (Exception e)
						{
							logger.error(e, e);
						}
					}

					observer.update("SET");
				}
				else // Process message by opponent
				{
					message = in.readUTF();
					logger.debug("Player received '" + message + "' message from opponent.");
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
			logger.info("Server received: " + line + " from client.");
			lineCount++;
			isShipsSet();
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
			isShipsSet();
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
			logger.info("Local Address: " + serverAddress());
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
		logger.info("Received " + s + " from observable controller.");
		StringBuilder t = new StringBuilder(s);
		updateResponse(s, t);
	}

	/**
	 * 
	 * @param opponentMessage
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
			
			over = true;
		}
		else if(opponentMessage.equals(Armada.CARRIER_NAME))
		{
			infoBox("You sunk your opponents Carrier!", "Player " + myID);
		}
		else if(opponentMessage.equals(Armada.BATTLESHIP_NAME))
		{
			infoBox("You sunk your opponents Battleship!", "Player " + myID);
		}
		else if(opponentMessage.equals(Armada.CRUISER_NAME))
		{
			infoBox("You sunk your opponents Cruiser!", "Player " + myID);
		}
		else if(opponentMessage.equals(Armada.SUBMARINE_NAME))
		{
			infoBox("You sunk your opponents Submarine!", "Player " + myID);
		}
		else if(opponentMessage.equals(Armada.DESTROYER_NAME))
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
			synchronized (shipSetSignal)
			{
				shipSetSignal.notifyAll();
			}
		}
		else if (str.equals("OVER"))
		{
			infoBox("You lost :(", "Player " + myID);
			over = true;
		}
		else if (str.contains(Armada.CARRIER_NAME))
		{
			infoBox("Your Carrier has been sunk!", "Player " + myID);
		}
		else if (str.contains(Armada.BATTLESHIP_NAME))
		{
			infoBox("Your Battleship has been sunk!", "Player " + myID);
		}
		else if (str.contains(Armada.CRUISER_NAME))
		{
			infoBox("Your Cruiser has been sunk!", "Player " + myID);
		}
		else if (str.contains(Armada.SUBMARINE_NAME))
		{
			infoBox("Your Submarine has been sunk!", "Player " + myID);
		}
		else if (str.contains(Armada.DESTROYER_NAME))
		{
			infoBox("Your Destroyer has been sunk!", "Player " + myID);
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
				logger.info("Sending '" + str + "' to opponent.");
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
	 */
	public void isShipsSet()
	{
		boolean isSet = false;

		while (!isSet)
		{
			synchronized (shipSetSignal)
			{
				try
				{
					shipSetSignal.wait();
				}
				catch (InterruptedException e)
				{
					logger.error(e, e);
					Thread.currentThread().interrupt();
				}
			}

			logger.info("All ships are set!");
			isSet = true;
			controller.getArmada().logArmadaPosition();
			infoBox("Ships are Set!", "Player " + myID);
		}
	}

	/**
	 * 
	 * @return String
	 */
	public String serverAddress()
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
			controller.shutdown();
			
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
	 * @return
	 */
	public boolean isAutomated()
	{
		return automated;
	}
	
	/**
	 * 
	 * @param automated
	 */
	public void setAutomated(boolean automated)
	{
		this.automated = automated;
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
	 * @return String
	 */
	public String getName()
	{
		return myName;
	}
	
	/**
	 * Capitalizes a provided string by making the first character in the string uppercase.
	 * 
	 * @param string The string to capitalize.
	 * @return String
	 */
	public String capitalize(String string)
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
		if (!isAutomated())
		{
			JOptionPane.showMessageDialog(null, infoMessage, myName + ": " + titleBar, JOptionPane.INFORMATION_MESSAGE);
		}
	}
}
