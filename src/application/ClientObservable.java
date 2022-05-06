package application;

public interface ClientObservable {
	public void registerObserver(CcontrollerObserver c);
	public void removeObserver();
	public void notifyObserver(String s);
}
