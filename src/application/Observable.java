package application;

public interface Observable {
	public void registerObserver(Observer s);
	public void removeObserver();
	public void notifyObserver(String s);
}
