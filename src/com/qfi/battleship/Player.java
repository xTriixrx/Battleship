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
		m_port = port;
		m_address = address;
		m_controller = controller;

		// byte seed for the SecureRandom object
		byte[] seed = ByteBuffer.allocate(Long.SIZE / Byte.SIZE)
			.putLong(System.currentTimeMillis()).array();

		// Instantiate random object
		SecureRandom random = new SecureRandom(seed);

		if (playerType == 1) // client
		{
			m_myID = CLIENT_ID;
			m_myName = capitalize(CLIENT);
		}
		else if (playerType == 2) // server
		{
			m_myID = SERVER_ID;
			m_myName = capitalize(SERVER);
			m_controller.setCurrentTurn(random.nextInt(MAX_PLAYERS) + 1);
		}

		// Establish bidirectional observer message pattern
		((Observable) m_controller).register(this);
		register((Observer) m_controller);
	}

	/**
	 * 
	 */
	@Override
	public void run()
	{
		String message;

		//
		performStartupProtocol();

		//
		while (!m_over)
		{
			message = readOpponentMessage();
			m_logger.debug("Player received '" + message + "' message from opponent.");
			handleOpponentMessage(message);
			m_observer.update(message);
		}

		destroy();
	}

	/**
	 *
	 */
	private void performStartupProtocol()
	{
		// Initializes the socket connection as either a client or server, based on players' configuration
		initializeConnection();

		// Instantiates the input and output data streams to communicate with opponent over socket
		initializeStreams();

		// Notify controller that the connection to the opponent has been established
		m_observer.update(Message.CONNECTED.getMsg());

		// The server component will submit the randomized turn value to the client, will wait for clients confirmation
		performHandshake();

		// A blocking method to wait for all players' ships to be set, upon all ships being set, the controller
		// will message "SHIPS" to the player instance signaling to this method to stop blocking
		isShipsSet();

		// Submit a READY message to the opponent to signify the SHIPS have been set are ready to play
		m_logger.debug("Player " + m_myID + " is sending READY to opponent.");
		messageOpponent(Message.READY.getMsg());

		// Block until the opponent has responded back with READY themselves
		while (!readOpponentMessage().equals(Message.READY.getMsg()))
		{
			m_logger.debug("Player " + m_myID + " waiting for incoming READY from opponent.");
			sleep(500);
		}

		// Notify the controller that the opponents' ships have been SET.
		m_observer.update(Message.SET.getMsg());
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
		messageOpponent(Integer.toString(m_controller.getCurrentTurn()));
		String line = readOpponentMessage();

		m_logger.info("Server received: " + line + " from client.");
	}
	
	/**
	 * 
	 */
	private void performClientHandshakeProtocol()
	{
		int currentTurn = Integer.parseInt(readOpponentMessage());
		m_controller.setCurrentTurn(currentTurn);

		messageOpponent("Client received " + currentTurn + " from Server...");
	}

	@Override
	public void update(String controllerMessage)
	{
		m_logger.info("Received " + controllerMessage + " from observable controller.");
		handleControllerMessage(controllerMessage);
	}

	/**
	 * 
	 * @param opponentMessage
	 */
	public void handleOpponentMessage(String opponentMessage)
	{
		if (opponentMessage.equalsIgnoreCase(Message.SHUTDOWN.getMsg()))
		{
			System.exit(0);
		}
		else if(opponentMessage.equals(Message.OVER.getMsg()))
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
		else if(opponentMessage.contains(Message.CARRIER.getMsg()))
		{
			infoBox("You sunk your opponents Carrier!", "Player " + m_myID);
		}
		else if(opponentMessage.contains(Message.BATTLESHIP.getMsg()))
		{
			infoBox("You sunk your opponents Battleship!", "Player " + m_myID);
		}
		else if(opponentMessage.contains(Message.CRUISER.getMsg()))
		{
			infoBox("You sunk your opponents Cruiser!", "Player " + m_myID);
		}
		else if(opponentMessage.contains(Message.SUBMARINE.getMsg()))
		{
			infoBox("You sunk your opponents Submarine!", "Player " + m_myID);
		}
		else if(opponentMessage.contains(Message.DESTROYER.getMsg()))
		{
			infoBox("You sunk your opponents Destroyer!", "Player " + m_myID);
		}
	}

	/**
	 *
	 * @param controllerMessage
	 */
	public void handleControllerMessage(String controllerMessage)
	{
		if (controllerMessage.equalsIgnoreCase(Message.SHUTDOWN.getMsg()))
		{
			messageOpponent(controllerMessage);
			System.exit(0);
		}
		else if (controllerMessage.equals(Message.SHIPS.getMsg()))
		{
			synchronized (m_shipSetSignal)
			{
				m_shipSetSignal.notifyAll();
			}
		}
		else if (controllerMessage.equals(Message.OVER.getMsg()))
		{
			infoBox("You lost :(", "Player " + m_myID);
			m_over = true;
		}
		else if (controllerMessage.contains(Message.CARRIER.getMsg()))
		{
			infoBox("Your Carrier has been sunk!", "Player " + m_myID);
		}
		else if (controllerMessage.contains(Message.BATTLESHIP.getMsg()))
		{
			infoBox("Your Battleship has been sunk!", "Player " + m_myID);
		}
		else if (controllerMessage.contains(Message.CRUISER.getMsg()))
		{
			infoBox("Your Cruiser has been sunk!", "Player " + m_myID);
		}
		else if (controllerMessage.contains(Message.SUBMARINE.getMsg()))
		{
			infoBox("Your Submarine has been sunk!", "Player " + m_myID);
		}
		else if (controllerMessage.contains(Message.DESTROYER.getMsg()))
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

		// If the controller sent a message other than the internal "SHIPS" message, pass message to opponent
		if (!controllerMessage.equals(Message.SHIPS.getMsg()))
		{
			m_logger.info("Sending '" + controllerMessage + "' to opponent.");
			messageOpponent(controllerMessage);
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
	 * Returns a string representing the host and port within the local network the server socket has binded to.
	 *
	 * @return String - Returns a partial hostname string representing the local address and port the server binded to.
	 */
	public String serverAddress()
	{
		String host = "";
		InetAddress fullHostname = null;

		try
		{
			fullHostname = InetAddress.getByName(InetAddress.getLocalHost().getHostName());
		}
		catch (Exception e)
		{
			m_logger.error(e, e);
		}

		if (fullHostname != null)
		{
			String[] hostPieces = fullHostname.toString().split("/");
			host = hostPieces[1];
		}

		return host;
	}

	/**
	 * Reads a message from the opponent via the socket interface.
	 *
	 * @return String - Returns a string representing a message from the opponent.
	 */
	private String readOpponentMessage()
	{
		String message = "";

		try
		{
			message = m_in.readUTF();
		}
		catch (Exception e)
		{
			m_logger.error(e, e);
		}

		return message;
	}

	/**
	 * Submits a message to the opponent via the socket interface.
	 *
	 * @param message - A string representing a message to send to the opponent.
	 */
	private void messageOpponent(String message)
	{
		try
		{
			m_out.writeUTF(message);
			m_out.flush();
		}
		catch (Exception e)
		{
			m_logger.error(e, e);
		}
	}
	
	/**
	 * Destroys and shuts down the controller, input and output data streams, the socket related objects.
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
	 * Depending on whether the player instance is set as a server or client, this method will either create
	 * a server socket for a client to establish a connection or will establish a connection as a client
	 * to an already binded server socket. It is assumed the player acting as the server has already started
	 * running prior to the client.
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
	 * Establishes a client connection to some host address and port configuration.
	 */
	private void connectToServer()
	{
		try
		{
			m_socket = new Socket(m_address, m_port);
		}
		catch (Exception e)
		{
			m_logger.error(e, e);
		}

		m_logger.info("Connected to server at " + m_address + ":" + m_port + "!");
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
	 * A wrapper for calling the Thread class's sleep method within a try catch block.
	 *
	 * @param milliseconds - Amount of seconds to sleep the calling thread.
	 */
	private void sleep(int milliseconds)
	{
		try
		{
			Thread.sleep(milliseconds);
		}
		catch (Exception e)
		{
			m_logger.error(e, e);
			Thread.currentThread().interrupt();
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
