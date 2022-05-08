package com.qfi.battleship;

import java.util.ArrayList;

import javafx.scene.control.Button;

public class Armada {
	
	private ArrayList<String> carrier = new ArrayList<String>();
	private ArrayList<String> battleship = new ArrayList<String>();
	private ArrayList<String> cruiser = new ArrayList<String>();
	private ArrayList<String> submarine = new ArrayList<String>();
	private ArrayList<String> destroyer = new ArrayList<String>();
	private boolean carrierSunk;
	private boolean battleshipSunk;
	private boolean cruiserSunk;
	private boolean submarineSunk;
	private boolean destroyerSunk;
	private int health;
	private boolean over;
	
	public Armada() {
		health = 5;
		over = false;
		carrierSunk = false;
		battleshipSunk = false;
		cruiserSunk = false;
		submarineSunk = false;
		destroyerSunk = false;
	}
	
	public boolean calculateHit(String hitTarget) {
		
		boolean isHit = false;
		
		for(int i = 0; i < this.getBattleShip().size(); i++) {
			if(hitTarget.charAt(1) == this.getBattleShip().get(i).charAt(0)
					&& hitTarget.charAt(2) == this.getBattleShip().get(i).charAt(1)) {
				isHit = true;
			}
		}
		
		if(!isHit) {
			for(int i = 0; i < this.getCarrier().size(); i++) {
				if(hitTarget.charAt(1) == this.getCarrier().get(i).charAt(0)
						&& hitTarget.charAt(2) == this.getCarrier().get(i).charAt(1)) {
					isHit = true;
				}
			}
		}
		
		if(!isHit) {
			for(int i = 0; i < this.getCruiser().size(); i++) {
				if(hitTarget.charAt(1) == this.getCruiser().get(i).charAt(0)
						&& hitTarget.charAt(2) == this.getCruiser().get(i).charAt(1)) {
					isHit = true;
				}
			}
		}
		
		if(!isHit) {
			for(int i = 0; i < this.getSubmarine().size(); i++) {
				if(hitTarget.charAt(1) == this.getSubmarine().get(i).charAt(0)
						&& hitTarget.charAt(2) == this.getSubmarine().get(i).charAt(1)) {
					isHit = true;
				}
			}
		}
		
		if(!isHit) {
			for(int i = 0; i < this.getDestroyer().size(); i++) {
				if(hitTarget.charAt(1) == this.getDestroyer().get(i).charAt(0)
						&& hitTarget.charAt(2) == this.getDestroyer().get(i).charAt(1)) {
					isHit = true;
				}
			}
		}
		
		return isHit;
		
	}
	
	public void updateArmada(String hitTarget) {
		
		boolean isHit = false;
		
		for(int i = 0; i < this.getBattleShip().size(); i++) {
			if(hitTarget.charAt(1) == this.getBattleShip().get(i).charAt(0)
					&& hitTarget.charAt(2) == this.getBattleShip().get(i).charAt(1)) {
				this.battleship.remove(i);
			}
		}
		
		if(!isHit) {
			for(int i = 0; i < this.getCarrier().size(); i++) {
				if(hitTarget.charAt(1) == this.getCarrier().get(i).charAt(0)
						&& hitTarget.charAt(2) == this.getCarrier().get(i).charAt(1)) {
					this.carrier.remove(i);
				}
			}
		}
		
		if(!isHit) {
			for(int i = 0; i < this.getCruiser().size(); i++) {
				if(hitTarget.charAt(1) == this.getCruiser().get(i).charAt(0)
						&& hitTarget.charAt(2) == this.getCruiser().get(i).charAt(1)) {
					this.cruiser.remove(i);
				}
			}
		}
		
		if(!isHit) {
			for(int i = 0; i < this.getSubmarine().size(); i++) {
				if(hitTarget.charAt(1) == this.getSubmarine().get(i).charAt(0)
						&& hitTarget.charAt(2) == this.getSubmarine().get(i).charAt(1)) {
					this.submarine.remove(i);
				}
			}
		}
		
		if(!isHit) {
			for(int i = 0; i < this.getDestroyer().size(); i++) {
				if(hitTarget.charAt(1) == this.getDestroyer().get(i).charAt(0)
						&& hitTarget.charAt(2) == this.getDestroyer().get(i).charAt(1)) {
					this.destroyer.remove(i);
				}
			}
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
			over = true;
			return true;
		}
		else
			return false;
	}
	
	public boolean isCarrierSunk() {
		if(getCarrier().size() == 0) {
			health--;
			carrierSunk = true;
			return true;
		}
		return false;
	}
	
	public boolean isBattleshipSunk() {
		if(getBattleShip().size() == 0) {
			health--;
			battleshipSunk = true;
			return true;
		}
		return false;
	}
	
	public boolean isCruiserSunk() {
		if(getCruiser().size() == 0) {
			health--;
			cruiserSunk = true;
			return true;
		}
		return false;
	}
	
	public boolean isSubmarineSunk() {
		if(getSubmarine().size() == 0) {
			health--;
			submarineSunk = true;
			return true;
		}
		return false;
	}
	
	public boolean isDestroyerSunk() {
		if(getDestroyer().size() == 0) {
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
		if(getBattleShip().size() == 0)
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
	
	public ArrayList<String> getCarrier(){
		return carrier;
	}
	
	public ArrayList<String> getBattleShip(){
		return battleship;
	}
	
	public ArrayList<String> getCruiser(){
		return cruiser;
	}
	
	public ArrayList<String> getSubmarine(){
		return submarine;
	}
	
	public ArrayList<String> getDestroyer(){
		return destroyer;
	}
	
	public void DisplayArmadaPosition() {
		System.out.println("Destroyer Position:");
		DisplayDestroyerPosition();
		
		System.out.println("Submarine Position: ");
		DisplaySubmarinePosition();
		
		System.out.println("Cruiser Position: ");
		DisplayCruiserPosition();
		
		System.out.println("Battleship Position: ");
		DisplayBattleshipPosition();
		
		System.out.println("Carrier Position: ");
		DisplayCarrierPosition();
	}
	
	public void DisplayCarrierPosition() {
		for(int i = 0; i < carrier.size(); i++) {
			System.out.print(carrier.get(i) + " ");
		}
		System.out.println();
	}
	
	public void DisplayBattleshipPosition() {
		for(int i = 0; i < battleship.size(); i++) {
			System.out.print(battleship.get(i) + " ");
		}
		System.out.println();
	}
	
	public void DisplayDestroyerPosition() {
		for(int i = 0; i < destroyer.size(); i++) {
			System.out.print(destroyer.get(i) + " ");
		}
		System.out.println();
	}
	
	public void DisplaySubmarinePosition() {
		for(int i = 0; i < submarine.size(); i++) {
			System.out.print(submarine.get(i) + " ");
		}
		System.out.println();
	}
	
	public void DisplayCruiserPosition() {
		for(int i = 0; i < cruiser.size(); i++) {
			System.out.print(cruiser.get(i) + " ");
		}
		System.out.println();
	}
	
	public void addToCarrier(String pos) {
		if(carrier.size() < 5) {
			carrier.add(pos);
		}else {
			System.out.println("Carrier position is already set");
		}
	}
	
	public void addToBattleship(String pos) {
		if(battleship.size() < 4) {
			battleship.add(pos);
		}else {
			System.out.println("Battleship position is already set");
		}
	}
	
	public void addToCruiser(String pos) {
		if(cruiser.size() < 3) {
			cruiser.add(pos);
		}else {
			System.out.println("Cruise position is already set");
		}
	}
	
	public void addToSubmarine(String pos) {
		if(submarine.size() < 3) {
			submarine.add(pos);
		}else {
			System.out.println("Submarine position is already set");
		}
	}
	
	public void addToDestroyer(String pos) {
		if(destroyer.size() < 2) {
			destroyer.add(pos);
		}else {
			System.out.println("Destroyer position is already set");
		}
	}

}
