package com.qfi.battleship;

public interface Controller
{
	public int getID();
	public void shutdown();
	public Armada getArmada();
	public int getCurrentTurn();
	public void setCurrentTurn(int turn);
}
