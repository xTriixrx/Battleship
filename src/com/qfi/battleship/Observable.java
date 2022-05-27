package com.qfi.battleship;

/**
 * An Observable interface that authorizes communication between two components; a communication logic component and a
 * board controller.
 * 
 * @author Vincent.Nigro
 * @version 1.0.0
 */
public interface Observable
{	
	/**
	 * Registers a reference to the Observer instance that the Observable needs to update.
	 * 
	 * @param observer An Observer instance that the Observable instance needs to update.
	 */
	public void register(Observer observer);
}
