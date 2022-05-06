package application;

import javafx.scene.Node;
import javafx.scene.control.Button;

public class DragDropTwoController {
	public boolean highlightLengthTwoHorizontalTargetThree(Node button, Button target, char one) {
		return (button.getId().toString().charAt(1) == one)
		&& (button.getId().toString().charAt(2) == target.getId().toString().charAt(2));
	}

	
	public boolean highlightLengthTwoHorizontalTargetFour(Node button, Button target, char one) {
		return (button.getId().toString().charAt(1) == one) && (button.getId().toString()
				.charAt(2) == target.getId().toString().charAt(2)
				&& button.getId().toString().charAt(3) == target.getId().toString().charAt(3));
	}
	
	public boolean highlightLengthTwoVertical(Node button, Button target, char one) {
		return (button.getId().toString().charAt(2) == one) && (button.getId().toString()
				.charAt(1) == target.getId().toString().charAt(1));
	}
	
	public boolean unhighlightLengthTwoHorizontalTargetThree(Node button, Button target, char one) {
		return (button.getId().toString().charAt(1) == one)
		&& (button.getId().toString().charAt(2) == target.getId().toString().charAt(2));
	}

	
	public boolean unhighlightLengthTwoHorizontalTargetFour(Node button, Button target, char one) {
		return (button.getId().toString().charAt(1) == one) && (button.getId().toString()
				.charAt(2) == target.getId().toString().charAt(2)
				&& button.getId().toString().charAt(3) == target.getId().toString().charAt(3));
	}
	
	public boolean unhighlightLengthTwoVertical(Node button, Button target, char one) {
		return (button.getId().toString().charAt(2) == one) && (button.getId().toString()
				.charAt(1) == target.getId().toString().charAt(1));
	}
	
	public boolean unhighlightLengthTwoHorizontalTargetFourSpecialCaseI(Node button, Button target, char one, char two){
		return (button.getId().toString().charAt(1) == one)
		&& (button.getId().toString().charAt(2) == target.getId().toString().charAt(2)
				&& button.getId().toString().charAt(3) == target.getId().toString()
						.charAt(3))
		|| (button.getId().toString().charAt(1) == two)
				&& (button.getId().toString().charAt(2) == target.getId().toString()
						.charAt(2))
				&& button.getId().toString().charAt(3) == target.getId().toString()
						.charAt(3);
	}
	
	public boolean unhighlightLengthTwoHorizontalTargetFourSpecialCaseJ(Node button, Button target, char one) {
		return (button.getId().toString().charAt(1) == one)
		&& (button.getId().toString().charAt(2) == target.getId().toString().charAt(2))
		&& (button.getId().toString().charAt(3) == target.getId().toString().charAt(3));
	}
	
	public void addTargetToArmadaFour(Button target, Armada clientArmada) {
		StringBuilder s = new StringBuilder();
		s.append(target.getId().toString().charAt(1));
		s.append(target.getId().toString().charAt(2));
		s.append(target.getId().toString().charAt(3));
		target.setDisable(true);
		clientArmada.addToDestroyer(s.toString());
	}
	
	public void addTargetToArmada(Button target, Armada clientArmada) {
		StringBuilder s = new StringBuilder();
		s.append(target.getId().toString().charAt(1));
		s.append(target.getId().toString().charAt(2));
		target.setDisable(true);
		clientArmada.addToDestroyer(s.toString());
	}
	
	public void addButtonsToArmada(Node button, Armada clientArmada ) {
		StringBuilder sb = new StringBuilder();
		sb.append(button.getId().toString().charAt(1));
		sb.append(button.getId().toString().charAt(2));
		
		String buttonToAdd = sb.toString();
		clientArmada.addToDestroyer(buttonToAdd);
		button.setDisable(true);
		sb.setLength(0);
	}
	
	public void addButtonsToArmadaFour(Node button, Armada clientArmada ) {
		StringBuilder sb = new StringBuilder();
		sb.append(button.getId().toString().charAt(1));
		sb.append(button.getId().toString().charAt(2));
		sb.append(button.getId().toString().charAt(3));

		String buttonToAdd = sb.toString();
		clientArmada.addToDestroyer(buttonToAdd);
		button.setDisable(true);
		sb.setLength(0);
	}
}
