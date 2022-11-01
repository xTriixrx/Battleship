package com.qfi.battleship;

import java.util.List;
import java.util.ArrayList;
import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;

/**
 * The Armada class is a stateful representation of the current status of a players' Armada. The armada contains a
 * Carrier, Battleship, Cruiser, Submarine, and Destroyer. Each ship is represented as a mutable list that can be
 * populated during the start-up of the game and having positions popped off as they are being hit. The Armada class
 * can also be utilized to represent the state of hit ships as the game is proceeding. It is up to the controller to
 * dictate how it will utilize the mutability of this object.
 *
 * @author Vincent.Nigro
 * @version 1.0.0
 */
public class Armada
{
	private int m_health = 5;
	private boolean m_carrierSunk = false;
	private boolean m_cruiserSunk = false;
	private boolean m_submarineSunk = false;
	private boolean m_destroyerSunk = false;
	private boolean m_battleshipSunk = false;
	private final List<String> m_carrier = new ArrayList<>();
	private final List<String> m_cruiser = new ArrayList<>();
	private final List<String> m_submarine = new ArrayList<>();
	private final List<String> m_destroyer = new ArrayList<>();
	private final List<String> m_battleship = new ArrayList<>();
	private static final Logger m_logger = LogManager.getLogger(Armada.class);

	public static final short CARRIER_SIZE = 5;
	public static final short CRUISER_SIZE = 3;
	public static final short SUBMARINE_SIZE = 3;
	public static final short DESTROYER_SIZE = 2;
	public static final short BATTLESHIP_SIZE = 4;
	public static final String CARRIER_NAME = "CARRIER";
	public static final String CRUISER_NAME = "CRUISER";
	public static final String SUBMARINE_NAME = "SUBMARINE";
	public static final String DESTROYER_NAME = "DESTROYER";
	public static final String BATTLESHIP_NAME = "BATTLESHIP";

	private static final String ATTEMPTED_TO_ADD = "Attempted to add ";
	private static final String RECEIVED_POSITIONS = ", received positions: ";
	private static final String POSITIONS_WHEN_ONLY_NEEDED = " positions when only needed ";

	/**
	 * A package protected enumeration representing the types of ships within the Armada.
	 *
	 * @author Vincent.Nigro
	 * @version 1.0.0
	 */
	protected enum ArmadaType
	{
		DESTROYER,
		SUBMARINE,
		CRUISER,
		BATTLESHIP,
		CARRIER,
	}
	
	/**
	 * A boolish method to determine if a targeted position actually hit a ship or missed.
	 *
	 * @param targetPosition - The position on the board that was targeted.
	 * @return boolean - A flag representing whether the position hit a ship within the armada.
	 */
	public boolean calculateHit(String targetPosition)
	{
		return m_cruiser.contains(targetPosition) ||
				m_carrier.contains(targetPosition) ||
				m_destroyer.contains(targetPosition) ||
				m_submarine.contains(targetPosition) ||
				m_battleship.contains(targetPosition);
	}
	
	/**
	 * Will attempt to update the mutable Armada based on the position that was targeted. If the position
	 * is contained in one of the ships within the armada, the position will be removed from that respective
	 * ship and the standard name of the ship will be returned.
	 *
	 * @param position - The position on the board that was targeted.
	 * @return String - The standard name of the ship which was hit.
	 */
	public String updateArmada(String position)
	{	
		if (m_destroyer.contains(position))
		{
			m_destroyer.remove(position);
			return DESTROYER_NAME;
		}
		else if (m_submarine.contains(position))
		{
			m_submarine.remove(position);
			return SUBMARINE_NAME;
		}
		else if (m_cruiser.contains(position))
		{
			m_cruiser.remove(position);
			return CRUISER_NAME;
		}
		else if (m_battleship.contains(position))
		{
			m_battleship.remove(position);
			return BATTLESHIP_NAME;
		}
		else if (m_carrier.contains(position))
		{
			m_carrier.remove(position);
			return CARRIER_NAME;
		}

		return "";
	}

	/**
	 * Adds a list strings representing positions for the carrier ship. If the Armada member list for carrier
	 * is empty and the parameter list provided is the same size as the expected carrier size the parameter list will
	 * be added into the internal member list for the carrier.
	 * 
	 * @param carrierPos - A list of strings representing multiple blocks for a carrier.
	 */
	public void addToCarrier(List<String> carrierPos)
	{
		if (m_carrier.isEmpty())
		{
			m_carrier.addAll(carrierPos);
		}
		else if (carrierPos.size() != CARRIER_SIZE)
		{
			m_logger.debug(ATTEMPTED_TO_ADD + carrierPos.size() + POSITIONS_WHEN_ONLY_NEEDED + CARRIER_SIZE +
					RECEIVED_POSITIONS + carrierPos + ".");
		}
		else
		{
			m_logger.debug("Carrier position is already set.");
		}
	}
	
	/**
	 * Adds a list strings representing positions for the battleship ship. If the Armada member list for battleship
	 * is empty and the parameter list provided is the same size as the expected battleship size the parameter list will
	 * be added into the internal member list for the battleship.
	 * 
	 * @param battleshipPos - A list of strings representing multiple blocks for a battleship.
	 */
	public void addToBattleship(List<String> battleshipPos)
	{
		if (m_battleship.isEmpty())
		{
			m_battleship.addAll(battleshipPos);
		}
		else if (battleshipPos.size() != BATTLESHIP_SIZE)
		{
			m_logger.debug(ATTEMPTED_TO_ADD + battleshipPos.size() + POSITIONS_WHEN_ONLY_NEEDED + BATTLESHIP_SIZE
					+ RECEIVED_POSITIONS + battleshipPos + ".");
		}
		else
		{
			m_logger.debug("Battleship position is already set.");
		}
	}
	
	/**
	 * Adds a list strings representing positions for the cruiser ship. If the Armada member list for cruiser
	 * is empty and the parameter list provided is the same size as the expected cruiser size the parameter list will
	 * be added into the internal member list for the cruiser.
	 * 
	 * @param cruiserPos - A list of strings representing multiple blocks for a cruiser.
	 */
	public void addToCruiser(List<String> cruiserPos)
	{
		if (m_cruiser.isEmpty())
		{
			m_cruiser.addAll(cruiserPos);
		}
		else if (cruiserPos.size() != CRUISER_SIZE)
		{
			m_logger.debug(ATTEMPTED_TO_ADD + cruiserPos.size() + POSITIONS_WHEN_ONLY_NEEDED + CRUISER_SIZE +
					RECEIVED_POSITIONS + cruiserPos + ".");
		}
		else
		{
			m_logger.debug("Cruise position is already set.");
		}
	}
	
	/**
	 * Adds a list strings representing positions for the submarine ship. If the Armada member list for submarine
	 * is empty and the parameter list provided is the same size as the expected submarine size the parameter list will
	 * be added into the internal member list for the submarine.
	 * 
	 * @param submarinePos - A list of strings representing multiple blocks for a submarine.
	 */
	public void addToSubmarine(List<String> submarinePos)
	{
		if (m_submarine.isEmpty())
		{
			m_submarine.addAll(submarinePos);
		}
		else if (submarinePos.size() != SUBMARINE_SIZE)
		{
			m_logger.debug(ATTEMPTED_TO_ADD + submarinePos.size() + POSITIONS_WHEN_ONLY_NEEDED + SUBMARINE_SIZE
							+ RECEIVED_POSITIONS + submarinePos + ".");
		}
		else
		{
			m_logger.debug("Submarine position is already set.");
		}
	}
	
	/**
	 * Adds a list strings representing positions for the destroyer ship. If the Armada member list for destroyer
	 * is empty and the parameter list provided is the same size as the expected destroyer size the parameter list will
	 * be added into the internal member list for the destroyer.
	 * 
	 * @param destroyerPos - A list of strings representing multiple blocks for a destroyer.
	 */
	public void addToDestroyer(List<String> destroyerPos)
	{
		if (m_destroyer.isEmpty() && destroyerPos.size() == DESTROYER_SIZE)
		{
			m_destroyer.addAll(destroyerPos);
		}
		else if (destroyerPos.size() != DESTROYER_SIZE)
		{
			m_logger.debug(ATTEMPTED_TO_ADD + destroyerPos.size() + POSITIONS_WHEN_ONLY_NEEDED + DESTROYER_SIZE
							+ RECEIVED_POSITIONS + destroyerPos + ".");
		}
		else
		{
			m_logger.debug("Destroyer position is already set.");
		}
	}
	
	/**
	 * Adds a single block position to the carrier if and only if the size of the Armada member list for carrier is 
	 * smaller than the expected carrier size.
	 * 
	 * @param pos - A string representing a single block position for a carrier.
	 */
	public void addToCarrier(String pos)
	{
		if (m_carrier.size() < CARRIER_SIZE)
		{
			m_carrier.add(pos);
		}
		else
		{
			m_logger.debug("Carrier position is already set.");
		}
	}
	
	/**
	 * Adds a single block position to the battleship if and only if the size of the Armada member list for battleship is 
	 * smaller than the expected battleship size.
	 * 
	 * @param pos - A string representing a single block position for a battleship.
	 */
	public void addToBattleship(String pos)
	{
		if (m_battleship.size() < BATTLESHIP_SIZE)
		{
			m_battleship.add(pos);
		}
		else
		{
			m_logger.debug("Battleship position is already set.");
		}
	}
	
	/**
	 * Adds a single block position to the cruiser if and only if the size of the Armada member list for cruiser is 
	 * smaller than the expected cruiser size.
	 * 
	 * @param pos - A string representing a single block position for a cruiser.
	 */
	public void addToCruiser(String pos)
	{
		if (m_cruiser.size() < CRUISER_SIZE)
		{
			m_cruiser.add(pos);
		}
		else
		{
			m_logger.debug("Cruise position is already set.");
		}
	}
	
	/**
	 * Adds a single block position to the submarine if and only if the size of the Armada member list for submarine is 
	 * smaller than the expected submarine size.
	 * 
	 * @param pos - A string representing a single block position for a submarine.
	 */
	public void addToSubmarine(String pos)
	{
		if (m_submarine.size() < SUBMARINE_SIZE)
		{
			m_submarine.add(pos);
		}
		else
		{
			m_logger.debug("Submarine position is already set.");
		}
	}
	
	/**
	 * Adds a single block position to the destroyer if and only if the size of the Armada member list for destroyer is 
	 * smaller than the expected destroyer size.
	 * 
	 * @param pos - A string representing a single block position for a destroyer.
	 */
	public void addToDestroyer(String pos)
	{
		if (m_destroyer.size() < DESTROYER_SIZE)
		{
			m_destroyer.add(pos);
		}
		else
		{
			m_logger.debug("Destroyer position is already set.");
		}
	}

	/**
	 * Public interface for logging the allocated positions for the Armada.
	 */
	public void logArmadaPosition()
	{
		logPosition(ArmadaType.DESTROYER, m_destroyer);
		logPosition(ArmadaType.SUBMARINE, m_submarine);
		logPosition(ArmadaType.CRUISER, m_cruiser);
		logPosition(ArmadaType.BATTLESHIP, m_battleship);
		logPosition(ArmadaType.CARRIER, m_carrier);
	}

	/**
	 * Generic logging function that uses a ArmadaType enumeration value and a list of strings representing a ships
	 * position to log the blocks allocated for that given ship type.
	 *
	 * @param type - An ArmadaType enum representing the type of ship that is being logged.
	 * @param ship - The list containing a set of positions representing the ships' coordinates.
	 */
	private void logPosition(ArmadaType type, List<String> ship)
	{
		StringBuilder shipPositions = new StringBuilder();

		for (String position : ship)
		{
			shipPositions.append(position).append(" ");
		}

		m_logger.trace(type + " Position: " + shipPositions);
	}

	/**
	 * A utility method for creating a space separated string containing the positions of a given ship.
	 *
	 * @param ship - The ship list to create a position string for.
	 * @return - A String representing the positions within set of string positions within the ships' list.
	 */
	private String getPositions(List<String> ship)
	{
		StringBuilder positions = new StringBuilder();

		for (String position : ship)
		{
			positions.append(position).append(" ");
		}

		return positions.toString();
	}

	/**
	 * A boolish method to determine if the entire armada has been sunk. This method utilizes the internal
	 * ship flags to represent the overall state of the armada instance.
	 *
	 * @return boolean - A boolean representing whether the armada instance has sunk.
	 */
	public boolean isArmadaSunk()
	{
		return m_health == 0 && m_carrierSunk && m_battleshipSunk &&
				m_cruiserSunk && m_submarineSunk && m_destroyerSunk;
	}

	/**
	 * A boolish method to determine if the carrier has been sunk. This method will also update the armada instance's
	 * internal health as well as the internal associated sunk flag with the carrier ship. This function will only
	 * return true once as it is supposed to indicate when a ship has just sunk rather than persistently holding this
	 * information. This is due to the controller's utilizing the armada needing to query for which ship has sunk by
	 * polling each is sunk method.
	 *
	 * @return boolean - A flag representing whether the carrier ship has been sunk.
	 */
	public boolean isCarrierSunk()
	{
		// If the carrier list is empty and its carrier flag has not been set, the ship has just sunk.
		if (getCarrier().isEmpty() && !m_carrierSunk)
		{
			m_health--;
			m_carrierSunk = true;
			return true;
		}

		return false;
	}

	/**
	 * A boolish method to determine if the battleship has been sunk. This method will also update the armada instance's
	 * internal health as well as the internal associated sunk flag with the battleship ship. This function will only
	 * return true once as it is supposed to indicate when a ship has just sunk rather than persistently holding this
	 * information. This is due to the controller's utilizing the armada needing to query for which ship has sunk by
	 * polling each is sunk method.
	 *
	 * @return boolean - A flag representing whether the battleship ship has been sunk.
	 */
	public boolean isBattleshipSunk()
	{
		// If the destroyer list is empty and its destroyer flag has not been set, the ship has just sunk.
		if (getBattleship().isEmpty() && !m_battleshipSunk)
		{
			m_health--;
			m_battleshipSunk = true;
			return true;
		}

		return false;
	}

	/**
	 * A boolish method to determine if the cruiser has been sunk. This method will also update the armada instance's
	 * internal health as well as the internal associated sunk flag with the cruiser ship. This function will only
	 * return true once as it is supposed to indicate when a ship has just sunk rather than persistently holding this
	 * information. This is due to the controller's utilizing the armada needing to query for which ship has sunk by
	 * polling each is sunk method.
	 *
	 * @return boolean - A flag representing whether the cruiser ship has been sunk.
	 */
	public boolean isCruiserSunk()
	{
		// If the cruiser list is empty and its cruiser flag has not been set, the ship has just sunk.
		if (getCruiser().isEmpty() && !m_cruiserSunk)
		{
			m_health--;
			m_cruiserSunk = true;
			return true;
		}

		return false;
	}

	/**
	 * A boolish method to determine if the submarine has been sunk. This method will also update the armada instance's
	 * internal health as well as the internal associated sunk flag with the submarine ship. This function will only
	 * return true once as it is supposed to indicate when a ship has just sunk rather than persistently holding this
	 * information. This is due to the controller's utilizing the armada needing to query for which ship has sunk by
	 * polling each is sunk method.
	 *
	 * @return boolean - A flag representing whether the submarine ship has been sunk.
	 */
	public boolean isSubmarineSunk()
	{
		// If the submarine list is empty and its submarine flag has not been set, the ship has just sunk.
		if (getSubmarine().isEmpty() && !m_submarineSunk)
		{
			m_health--;
			m_submarineSunk = true;
			return true;
		}

		return false;
	}

	/**
	 * A boolish method to determine if the destroyer has been sunk. This method will also update the armada instance's
	 * internal health as well as the internal associated sunk flag with the destroyer ship. This function will only
	 * return true once as it is supposed to indicate when a ship has just sunk rather than persistently holding this
	 * information. This is due to the controller's utilizing the armada needing to query for which ship has sunk by
	 * polling each is sunk method.
	 *
	 * @return boolean - A flag representing whether the destroyer ship has been sunk.
	 */
	public boolean isDestroyerSunk()
	{
		// If the destroyer list is empty and its destroyer flag has not been set, the ship has just sunk.
		if (getDestroyer().isEmpty() && !m_destroyerSunk)
		{
			m_health--;
			m_destroyerSunk = true;
			return true;
		}

		return false;
	}

	/**
	 * A boolish method to determine if the carrier has been completely set.
	 *
	 * @return boolean - A flag representing whether the carrier ship has been completely set.
	 */
	public boolean isCarrierSet()
	{
		return getCarrier().size() == CARRIER_SIZE;
	}

	/**
	 * A boolish method to determine if the battleship has been completely set.
	 *
	 * @return boolean - A flag representing whether the battleship ship has been completely set.
	 */
	public boolean isBattleshipSet()
	{
		return getBattleship().size() == BATTLESHIP_SIZE;
	}

	/**
	 * A boolish method to determine if the cruiser has been completely set.
	 *
	 * @return boolean - A flag representing whether the cruiser ship has been completely set.
	 */
	public boolean isCruiserSet()
	{
		return getCruiser().size() == CRUISER_SIZE;
	}

	/**
	 * A boolish method to determine if the submarine has been completely set.
	 *
	 * @return boolean - A flag representing whether the submarine ship has been completely set.
	 */
	public boolean isSubmarineSet()
	{
		return getSubmarine().size() == SUBMARINE_SIZE;
	}

	/**
	 * A boolish method to determine if the destroyer has been completely set.
	 *
	 * @return boolean - A flag representing whether the destroyer ship has been completely set.
	 */
	public boolean isDestroyerSet()
	{
		return getDestroyer().size() == DESTROYER_SIZE;
	}

	/**
	 * Accessor for getting a string containing the positions within the carrier.
	 *
	 * @return String - A string representing the carriers' positions.
	 */
	public String getCarrierPositions()
	{
		return getPositions(m_carrier);
	}

	/**
	 * Accessor for getting a string containing the positions within the cruiser.
	 *
	 * @return String - A string representing the cruisers' positions.
	 */
	public String getCruiserPositions()
	{
		return getPositions(m_cruiser);
	}

	/**
	 * Accessor for getting a string containing the positions within the destroyer.
	 *
	 * @return String - A string representing the destroyers' positions.
	 */
	public String getDestroyerPositions()
	{
		return getPositions(m_destroyer);
	}

	/**
	 * Accessor for getting a string containing the positions within the submarine.
	 *
	 * @return String - A string representing the submarines' positions.
	 */
	public String getSubmarinePositions()
	{
		return getPositions(m_submarine);
	}

	/**
	 * Accessor for getting a string containing the positions within the battleship.
	 *
	 * @return String - A string representing the battleships' positions.
	 */
	public String getBattleshipPositions()
	{
		return getPositions(m_battleship);
	}

	/**
	 * Accessor for getting carrier list.
	 *
	 * @return List<String> - The carriers' position list.
	 */
	public List<String> getCarrier()
	{
		return m_carrier;
	}

	/**
	 * Accessor for getting battleship list.
	 *
	 * @return List<String> - The battleships' position list.
	 */
	public List<String> getBattleship()
	{
		return m_battleship;
	}

	/**
	 * Accessor for getting cruiser list.
	 *
	 * @return List<String> - The cruisers' position list.
	 */
	public List<String> getCruiser()
	{
		return m_cruiser;
	}

	/**
	 * Accessor for getting submarine list.
	 *
	 * @return List<String> - The submarines' position list.
	 */
	public List<String> getSubmarine()
	{
		return m_submarine;
	}

	/**
	 * Accessor for getting destroyer list.
	 *
	 * @return List<String> - The destroyers' position list.
	 */
	public List<String> getDestroyer()
	{
		return m_destroyer;
	}
}
