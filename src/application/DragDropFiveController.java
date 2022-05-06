package application;

import javafx.scene.Node;
import javafx.scene.control.Button;

public class DragDropFiveController {

	//Code for highlighting vertical and horizontal length = five
		public boolean highlightLengthFiveHorizontalTargetThree(Node button, Button target, char one, char two, char three, char four) {
			return (button.getId().toString().charAt(1) == one)
			&& (button.getId().toString().charAt(2) == target.getId().toString().charAt(2))
			|| (button.getId().toString().charAt(1) == two) && (button.getId().toString()
					.charAt(2) == target.getId().toString().charAt(2))
			|| (button.getId().toString().charAt(1) == three) && (button.getId().toString()
					.charAt(2) == target.getId().toString().charAt(2))
			|| (button.getId().toString().charAt(1) == four) && (button.getId().toString()
					.charAt(2) == target.getId().toString().charAt(2));
		}
		
		public boolean highlightLengthFiveHorizontalTargetFour(Node button, Button target, char one, char two, char three, char four) {
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
							.charAt(3))
			|| (button.getId().toString().charAt(1) == four) && (button.getId().toString()
					.charAt(2) == target.getId().toString().charAt(2)
					&& button.getId().toString().charAt(3) == target.getId().toString()
							.charAt(3));
		}
		
		public boolean hightlightLengthFiveVerticalTargetThree(Node button, Button target, char one, char two, char three, char four) {
			return (button.getId().toString().charAt(2) == one)
					&& (button.getId().toString().charAt(1) == target.getId().toString().charAt(1))
					|| (button.getId().toString().charAt(2) == two) && (button.getId().toString()
							.charAt(1) == target.getId().toString().charAt(1))
					|| (button.getId().toString().charAt(2) == three) && (button.getId().toString()
							.charAt(1) == target.getId().toString().charAt(1))
					|| (button.getId().toString().charAt(2) == four) && (button.getId().toString()
							.charAt(1) == target.getId().toString().charAt(1));
		}

		public boolean highlightLengthFiveVerticalTargetThreeSpecialCaseFour(Node button, Button target, char one, char two) {
			return (button.getId().toString().charAt(2) == one
					&& button.getId().toString().charAt(3) == two)
					&& (button.getId().toString().charAt(1) == target.getId().toString()
							.charAt(1));
		}
		
		public boolean highlightLengthFiveVerticalTargetThreeSpecialCaseThree(Node button, Button target, char one, char two, char three) {
			return (button.getId().toString().charAt(2) == one)
					&& (button.getId().toString().charAt(1) == target.getId().toString().charAt(1))
					|| (button.getId().toString().charAt(2) == two) && (button.getId().toString()
							.charAt(1) == target.getId().toString().charAt(1))
					|| (button.getId().toString().charAt(2) == three) && (button.getId().toString()
							.charAt(1) == target.getId().toString().charAt(1));
		}
		
		public boolean highlightLengthFiveVerticalTargetThreeSpecialCaseOne(Node button, Button target, char one, char two, char three, char four) {
			return (button.getId().toString().charAt(2) == one)
					&& (button.getId().toString().charAt(1) == target.getId().toString()
					.charAt(1))
			|| (button.getId().toString().charAt(2) == two) && (button.getId()
					.toString().charAt(1) == target.getId().toString().charAt(1))
			|| (button.getId().toString().charAt(2) == three) && (button.getId()
					.toString().charAt(1) == target.getId().toString().charAt(1))
			|| (button.getId().toString().charAt(2) == four) && (button.getId()
					.toString().charAt(1) == target.getId().toString().charAt(1));
		}

		public boolean highlightLengthFiveVerticalTargetFour(Node button, Button target, char one, char two, char three, char four) {
			return (button.getId().toString().charAt(2) == one)
			&& (button.getId().toString().charAt(1) == target.getId().toString()
					.charAt(1))
			|| (button.getId().toString().charAt(2) == two) && (button.getId()
					.toString().charAt(1) == target.getId().toString().charAt(1))
			|| (button.getId().toString().charAt(2) == three) && (button.getId()
					.toString().charAt(1) == target.getId().toString().charAt(1))
			|| (button.getId().toString().charAt(2) == four) && (button.getId()
					.toString().charAt(1) == target.getId().toString().charAt(1));
		}
		
		
		//Code for unhighlighting vertical and horizontal length = five
		public boolean unhighlightLengthFiveHorizontalTargetThree(Node button, Button target, char one, char two, char three, char four) {
			return (button.getId().toString().charAt(1) == one)
			&& (button.getId().toString().charAt(2) == target.getId().toString().charAt(2))
			|| (button.getId().toString().charAt(1) == two) && (button.getId().toString()
					.charAt(2) == target.getId().toString().charAt(2))
			|| (button.getId().toString().charAt(1) == three) && (button.getId().toString()
					.charAt(2) == target.getId().toString().charAt(2))
			|| (button.getId().toString().charAt(1) == four) && (button.getId().toString()
					.charAt(2) == target.getId().toString().charAt(2));
		}
		
		public boolean unhighlightLengthFiveHorizontalTargetFourSpecialCaseA(Node button, Button target, char one, char two, char three, char four) {
			return (button.getId().toString().charAt(1) == one)
			&& (button.getId().toString().charAt(2) == target.getId().toString().charAt(2))
			|| (button.getId().toString().charAt(1) == two) && (button.getId().toString()
					.charAt(2) == target.getId().toString().charAt(2))
			|| (button.getId().toString().charAt(1) == three) && (button.getId().toString()
					.charAt(2) == target.getId().toString().charAt(2))
			|| (button.getId().toString().charAt(1) == four) && (button.getId().toString()
					.charAt(2) == target.getId().toString().charAt(2));
		}
		
		public boolean unhighlightLengthFiveHorizontalTargetFour(Node button, Button target, char one, char two, char three, char four) {
			return (button.getId().toString().charAt(1) == one)
			&& (button.getId().toString().charAt(2) == target.getId().toString().charAt(2)
					&& button.getId().toString().charAt(3) == target.getId().toString()
							.charAt(3))
			|| (button.getId().toString().charAt(1) == two)
					&& (button.getId().toString().charAt(2) == target.getId().toString()
							.charAt(2))
					&& button.getId().toString().charAt(3) == target.getId().toString()
							.charAt(3)
			|| (button.getId().toString().charAt(1) == three)
					&& (button.getId().toString().charAt(2) == target.getId().toString()
							.charAt(2))
					&& button.getId().toString().charAt(3) == target.getId().toString()
							.charAt(3)
			|| (button.getId().toString().charAt(1) == four)
					&& (button.getId().toString().charAt(2) == target.getId().toString()
							.charAt(2))
					&& button.getId().toString().charAt(3) == target.getId().toString()
							.charAt(3);
		}
		
		public boolean unhighlightLengthFiveVerticalTargetFour(Node button, Button target, char one, char two, char three, char four) {
			return (button.getId().toString().charAt(2) == one)
					&& (button.getId().toString().charAt(1) == target.getId().toString().charAt(1))
					|| (button.getId().toString().charAt(2) == two) && (button.getId().toString()
					.charAt(1) == target.getId().toString().charAt(1))
					|| (button.getId().toString().charAt(2) == three) && (button.getId().toString()
					.charAt(1) == target.getId().toString().charAt(1))
					|| (button.getId().toString().charAt(2) == four) && (button.getId().toString()
					.charAt(1) == target.getId().toString().charAt(1));
		}
		
		public boolean unhighlightLengthFiveVerticalTargetThree(Node button, Button target, char one, char two, char three, char four){
			return ((button.getId().toString().charAt(2) == one)
					&& (button.getId().toString().charAt(1) == target.getId().toString().charAt(1))
					|| (button.getId().toString().charAt(2) == two) && (button.getId().toString()
							.charAt(1) == target.getId().toString().charAt(1))
					|| (button.getId().toString().charAt(2) == three) && (button.getId().toString()
							.charAt(1) == target.getId().toString().charAt(1))
					|| (button.getId().toString().charAt(2) == four) && (button.getId().toString()
							.charAt(1) == target.getId().toString().charAt(1)));
		}
		
		public boolean unhighlightLengthFiveVerticalSpecialCaseNonTen(Node button, Button target, char one, char two, char three) {
			return (button.getId().toString().charAt(2) == one)
					&& (button.getId().toString().charAt(1) == target.getId().toString().charAt(1))
					|| (button.getId().toString().charAt(2) == two) && (button.getId().toString()
					.charAt(1) == target.getId().toString().charAt(1))
					|| (button.getId().toString().charAt(2) == three) && (button.getId().toString()
					.charAt(1) == target.getId().toString().charAt(1));
		}
		
		public boolean unhighlightLengthFiveVerticalSpecialCaseTen(Node button, Button target, char one, char two) {
			return(button.getId().toString().charAt(2) == one)
					&& (button.getId().toString().charAt(3) == two)
					&& (button.getId().toString().charAt(1) == target.getId().toString().charAt(1));
		}
		
		public void addButtonsToCarrier(Node button, Armada clientArmada) {
			StringBuilder sb = new StringBuilder();
			sb.append(button.getId().toString().charAt(1));
			sb.append(button.getId().toString().charAt(2));
			String buttonToAdd = sb.toString();
			clientArmada.addToCarrier(buttonToAdd);
			button.setDisable(true);
			sb.setLength(0);
		}
		
		public void addButtonsToCarrierFour(Node button, Armada clientArmada) {
			StringBuilder sb = new StringBuilder();
			sb.append(button.getId().toString().charAt(1));
			sb.append(button.getId().toString().charAt(2));
			sb.append(button.getId().toString().charAt(3));
			String buttonToAdd = sb.toString();
			clientArmada.addToCarrier(buttonToAdd);
			button.setDisable(true);
			sb.setLength(0);
		}
		
		public void addTargetToCarrier(Button target, Armada clientArmada) {
			StringBuilder s = new StringBuilder();
			s.append(target.getId().toString().charAt(1));
			s.append(target.getId().toString().charAt(2));
			target.setDisable(true);
			clientArmada.addToCarrier(s.toString());
		}
		
		public void addTargetToCarrierFour(Button target, Armada clientArmada) {
			StringBuilder s = new StringBuilder();
			s.append(target.getId().toString().charAt(1));
			s.append(target.getId().toString().charAt(2));
			s.append(target.getId().toString().charAt(3));
			target.setDisable(true);
			clientArmada.addToCarrier(s.toString());
		}
}
