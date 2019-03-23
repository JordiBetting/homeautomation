package nl.gingerbeard.automation.devices;

public interface IDevice<T> {

	T getState();

	void setState(final T newState);

	int getIdx();

}
