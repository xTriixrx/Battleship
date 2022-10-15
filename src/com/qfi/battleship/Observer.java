package com.qfi.battleship;

/**
 * An Observer interface that authorizes communication between two components; a communication logic component and a
 * board controller. 
 * 
 * @author Vincent.Nigro
 * @version 1.0.0
 */
public interface Observer
{
	/**
	 * An update message for the observer to act on.
	 * 
	 * @param update - A message from the observable component sending an update to the observer.
	 */
	public void update(String update);
}
