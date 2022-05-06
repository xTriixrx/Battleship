package application;

public interface ServerObservable {
	public void registerObserver(ScontrollerObserver c);
	public void removeObserver();
	public void notifyObserver(String s);
}
