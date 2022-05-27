package com.qfi.battleship;

/**
 * 
 * @author Vincent Nigro
 * @version 1.0.0
 */
public interface Observable
{
	public void notifyObserver(String update);
	public void registerObserver(Observer observer);
}
