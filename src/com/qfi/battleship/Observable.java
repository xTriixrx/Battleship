package com.qfi.battleship;

/**
 * 
 * @author Vincent Nigro
 * @version 1.0.0
 */
public interface Observable
{
	public void removeObserver();
	public void notifyObserver(String s);
	public void registerObserver(Observer s);
}
