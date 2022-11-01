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
	private int m_port = 0;
	private int m_myID = 0;
	private String m_myName = "";
	private String m_address = "";
	private boolean m_over = false;
	private Socket m_socket = null;
	private Observer m_observer = null;
	private boolean m_automated = false;
	private DataInputStream m_in = null;
	private ServerSocket m_server = null;
	private DataOutputStream m_out = null;
	private Controller m_controller = null;
	private final Object m_shipSetSignal = new Object();
	private static final Logger m_logger = LogManager.getLogger(Player.class);

	public static final int CLIENT_ID = 1;
	public static final int SERVER_ID = 2;
	public static final int MAX_PLAYERS = 2;
	public static final String SERVER = "server";
	public static final String CLIENT = "client";

	/**
	 * Player constructor.
	 *
	 * @param controller - Some controller interface in order to pass/receive game updates.
	 * @param playerType - The type of player this player instance will be.
	 * @param address - The address of the expected server instance.
	 * @param port - The port of the expected server instance.
	 */
	public Player(Controller controller, int playerType, String address, int port)
	{
		if (playerType == 1) // client
		{
			m_myID = CLIENT_ID;
			m_myName = capitalize(CLIENT);
		}
		else if (playerType == 2) // server
		{
			m_myID = SERVER_ID;
			m_myName = capitalize(SERVER);
		}

		m_port = port;
		m_address = address;
		m_controller = controller;

		// Establish bidirectional observer message pattern
		((Observable) controller).register(this);
		register((Observer) controller);

		// byte seed for the SecureRandom object
		byte[] seed = ByteBuffer.allocate(Long.SIZE / Byte.SIZE)
			.putLong(System.currentTimeMillis()).array();
		
		// Instantiate random object
		SecureRandom random = new SecureRandom(seed);
		
		if (m_myID == SERVER_ID)
		{
			controller.setCurrentTurn(random.nextInt(MAX_PLAYERS) + 1);
		}
	}

	/**
	 * 
	 */
	@Override
	public void run()
	{
		int lineCount = 0;
		initializeConnection();
		initializeStreams();

		m_observer.update("CONNECTED");

		// string to read message from input 
		String message = "";

		while (!m_over)
		{
			try
			{
				// If no lines have been read, the initial handshake needs to take place
				if (lineCount == 0)
				{
					//
					performHandshake();

					//
					isShipsSet();

					try
					{
						m_logger.debug("Player " + m_myID + " is sending READY to opponent.");
						m_out.writeUTF("READY");
						m_out.flush();
					}
					catch (Exception e)
					{

					}

					while (!(m_in.readUTF()).equals("READY"))
					{
						try
						{
							m_logger.debug("Player " + m_myID + " waiting for incoming READY from opponent.");
							Thread.sleep(500);
						}
						catch (Exception e)
						{
							m_logger.error(e, e);
						}
					}

					m_observer.update("SET");
				}
				else // Process message by opponent
				{
					message = m_in.readUTF();
					m_logger.debug("Player received '" + message + "' message from opponent.");
					received(message);
					m_observer.update(message);
				}

				lineCount++;
			}
			catch (Exception e)
			{
				m_logger.error(e, e);
			}
		}

		destroy();
	}
	
	/**
	 * 
	 */
	private void performHandshake()
	{
		if (m_myID == SERVER_ID) // server
		{
			performServerHandshakeProtocol();
		}
		else if (m_myID == CLIENT_ID) // client
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
			m_out.writeInt(m_controller.getCurrentTurn());
			m_out.flush();
			line = m_in.readUTF();
			m_logger.info("Server received: " + line + " from client.");
		}
		catch (Exception e)
		{
			m_logger.error(e, e);
		}
	}
	
	/**
	 * 
	 */
	private void performClientHandshakeProtocol()
	{
		try
		{
			int currentTurn = m_in.readInt();
			m_out.writeUTF("Client received " + currentTurn + " from Server...");
			m_out.flush();
			m_controller.setCurrentTurn(currentTurn);
		}
		catch (Exception e)
		{
			m_logger.error(e, e);
		}
	}
	
	/**
	 * 
	 */
	private void connectToServer()
	{
		try
		{
			m_socket = new Socket(m_address, m_port);
			m_logger.info("Connected to server at " + m_address + ":" + m_port + "!");
		}
		catch (Exception e)
		{
			m_logger.error(e, e);
		}
	}

	@Override
	public void update(String controllerMessage)
	{
		m_logger.info("Received " + controllerMessage + " from observable controller.");
		updateResponse(controllerMessage);
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
			infoBox("You Won! (:", "Player " + m_myID);
			
			try
			{
				Thread.sleep(1000);
				System.exit(0);
			}
			catch (Exception e)
			{
				m_logger.error(e, e);
			}

			m_over = true;
		}
		else if(opponentMessage.equals(Armada.CARRIER_NAME))
		{
			infoBox("You sunk your opponents Carrier!", "Player " + m_myID);
		}
		else if(opponentMessage.equals(Armada.BATTLESHIP_NAME))
		{
			infoBox("You sunk your opponents Battleship!", "Player " + m_myID);
		}
		else if(opponentMessage.equals(Armada.CRUISER_NAME))
		{
			infoBox("You sunk your opponents Cruiser!", "Player " + m_myID);
		}
		else if(opponentMessage.equals(Armada.SUBMARINE_NAME))
		{
			infoBox("You sunk your opponents Submarine!", "Player " + m_myID);
		}
		else if(opponentMessage.equals(Armada.DESTROYER_NAME))
		{
			infoBox("You sunk your opponents Destroyer!", "Player " + m_myID);
		}
	}

	/**
	 *
	 * @param controllerMessage
	 */
	public void updateResponse(String controllerMessage)
	{
		if (controllerMessage.equalsIgnoreCase("SHUTDOWN"))
		{
			try
			{
				m_out.writeUTF(controllerMessage);
				m_out.flush();
			}
			catch (Exception e)
			{

			}

			System.exit(0);
		}
		else if (controllerMessage.equals("SHIPS"))
		{
			synchronized (m_shipSetSignal)
			{
				m_shipSetSignal.notifyAll();
			}
		}
		else if (controllerMessage.equals("OVER"))
		{
			infoBox("You lost :(", "Player " + m_myID);
			m_over = true;
		}
		else if (controllerMessage.contains(Armada.CARRIER_NAME))
		{
			infoBox("Your Carrier has been sunk!", "Player " + m_myID);
		}
		else if (controllerMessage.contains(Armada.BATTLESHIP_NAME))
		{
			infoBox("Your Battleship has been sunk!", "Player " + m_myID);
		}
		else if (controllerMessage.contains(Armada.CRUISER_NAME))
		{
			infoBox("Your Cruiser has been sunk!", "Player " + m_myID);
		}
		else if (controllerMessage.contains(Armada.SUBMARINE_NAME))
		{
			infoBox("Your Submarine has been sunk!", "Player " + m_myID);
		}
		else if (controllerMessage.contains(Armada.DESTROYER_NAME))
		{
			infoBox("Your Destroyer has been sunk!", "Player " + m_myID);
		}
		else
		{
			if (controllerMessage.charAt(controllerMessage.length() - 1) == m_myID)
			{
				StringBuilder sb = new StringBuilder(controllerMessage);
				sb.deleteCharAt(sb.length() - 1);
				controllerMessage = sb.toString();
			}
		}

		try
		{
			if (!controllerMessage.equals("SHIPS"))
			{
				m_logger.info("Sending '" + controllerMessage + "' to opponent.");
				m_out.writeUTF(controllerMessage);
				m_out.flush();
			}
		}
		catch (Exception e)
		{
			m_logger.error(e, e);
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
			synchronized (m_shipSetSignal)
			{
				try
				{
					m_shipSetSignal.wait();
				}
				catch (InterruptedException e)
				{
					m_logger.error(e, e);
					Thread.currentThread().interrupt();
				}
			}

			m_logger.info("All ships are set!");
			isSet = true;
			m_controller.getArmada().logArmadaPosition();
			infoBox("Ships are Set!", "Player " + m_myID);
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
			m_logger.error(e, e);
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
			m_controller.shutdown();

			m_in.close();
			m_out.close();
			m_socket.close();
			
			if (m_server != null)
			{
				m_server.close();
			}
		}
		catch (Exception e)
		{
			m_logger.error(e, e);
		}
	}

	/**
	 *
	 */
	private void initializeConnection()
	{
		if (m_myID == SERVER_ID)
		{
			initializeServerSocket();
		}
		else if (m_myID == CLIENT_ID)
		{
			connectToServer();
		}
	}

	/**
	 * Instantiates and initializes the server socket for a client to connect to, will block
	 * until the client connection has been established.
	 */
	private void initializeServerSocket()
	{
		try
		{
			m_server = new ServerSocket(m_port);
			m_logger.info("Local Address: " + serverAddress());
			m_logger.info("Waiting for a client to connect...");

			// Blocks waiting for a client to connect to the socket
			m_socket = m_server.accept();
			m_logger.info("Client accepted.");
		}
		catch (Exception e)
		{
			m_logger.error(e, e);
		}
	}

	/**
	 * Instantiates and initializes the data input and output streams to communicate through
	 * the socket interface.
	 */
	private void initializeStreams()
	{
		try
		{
			// takes input from the client socket
			m_in = new DataInputStream(m_socket.getInputStream());
			m_out = new DataOutputStream(m_socket.getOutputStream());
		}
		catch (Exception e)
		{
			m_logger.error(e, e);
		}
	}

	/**
	 * A Java Swing pop up info box for showing basic pop up information to the user during the game.
	 * 
	 * @param infoMessage - The message to be inserted into the pop-up message.
	 * @param titleBar - The partial title of the pop-up box.
	 */
	public void infoBox(String infoMessage, String titleBar)
	{
		if (!isAutomated())
		{
			JOptionPane.showMessageDialog(null, infoMessage, m_myName + ": " + titleBar, JOptionPane.INFORMATION_MESSAGE);
		}
	}

	/**
	 * Capitalizes a provided string by making the first character in the string uppercase.
	 *
	 * @param string The string to capitalize.
	 * @return String - A capitalized string.
	 */
	public String capitalize(String string)
	{
		return string.substring(0, 1).toUpperCase() + string.substring(1);
	}

	/**
	 * Registers an observer object instance to this observable instance.
	 *
	 * @param observer - An observer interface to exchange messages between the observer & the observable.
	 */
	@Override
	public void register(Observer observer)
	{
		m_observer = observer;
	}

	/**
	 * A mutator for setting whether the player is communicating with an automated controller.
	 *
	 * @param automated - A flag representing if the controller is automated.
	 */
	public void setAutomated(boolean automated)
	{
		m_automated = automated;
	}

	/**
	 * An accessor returning a string based name representing the type of player instance.
	 *
	 * @return String - A string representing the name of the type of player that this instance represents.
	 */
	public String getName()
	{
		return m_myName;
	}

	/**
	 * A boolish method representing whether an automated controller is communicating with this player instance.
	 *
	 * @return boolean - A flag representing whether this player is representing an automated controller.
	 */
	public boolean isAutomated()
	{
		return m_automated;
	}
}
