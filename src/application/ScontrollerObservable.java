package application;

public interface ScontrollerObservable {
	public void registerObserver(ServerObserver s);
	public void removeObserver();
	public void notifyObserver(String s);
}
