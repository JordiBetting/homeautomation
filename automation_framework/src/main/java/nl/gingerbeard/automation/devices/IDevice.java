package nl.gingerbeard.automation.devices;

public interface IDevice<T> {

	boolean updateState(final String newState);

	T getState();

	void setState(final T newState);

}
