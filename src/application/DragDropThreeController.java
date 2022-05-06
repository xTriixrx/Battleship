package application;

import javafx.scene.Node;
import javafx.scene.control.Button;

public class DragDropThreeController {
	public boolean unhighlightLengthThreeHorizontalTargetThree(Node button, Button target, char one, char two) {
		return (button.getId().toString().charAt(1) == one)
				&& (button.getId().toString().charAt(2) == target.getId().toString().charAt(2))
				|| (button.getId().toString().charAt(1) == two) && (button.getId().toString()
						.charAt(2) == target.getId().toString().charAt(2));
	}
	
	public boolean unhighlightLengthThreeHorizontalTargetFour(Node button, Button target, char one, char two) {
		return (button.getId().toString().charAt(1) == one)
				&& (button.getId().toString().charAt(2) == target.getId().toString().charAt(2))
				&& button.getId().toString().charAt(3) == target.getId().toString().charAt(3)
				|| (button.getId().toString().charAt(1) == two)
						&& (button.getId().toString().charAt(2) == target.getId().toString()
								.charAt(2))
						&& button.getId().toString().charAt(3) == target.getId().toString()
								.charAt(3);
	}
	
	public boolean unhighlightLengthThreeVerticalTargetThree(Node button, Button target, char one, char two) {
		return (button.getId().toString().charAt(2) == one)
				&& (button.getId().toString().charAt(1) == target.getId().toString().charAt(1))
				|| (button.getId().toString().charAt(2) == two) && (button.getId().toString()
						.charAt(1) == target.getId().toString().charAt(1));
	}
	
	public boolean unhighlightLengthThreeVerticalTargetThreeSpecialCaseNine(Node button, Button target, char one) {
		return (button.getId().toString().charAt(2) == one)
		&& (button.getId().toString().charAt(1) == target.getId().toString().charAt(1));
	}
	
	public boolean unhighlightLengthThreeVerticalTargetThreeSpecialCaseTen(Node button, Button target, char one, char two) {
		return (button.getId().toString().charAt(2) == one)
				&& (button.getId().toString().charAt(3) == two)
				&& (button.getId().toString().charAt(1) == target.getId().toString().charAt(1));
	}
	
	public boolean unhighlightLengthThreeVerticalTargetFour(Node button, Button target, char one, char two) {
		return (button.getId().toString().charAt(2) == one)
				&& (button.getId().toString().charAt(1) == target.getId().toString().charAt(1))
				|| (button.getId().toString().charAt(2) == two) && (button.getId().toString()
						.charAt(1) == target.getId().toString().charAt(1));
	}
	public boolean highlightLengthThreeHorizontalTargetThree(Node button, Button target, char one, char two) {
		return (button.getId().toString().charAt(1) == one)
		&& (button.getId().toString().charAt(2) == target.getId().toString().charAt(2))
		|| (button.getId().toString().charAt(1) == two) && (button.getId().toString()
				.charAt(2) == target.getId().toString().charAt(2));
	}
	public boolean highlightLengthThreeHorizontalTargetFour(Node button, Button target, char one, char two) {
		return (button.getId().toString().charAt(1) == one)
		&& (button.getId().toString().charAt(2) == target.getId().toString().charAt(2)
				&& button.getId().toString().charAt(3) == target.getId().toString()
						.charAt(3))
		|| (button.getId().toString().charAt(1) == two) && (button.getId().toString()
				.charAt(2) == target.getId().toString().charAt(2)
				&& button.getId().toString().charAt(3) == target.getId().toString()
						.charAt(3));
	}
	public boolean highlightLengthThreeVerticalTargetThreeSpecialCaseOneA(Node button, Button target, char one, char two) {
		return (button.getId().toString().charAt(2) == one)
		&& (button.getId().toString().charAt(1) == target.getId().toString()
				.charAt(1))
		|| (button.getId().toString().charAt(2) == two) && (button.getId()
				.toString().charAt(1) == target.getId().toString().charAt(1));
	}
	public boolean highlightLengthThreeVerticalTargetThreeSpecialCaseOneB(Node button, Button target, char one, char two) {
		return (button.getId().toString().charAt(2) == one)
				&& (button.getId().toString().charAt(1) == target.getId().toString().charAt(1))
				|| (button.getId().toString().charAt(2) == two) && (button.getId().toString()
						.charAt(1) == target.getId().toString().charAt(1));
	}
	public boolean highlightLengthThreeVerticalTargetThree(Node button, Button target, char one, char two) {
		return (button.getId().toString().charAt(2) == one)
		&& (button.getId().toString().charAt(1) == target.getId().toString().charAt(1))
		|| (button.getId().toString().charAt(2) == two) && (button.getId().toString()
				.charAt(1) == target.getId().toString().charAt(1));
	}
	public boolean highlightLengthThreeVerticalTargetThreeSpecialCaseNine(Node button, Button target, char one) {
		return (button.getId().toString().charAt(2) == one)
				&& (button.getId().toString().charAt(1) == target.getId().toString().charAt(1));
	}
	public boolean highlightLengthThreeVerticalTargetThreeSpecialCaseTen(Node button, Button target, char one, char two) {
		return (button.getId().toString().charAt(2) == one)
				&& (button.getId().toString().charAt(3) == two)
				&& (button.getId().toString().charAt(1) == target.getId().toString().charAt(1));
	}
	public boolean highlightLengthThreeVerticalTargetFour(Node button, Button target, char one, char two) {
		return (button.getId().toString().charAt(2) == one)
				&& (button.getId().toString().charAt(1) == target.getId().toString().charAt(1))
				|| (button.getId().toString().charAt(2) == two) && (button.getId().toString()
						.charAt(1) == target.getId().toString().charAt(1));
	}
	
	public void addButtonsToSubmarine(Node button, Armada clientArmada) {
		StringBuilder sb = new StringBuilder();
		sb.append(button.getId().toString().charAt(1));
		sb.append(button.getId().toString().charAt(2));
		String buttonToAdd = sb.toString();
		clientArmada.addToSubmarine(buttonToAdd);
		button.setDisable(true);
	}
	
	public void addButtonsToSubmarineFour(Node button, Armada clientArmada) {
		StringBuilder sb = new StringBuilder();
		sb.append(button.getId().toString().charAt(1));
		sb.append(button.getId().toString().charAt(2));
		sb.append(button.getId().toString().charAt(3));
		String buttonToAdd = sb.toString();
		clientArmada.addToSubmarine(buttonToAdd);
		button.setDisable(true);
	}
	
	public void addTargetToSubmarine(Button target, Armada clientArmada) {
		StringBuilder s = new StringBuilder();
		s.append(target.getId().toString().charAt(1));
		s.append(target.getId().toString().charAt(2));
		target.setDisable(true);
		clientArmada.addToSubmarine(s.toString());
	}
	
	public void addTargetToSubmarineFour(Button target, Armada clientArmada) {
		StringBuilder s = new StringBuilder();
		s.append(target.getId().toString().charAt(1));
		s.append(target.getId().toString().charAt(2));
		s.append(target.getId().toString().charAt(3));
		target.setDisable(true);
		clientArmada.addToSubmarine(s.toString());
	}
	
	public void addButtonsToCruiser(Node button, Armada clientArmada) {
		StringBuilder sb = new StringBuilder();
		sb.append(button.getId().toString().charAt(1));
		sb.append(button.getId().toString().charAt(2));
		String buttonToAdd = sb.toString();
		clientArmada.addToCruiser(buttonToAdd);
		button.setDisable(true);
	}
	
	public void addButtonsToCruiserFour(Node button, Armada clientArmada) {
		StringBuilder sb = new StringBuilder();
		sb.append(button.getId().toString().charAt(1));
		sb.append(button.getId().toString().charAt(2));
		sb.append(button.getId().toString().charAt(3));
		String buttonToAdd = sb.toString();
		clientArmada.addToCruiser(buttonToAdd);
		button.setDisable(true);
	}
	
	public void addTargetToCruiser(Button target, Armada clientArmada) {
		StringBuilder s = new StringBuilder();
		s.append(target.getId().toString().charAt(1));
		s.append(target.getId().toString().charAt(2));
		target.setDisable(true);
		clientArmada.addToCruiser(s.toString());
	}
	
	public void addTargetToCruiserFour(Button target, Armada clientArmada) {
		StringBuilder s = new StringBuilder();
		s.append(target.getId().toString().charAt(1));
		s.append(target.getId().toString().charAt(2));
		s.append(target.getId().toString().charAt(3));
		target.setDisable(true);
		clientArmada.addToCruiser(s.toString());
	}
}
