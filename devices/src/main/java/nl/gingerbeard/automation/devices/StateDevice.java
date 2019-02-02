package nl.gingerbeard.automation.devices;

public abstract class StateDevice<T> implements IDevice<T> {

	private T state;

	@Override
	public final T getState() {
		return state;
	}

	@Override
	public final void setState(final T newState) {
		state = newState;
	}

}
