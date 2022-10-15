package com.qfi.battleship;

/**
 * The main method class wrapper that is instantiated at start up. Will indirectly call the GUIDriver's main method to
 * launch the Application.
 * 
 * @author Vincent.Nigro
 * @version 1.0.0
 */
public class GUIDriverRunner
{
	/**
	 * Main method that is called from the command line with keyword command line arguments. Will call GUIDriver's main
	 * as the command line cannot directly call the GUIDriver due to JavaFX Application lifecycle reasons.
	 * 
	 * @param args - Keyword arguments from the command line.
	 */
	public static void main(String[] args)
	{
		GUIDriver.main(args);
	}
}
