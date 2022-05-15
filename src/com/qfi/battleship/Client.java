package com.qfi.battleship;
// A Java program for a Client 
import java.net.*;

import javax.swing.JOptionPane;

import java.io.*; 
  
public class Client implements Runnable, Observable, Observer
{ 
	
    // initialize socket and input output streams 
    private Socket socket            = null; 
    private DataInputStream  input   = null; 
    private DataOutputStream out     = null; 
    private static int count = 0;
    private String address;
    private int port;
    private static BoardController controller;
    private int turn = 0;
    private static boolean over = false;
    private static boolean sSending = false;
    private static boolean ShipsSet = false;
    private static boolean CarrierSet = false;
    private static boolean BattleshipSet = false;
    private static boolean CruiserSet = false;
    private static boolean SubmarineSet = false;
    private static boolean DestroyerSet = false;
    private static Observer cCobs;
  
    // constructor to put ip address and port 
    public Client(String address, int port) 
    { 
    	this.address = address;
    	this.port = port;
    	controller = new BoardController(1);
    	controller.registerObserver((Observer)this);
    	registerObserver(controller);
    	
    	System.out.println(controller.getID());
    } 
  
    public BoardController getController()
    {
    	return controller;
    }
    
    public static void infoBox(String infoMessage, String titleBar)
    {
        JOptionPane.showMessageDialog(null, infoMessage, "Client: " + titleBar, JOptionPane.INFORMATION_MESSAGE);
    }
    
    @Override
    public void run() {
    	 // establish a connection 
        try
        { 
            socket = new Socket(address, port); 
            System.out.println("Connected"); 
  
            // takes input from terminal 
            input  = new DataInputStream(socket.getInputStream()); 
  
            // sends output to the socket 
            out    = new DataOutputStream(socket.getOutputStream()); 
        } 
        catch(UnknownHostException u) 
        { 
            System.out.println(u); 
        } 
        catch(IOException i) 
        { 
            System.out.println(i); 
        }
  
        // string to read message from input 
        String line = ""; 
  
        // keep reading until "Over" is input 
        while (!GameOver()) 
        {	 
            try
            {
            	if(count == 0) {
            	turn = input.readInt();
            	out.writeUTF("Client recieved " + turn + " from Server...");
            	out.flush();
            	controller.setCurrentTurn(turn);
            	count++;
            	isShipsSet(ShipsSet);
            	}
            	else {
            		line = input.readUTF();
            		System.out.println("Client: Recieved " + line + " from Server.");
            		receiveFromServer(line);
            		notifyObserver(line);
            	}
            	
            } 
            catch(IOException i) 
            { 
                System.out.println(i); 
            } 
        }
        
  
        // close the connection 
        try
        { 
            input.close(); 
            out.close(); 
            socket.close(); 
        } 
        catch(IOException i) 
        { 
            System.out.println(i); 
        }
        if(GameOver())
        	System.exit(0);
    }
    
    public void receiveFromServer(String l) {
		if(l.equals("OVER")) {
			infoBox("You Won! (:", "Player 1");
			try {
				Thread.sleep(1000);
				System.exit(0);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		else if(l.equals("CARRIER")) {
			infoBox("You sunk your opponents Carrier!","Player 1");
		}
		else if(l.equals("BATTLESHIP")) {
			infoBox("You sunk your opponents Battleship!","Player 1");
		}
		else if(l.equals("CRUISER")) {
			infoBox("You sunk your opponents Cruiser!","Player 1");
		}
		else if(l.equals("SUBMARINE")) {
			infoBox("You sunk your opponents Submarine!","Player 1");
		}
		else if(l.equals("DESTROYER")) {
			infoBox("You sunk your opponents Destroyer!","Player 1");
		}
    }
    
    public void isShipsSet(boolean isSet) { 
    	while(!isSet) {
    		try
    		{
    			Thread.sleep(500);
    		}
    		catch (Throwable t)
    		{
    			
    		}
    		
    		System.out.println("");
      	if(controller.getArmada().isCarrierSet()) {
      		CarrierSet = true;
      		System.out.println("Carrier is Set.");
      	}
      	
      	if(controller.getArmada().isBattleshipSet()) {
      		BattleshipSet = true;
      		System.out.println("Battleship is Set.");
      	}
      	
      	if(controller.getArmada().isCruiserSet()) {
      		CruiserSet = true;
      		System.out.println("Cruiser is Set.");
      	}
      	
      	if(controller.getArmada().isSubmarineSet()) {
      		SubmarineSet = true;
      		System.out.println("Submarine is Set.");
      	}
      	
      	if(controller.getArmada().isDestroyerSet()) {
      		DestroyerSet = true;
      		System.out.println("Destroyer is Set.");
      	}
      	
      	if(CarrierSet && BattleshipSet && CruiserSet &&
      			SubmarineSet && DestroyerSet) {
      		System.out.println("SET");
      		isSet = true;
      		notifyObserver("SET");
      		controller.getArmada().logArmadaPosition();
      		infoBox("Ships are Set!", "Player 1");
      	}
      	
      }
    }
    
    public static boolean GameOver() {
    	if(over)
    		return true;
    	else
    		return false;
    }
    
    public static void setGameOver(boolean g) {
    	over = g;
    }
    
    
    public boolean ServerSending() {
    	return sSending;
    }
    public static void serverControllerSending(boolean val) {
    	sSending = val;
    }

	@Override
	public void registerObserver(Observer c) {
		cCobs = c;
		
	}

	@Override
	public void removeObserver() {
		cCobs = null;
		
	}

	@Override
	public void notifyObserver(String s) {
		cCobs.update(s);
		
	}

	@Override
	public void update(String s) {
		System.out.println("Recieved " + s + " from CC.");
		StringBuilder t = new StringBuilder(s);
		updateResponse(s, t);
	}
	
	public void updateResponse(String str, StringBuilder build) {
		if(str.equals("SHIPS")) {
			CarrierSet = true;
			BattleshipSet = true;
			CruiserSet = true;
			SubmarineSet = true;
			DestroyerSet = true;
			ShipsSet = true;
		}
		else if(str.equals("OVER")) {
			infoBox("You lost :(", "Player 1");
			over = true;
		}
		else if(str.equals("CARRIER")) {
			infoBox("Your Carrier has been sunk!","Player 1");
		}
		else if(str.equals("BATTLESHIP")) {
			infoBox("Your Battleship has been sunk!","Player 1");
		}
		else if(str.equals("CRUISER")) {
			infoBox("Your Cruiser has been sunk!","Player 1");
		}
		else if(str.equals("SUBMARINE")) {
			infoBox("Your Submarine has been sunk!","Player 1");
		}
		else if(str.equals("DESTROYER")) {
			infoBox("Your Destroyer has been sunk!","Player 1");
		}
		else if(build.length() == 4) {
			if(build.charAt(3) == '1') {
				build.deleteCharAt(3);
				str = build.toString();
				turn = 1;
			}
		}
		else if(build.length() == 5) {
			if(build.charAt(4) == '1') {
				build.deleteCharAt(4);
				str = build.toString();
				turn = 1;
			}
		}
		
			try {
				if(!str.equals("SHIPS")) {
				out.writeUTF(str);
				out.flush();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
} 