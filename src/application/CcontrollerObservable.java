package application;

public interface CcontrollerObservable {
	public void registerObserver(ClientObserver c);
	public void removeObserver();
	public void notifyObserver(String s);
}
