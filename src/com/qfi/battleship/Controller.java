package com.qfi.battleship;

/**
 * A Controller interface that manages the GUI board logic and interacts with some Player object through
 * the Observer & Observable interfaces.
 *
 * @author Vincent.Nigro
 * @version 1.0.0
 */
public interface Controller
{
	/**
	 * Returns some sort of unique id for some controller object in order to uniquely identify
	 * the controller.
	 *
	 * @return int - A unique id for a conforming controller.
	 */
	public int getID();

	/**
	 * A method in order to shut down the controller.
	 */
	public void shutdown();

	/**
	 * Returns the current turn.
	 *
	 * @return int - An integer representing the current turn of either the server or client.
	 */
	public int getCurrentTurn();

	/**
	 * Sets the current turn.
	 *
	 * @param turn - The turn for which the internal turn value should be set to.
	 */
	public void setCurrentTurn(int turn);
}
