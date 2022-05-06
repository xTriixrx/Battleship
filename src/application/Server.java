package application;
// A Java program for a Server 
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.swing.JOptionPane;

import java.io.*; 
  
public class Server extends Thread implements Observable, Observer
{ 
    //initialize socket and input stream 
    private Socket          socket   = null; 
    private ServerSocket    server   = null; 
    private DataInputStream in       =  null;
    private DataOutputStream out 	 = null;
    private static boolean over = false;
    private static int count = 0;
    private int port;
    private static Scontroller c;
    private int turn = 0;
    private static boolean ShipsSet = false;
    private static boolean CarrierSet = false;
    private static boolean BattleshipSet = false;
    private static boolean CruiserSet = false;
    private static boolean SubmarineSet = false;
    private static boolean DestroyerSet = false;
    public static Observer sCobs;
  
    // constructor with port 
    public Server(int port) 
    { 
    	this.port = port;
    	c = new Scontroller();
    	c.registerObserver((Observer)this);
    	registerObserver(c);
    	c.setServer(this);
    	System.out.println(c.getID());
    	turn = (int) (Math.random() * 2 + 1);
    	c.setTurn(turn);
    }
    
    public DataInputStream getServerInput() {
    	return in;
    }
    
    public DataOutputStream getServerOutput() {
    	return out;
    }
    
    public static void infoBox(String infoMessage, String titleBar)
    {
        JOptionPane.showMessageDialog(null, infoMessage, "Server: " + titleBar, JOptionPane.INFORMATION_MESSAGE);
    }
    
    @Override
    public void run() {
    	try
        { 
            server = new ServerSocket(port); 
            System.out.println("Server started"); 
            System.out.println(ServerAddress());
            System.out.println("Waiting for a client ..."); 
  
            socket = server.accept(); 
            System.out.println("Client accepted"); 
  
            // takes input from the client socket 
            in = new DataInputStream( 
                new BufferedInputStream(socket.getInputStream()));
            out = new DataOutputStream(
            		new BufferedOutputStream(socket.getOutputStream()));
            String line = "";
            
            c.setServerInputStream(in);
            c.setServerOutputStream(out);
            
  
            // reads message from client until "Over" is sent 
            while (!GameOver()) 
            {
            	
                try
                { 
                	if(count == 0) {
                		out.writeInt(turn);
                		out.flush();
		                line = in.readUTF(); 
		                System.out.println(line);
		                count++;
		                isShipsSet(ShipsSet);
                	}
                	else {
                		line = in.readUTF();
                		System.out.println("Server: Received " + line + " from Client.");
                		receiveFromClient(line);
                		notifyObserver(line);
                	}
                } 
                catch(IOException i) 
                { 
                    System.out.println(i); 
                } 
            } 
            System.out.println("Closing connection"); 
  
            // close connection 
            socket.close(); 
            in.close(); 
        }
        catch(IOException i) 
        { 
            System.out.println(i); 
        }
    	if(GameOver())
    		System.exit(0);
    }
    
    public void receiveFromClient(String l) {
    	if(l.equals("OVER")) {
			infoBox("You Won! (:", "Player 2");
			try {
				Thread.sleep(1000);
				System.exit(0);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		else if(l.equals("CARRIER")) {
			infoBox("You sunk your opponents Carrier!","Player 2");
		}
		else if(l.equals("BATTLESHIP")) {
			infoBox("You sunk your opponents Battleship!","Player 2");
		}
		else if(l.equals("CRUISER")) {
			infoBox("You sunk your opponents Cruiser!","Player 2");
		}
		else if(l.equals("SUBMARINE")) {
			infoBox("You sunk your opponents Submarine!","Player 2");
		}
		else if(l.equals("DESTROYER")) {
			infoBox("You sunk your opponents Destroyer!","Player 2");
		}
    }
    
    public void isShipsSet(boolean isSet) {
    	while(!isSet) {
    		System.out.println("");
    	if(c.getArmada().isCarrierSet()) {
    		CarrierSet = true;
    		System.out.println("Carrier is Set.");
    	}
    	
    	if(c.getArmada().isBattleshipSet()) {
    		BattleshipSet = true;
    		System.out.println("Battleship is Set.");
    	}
    	
    	if(c.getArmada().isCruiserSet()) {
    		CruiserSet = true;
    		System.out.println("Cruiser is Set.");
    	}
    	
    	if(c.getArmada().isSubmarineSet()) {
    		SubmarineSet = true;
    		System.out.println("Submarine is Set.");
    	}
    	
    	if(c.getArmada().isDestroyerSet()) {
    		DestroyerSet = true;
    		System.out.println("Destroyer is Set.");
    	}
    	
    	if(CarrierSet && BattleshipSet && CruiserSet &&
    			SubmarineSet && DestroyerSet) {
    		System.out.println("SET");
    		isSet = true;
    		notifyObserver("SET");
    		infoBox("Ships are Set!", "Player 2");
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
    public static String ServerAddress() throws UnknownHostException {
    	InetAddress temp = InetAddress.getByName(InetAddress.getLocalHost().getHostName());
    	String[] hostPieces = temp.toString().split("/");
    	String host = hostPieces[1];
    	return host;
    }

	@Override
	public void update(String s) {
		System.out.println("Server: Recieved " + s + " from SC.");
		StringBuilder t = new StringBuilder(s);
		updateResponse(s,t);
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
			infoBox("You lost :(", "Player 2");
			over = true;
		}
		else if(str.equals("CARRIER")) {
			infoBox("Your Carrier has been sunk!","Player 2");
		}
		else if(str.equals("BATTLESHIP")) {
			infoBox("Your Battleship has been sunk!","Player 2");
		}
		else if(str.equals("CRUISER")) {
			infoBox("Your Cruiser has been sunk!","Player 2");
		}
		else if(str.equals("SUBMARINE")) {
			infoBox("Your Submarine has been sunk!","Player 2");
		}
		else if(str.equals("DESTROYER")) {
			infoBox("Your Destroyer has been sunk!","Player 2");
		}
		else if(build.length() == 4) {
			if(build.charAt(3) == '2') {
				build.deleteCharAt(3);
				str = build.toString();
				turn = 2;
			}
		}
		else if(build.length() == 5) {
			if(build.charAt(4) == '2') {
				build.deleteCharAt(4);
				str = build.toString();
				turn = 2;
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

	@Override
	public void registerObserver(Observer c) {
		sCobs = c;
		
	}

	@Override
	public void removeObserver() {
		sCobs = null;
	}

	@Override
	public void notifyObserver(String s) {
		sCobs.update(s);
		
	}
	
    public static void main(String args[]) throws UnknownHostException 
    { 
    	Thread server = new Server(5000);
    	server.start();
    	sMain.launchGUI(args, (Server) server, c);
    }
}
