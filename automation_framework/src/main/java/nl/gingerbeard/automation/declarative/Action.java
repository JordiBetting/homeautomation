package nl.gingerbeard.automation.declarative;

import nl.gingerbeard.automation.devices.Device;

final class Action<T> {

	private final Device<T> device;
	private final T newState;

	public Action(final Device<T> device, final T newState) {
		this.device = device;
		this.newState = newState;
	}

	public Device<T> getDevice() {
		return device;
	}

	public T getNewState() {
		return newState;
	}

	public void execute() {
		device.setState(newState);
	}

}
