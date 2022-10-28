package com.qfi.battleship;

import java.util.List;
import java.util.ArrayList;
import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;

/**
 * 
 * @author Vincent.Nigro
 * @version 1.0.0
 */
public class Armada
{
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
	
	private boolean carrierSunk;
	private boolean battleshipSunk;
	private boolean cruiserSunk;
	private boolean submarineSunk;
	private boolean destroyerSunk;
	private int health;
	private List<String> m_carrier = new ArrayList<String>();
	private List<String> m_cruiser = new ArrayList<String>();
	private List<String> m_submarine = new ArrayList<String>();
	private List<String> m_destroyer = new ArrayList<String>();
	private List<String> m_battleship = new ArrayList<String>();
	private Logger m_logger = LogManager.getLogger(Armada.class);
	
	/**
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
	 * Armada constructor.
	 */
	public Armada()
	{
		health = 5;
		carrierSunk = false;
		cruiserSunk = false;
		submarineSunk = false;
		destroyerSunk = false;
		battleshipSunk = false;
	}
	
	/**
	 * 
	 * @param hitTarget
	 * @return
	 */
	public boolean calculateHit(String hitTarget)
	{
		if (m_destroyer.contains(hitTarget))
		{
			return true;
		}
		else if (m_submarine.contains(hitTarget))
		{
			return true;
		}
		else if (m_cruiser.contains(hitTarget))
		{
			return true;
		}
		else if (m_battleship.contains(hitTarget))
		{
			return true;
		}
		else if (m_carrier.contains(hitTarget))
		{
			return true;
		}
		
		return false;
		
	}
	
	/**
	 * 
	 * @param hitTarget
	 */
	public void updateArmada(String hitTarget)
	{	
		if (m_destroyer.contains(hitTarget))
		{
			m_destroyer.remove(hitTarget);
		}
		else if (m_submarine.contains(hitTarget))
		{
			m_submarine.remove(hitTarget);
		}
		else if (m_cruiser.contains(hitTarget))
		{
			m_cruiser.remove(hitTarget);
		}
		else if (m_battleship.contains(hitTarget))
		{
			m_battleship.remove(hitTarget);
		}
		else if (m_carrier.contains(hitTarget))
		{
			m_carrier.remove(hitTarget);
		}
		
	}
	
	public boolean isGameOver() {
		if(isArmadaSunk())
			return true;
		return false;	
	}
	
	public boolean isArmadaSunk() {
		if(health == 0 && carrierSunk && battleshipSunk &&
				cruiserSunk && submarineSunk && destroyerSunk) {
			return true;
		}
		else
			return false;
	}
	
	public boolean isCarrierSunk() {
		if(getCarrier().size() == 0 && !carrierSunk) {
			health--;
			carrierSunk = true;
			return true;
		}
		return false;
	}
	
	public boolean isBattleshipSunk() {
		if(getBattleship().size() == 0 && !battleshipSunk) {
			health--;
			battleshipSunk = true;
			return true;
		}
		return false;
	}
	
	public boolean isCruiserSunk() {
		if(getCruiser().size() == 0 && !cruiserSunk) {
			health--;
			cruiserSunk = true;
			return true;
		}
		return false;
	}
	
	public boolean isSubmarineSunk() {
		if(getSubmarine().size() == 0 && !submarineSunk) {
			health--;
			submarineSunk = true;
			return true;
		}
		return false;
	}
	
	public boolean isDestroyerSunk() {
		if(getDestroyer().size() == 0 && !destroyerSunk) {
			health--;
			destroyerSunk = true;
			return true;
		}
		return false;
	}
	
	public boolean isCarrierSet() {
		if(getCarrier().size() == 0)
			return false;
		else
			return true;
	}
	public boolean isBattleshipSet() {
		if(getBattleship().size() == 0)
			return false;
		else
			return true;
	}
	public boolean isCruiserSet() {
		if(getCarrier().size() == 0)
			return false;
		else
			return true;
	}
	public boolean isSubmarineSet() {
		if(getSubmarine().size() == 0)
			return false;
		else
			return true;
	}
	public boolean isDestroyerSet() {
		if(getDestroyer().size() == 0)
			return false;
		else
			return true;
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
	 * @param type An ArmadaType enum representing the type of ship that is being logged.
	 * @param ship
	 */
	private void logPosition(ArmadaType type, List<String> ship)
	{
		String shipPositions = "";
		
		for (String position : ship)
		{
			shipPositions += position + " ";
		}
		
		m_logger.debug(type + " Position: " + shipPositions);
	}
	
	/**
	 * Adds a list strings representing positions for the carrier ship. If the Armada member list for carrier
	 * is empty and the parameter list provided is the same size as the expected carrier size the parameter list will
	 * be added into the internal member list for the carrier.
	 * 
	 * @param carrierPos A list of strings representing multiple blocks for a carrier.
	 */
	public void addToCarrier(List<String> carrierPos)
	{
		if (m_carrier.isEmpty())
		{
			m_carrier.addAll(carrierPos);
		}
		else if (carrierPos.size() != CARRIER_SIZE)
		{
			m_logger.debug("Attempted to add " + carrierPos.size() + " positions when only needed " + CARRIER_SIZE +
							", received positions: " + carrierPos + ".");
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
	 * @param battleshipPos A list of strings representing multiple blocks for a battleship.
	 */
	public void addToBattleship(List<String> battleshipPos)
	{
		if (m_battleship.isEmpty())
		{
			m_battleship.addAll(battleshipPos);
		}
		else if (battleshipPos.size() != BATTLESHIP_SIZE)
		{
			m_logger.debug("Attempted to add " + battleshipPos.size() + " positions when only needed " + BATTLESHIP_SIZE
							+ ", received positions: " + battleshipPos + ".");
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
	 * @param cruiserPos A list of strings representing multiple blocks for a cruiser.
	 */
	public void addToCruiser(List<String> cruiserPos)
	{
		if (m_cruiser.isEmpty())
		{
			m_cruiser.addAll(cruiserPos);
		}
		else if (cruiserPos.size() != CRUISER_SIZE)
		{
			m_logger.debug("Attempted to add " + cruiserPos.size() + " positions when only needed " + CRUISER_SIZE +
							", received positions: " + cruiserPos + ".");
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
	 * @param submarinePos A list of strings representing multiple blocks for a submarine.
	 */
	public void addToSubmarine(List<String> submarinePos)
	{
		if (m_submarine.isEmpty())
		{
			m_submarine.addAll(submarinePos);
		}
		else if (submarinePos.size() != SUBMARINE_SIZE)
		{
			m_logger.debug("Attempted to add " + submarinePos.size() + " positions when only needed " + SUBMARINE_SIZE
							+ ", received positions: " + submarinePos + ".");
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
	 * @param destroyerPos A list of strings representing multiple blocks for a destroyer.
	 */
	public void addToDestroyer(List<String> destroyerPos)
	{
		if (m_destroyer.isEmpty() && destroyerPos.size() == DESTROYER_SIZE)
		{
			m_destroyer.addAll(destroyerPos);
		}
		else if (destroyerPos.size() != DESTROYER_SIZE)
		{
			m_logger.debug("Attempted to add " + destroyerPos.size() + " positions when only needed " + DESTROYER_SIZE
							+ ", received positions: " + destroyerPos + ".");
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
	 * @param pos A string representing a single block position for a carrier.
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
	 * @param pos A string representing a single block position for a battleship.
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
	 * @param pos A string representing a single block position for a cruiser.
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
	 * @param pos A string representing a single block position for a submarine.
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
	 * @param pos A string representing a single block position for a destroyer.
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
	 * Accessor for getting carrier list.
	 * 
	 * @return List<String>
	 */
	public List<String> getCarrier()
	{
		return m_carrier;
	}
	
	/**
	 * Accessor for getting battleship list.
	 * 
	 * @return List<String>
	 */
	public List<String> getBattleship()
	{
		return m_battleship;
	}
	
	/**
	 * Accessor for getting cruiser list.
	 * 
	 * @return List<String>
	 */
	public List<String> getCruiser()
	{
		return m_cruiser;
	}
	
	/**
	 * Accessor for getting submarine list.
	 * 
	 * @return List<String>
	 */
	public List<String> getSubmarine()
	{
		return m_submarine;
	}
	
	/**
	 * Accessor for getting destroyer list.
	 * 
	 * @return List<String>
	 */
	public List<String> getDestroyer()
	{
		return m_destroyer;
	}
}
