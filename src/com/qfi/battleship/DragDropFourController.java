package com.qfi.battleship;

import javafx.scene.Node;
import javafx.scene.control.Button;

public class DragDropFourController {

	public boolean unhighlightLengthFourVerticalTargetThree(Node button, Button target, char one, char two, char three) {
		return (button.getId().toString().charAt(2) == one)
		&& (button.getId().toString().charAt(1) == target.getId().toString().charAt(1))
		|| (button.getId().toString().charAt(2) == two) && (button.getId().toString()
				.charAt(1) == target.getId().toString().charAt(1))
		|| (button.getId().toString().charAt(2) == three) && (button.getId().toString()
				.charAt(1) == target.getId().toString().charAt(1));
	}
	
	
	public boolean unhighlightLengthFourVerticalTargetThreeSepcialCaseOne(Node button, Button target, char one, char two) {
		return (button.getId().toString().charAt(2) == one)
		&& (button.getId().toString().charAt(1) == target.getId().toString().charAt(1))
		|| (button.getId().toString().charAt(2) == two) && (button.getId().toString()
				.charAt(1) == target.getId().toString().charAt(1));
	}
	
	public boolean unhighlightLengthFourVerticalTargetThreeSpecialCaseTwo(Node button, Button target, char one, char two) {
		return (button.getId().toString().charAt(2) == one)
				&& (button.getId().toString().charAt(3) == two)
				&& (button.getId().toString().charAt(1) == target.getId().toString().charAt(1));
	}
	
	public boolean unhighlightLengthFourHorizontalTargetThree(Node button, Button target, char one, char two, char three) {
		return (button.getId().toString().charAt(1) == one)
				&& (button.getId().toString().charAt(2) == target.getId().toString().charAt(2))
				|| (button.getId().toString().charAt(1) == two) && (button.getId().toString()
						.charAt(2) == target.getId().toString().charAt(2))
				|| (button.getId().toString().charAt(1) == three) && (button.getId().toString()
						.charAt(2) == target.getId().toString().charAt(2));
	}
	
	public boolean unhighlightLengthFourHorizontalTargetFourCaseA(Node button, Button target, char one, char two, char three) {
		return (button.getId().toString().charAt(1) == one)
				&& (button.getId().toString().charAt(2) == target.getId().toString().charAt(2))
				|| (button.getId().toString().charAt(1) == two) && (button.getId().toString()
						.charAt(2) == target.getId().toString().charAt(2))
				|| (button.getId().toString().charAt(1) ==  three) && (button.getId().toString()
						.charAt(2) == target.getId().toString().charAt(2));
	}
	
	public boolean unhighlightLengthFourHorizontalTargetFour(Node button, Button target, char one, char two, char three) {
		return (button.getId().toString().charAt(1) == one)
		&& (button.getId().toString().charAt(2) == target.getId().toString().charAt(2))
		&& button.getId().toString().charAt(3) == target.getId().toString().charAt(3)
		|| (button.getId().toString().charAt(1) == two)
				&& (button.getId().toString().charAt(2) == target.getId().toString()
						.charAt(2))
				&& button.getId().toString().charAt(3) == target.getId().toString()
						.charAt(3)
		|| (button.getId().toString().charAt(1) == three) && (button.getId().toString()
				.charAt(2) == target.getId().toString().charAt(2)
				&& button.getId().toString().charAt(3) == target.getId().toString()
						.charAt(3));
	}
	
	public boolean highlightLengthFourHorizontalTargetThree(Node button, Button target, char one, char two, char three) {
		return (button.getId().toString().charAt(1) == one)
		&& (button.getId().toString().charAt(2) == target.getId().toString().charAt(2))
		|| (button.getId().toString().charAt(1) == two) && (button.getId().toString()
				.charAt(2) == target.getId().toString().charAt(2))
		|| (button.getId().toString().charAt(1) == three) && (button.getId().toString()
				.charAt(2) == target.getId().toString().charAt(2));
	}
	
	public boolean highlightLengthFourHorizontalTargetFour(Node button, Button target, char one, char two, char three) {
		return (button.getId().toString().charAt(1) == one)
				&& (button.getId().toString().charAt(2) == target.getId().toString().charAt(2)
				&& button.getId().toString().charAt(3) == target.getId().toString()
						.charAt(3))
		|| (button.getId().toString().charAt(1) == two) && (button.getId().toString()
				.charAt(2) == target.getId().toString().charAt(2)
				&& button.getId().toString().charAt(3) == target.getId().toString()
						.charAt(3))
		|| (button.getId().toString().charAt(1) == three) && (button.getId().toString()
				.charAt(2) == target.getId().toString().charAt(2)
				&& button.getId().toString().charAt(3) == target.getId().toString()
						.charAt(3));
	}
	
	public boolean highlightLengthFourVerticalTargetThree(Node button, Button target, char one, char two, char three) {
		return (button.getId().toString().charAt(2) == one)
				&& (button.getId().toString().charAt(1) == target.getId().toString().charAt(1))
				|| (button.getId().toString().charAt(2) == two) && (button.getId().toString()
						.charAt(1) == target.getId().toString().charAt(1))
				|| (button.getId().toString().charAt(2) == three) && (button.getId().toString()
						.charAt(1) == target.getId().toString().charAt(1));
	}
	
	public boolean highlightLengthFourVerticalTargetThreeSpecialCaseOne(Node button, Button target, char one, char two) {
		return (button.getId().toString().charAt(2) == one)
		&& (button.getId().toString().charAt(1) == target.getId().toString().charAt(1))
		|| (button.getId().toString().charAt(2) == two) && (button.getId().toString()
				.charAt(1) == target.getId().toString().charAt(1));
	}
	
	public boolean highlightLengthFourVerticalTargetThreeSpecialCaseTwo(Node button, Button target, char one, char two) {
		return (button.getId().toString().charAt(2) == one)
		&& (button.getId().toString().charAt(3) == two)
		&& (button.getId().toString().charAt(1) == target.getId().toString().charAt(1));
	}
	
	public boolean highlightLengthFourVerticalTargetThreeSpecialCaseTen(Node button, Button target, char one, char two, char three) {
		return (button.getId().toString().charAt(2) == one)
				&& (button.getId().toString().charAt(1) == target.getId().toString()
				.charAt(1))
		|| (button.getId().toString().charAt(2) == two) && (button.getId()
				.toString().charAt(1) == target.getId().toString().charAt(1))
		|| (button.getId().toString().charAt(2) == three) && (button.getId()
				.toString().charAt(1) == target.getId().toString().charAt(1));
	}
	
	public boolean highlightLengthFourVerticalTargetFour(Node button, Button target, char one, char two, char three) {
		return (button.getId().toString().charAt(2) == one)
				&& (button.getId().toString().charAt(1) == target.getId().toString().charAt(1))
				|| (button.getId().toString().charAt(2) == two) && (button.getId().toString()
						.charAt(1) == target.getId().toString().charAt(1))
				|| (button.getId().toString().charAt(2) == three) && (button.getId().toString()
						.charAt(1) == target.getId().toString().charAt(1));
	}
	
	public void addButtonToBattleship(Node button, Armada clientArmada) {
		StringBuilder sb = new StringBuilder();
		sb.append(button.getId().toString().charAt(1));
		sb.append(button.getId().toString().charAt(2));
		String buttonToAdd = sb.toString();
		clientArmada.addToBattleship(buttonToAdd);
		button.setDisable(true);
		sb.setLength(0);
	}
	public void addButtonToBattleshipFour(Node button, Armada clientArmada) {
		StringBuilder sb = new StringBuilder();
		sb.append(button.getId().toString().charAt(1));
		sb.append(button.getId().toString().charAt(2));
		sb.append(button.getId().toString().charAt(3));
		String buttonToAdd = sb.toString();
		clientArmada.addToBattleship(buttonToAdd);
		button.setDisable(true);
		sb.setLength(0);
	}
	
	public void addTargetToBattleship(Button target, Armada clientArmada) {
		StringBuilder s = new StringBuilder();
		s.append(target.getId().toString().charAt(1));
		s.append(target.getId().toString().charAt(2));
		target.setDisable(true);
		clientArmada.addToBattleship(s.toString());
	}
	
	public void addTargetToBattleshipFour(Button target, Armada clientArmada) {
		StringBuilder s = new StringBuilder();
		s.append(target.getId().toString().charAt(1));
		s.append(target.getId().toString().charAt(2));
		s.append(target.getId().toString().charAt(3));
		target.setDisable(true);
		clientArmada.addToBattleship(s.toString());
	}
}
