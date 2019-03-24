package nl.gingerbeard.automation;

import java.util.List;
import java.util.Optional;

import nl.gingerbeard.automation.devices.IDevice;
import nl.gingerbeard.automation.state.NextState;

public abstract class AutoControl {

	private Optional<AutoControlListener> listener = Optional.empty();
	private final String owner;

	protected AutoControl() {
		owner = determineOwner();
	}

	private String determineOwner() {
		// 0 Java.lang.Thread
		// 1 This method
		// 2 Constructor of AutoControl
		// 3 Derived AutoControl class
		// 4 Derived AutoControl class constructor
		// 5 Creator of the derived AutoControl
		return Thread.currentThread().getStackTrace()[5].getClassName().replaceAll(".*\\.", "");
	}

	public final void setListener(final AutoControlListener listener) {
		this.listener = Optional.of(listener);
	}

	protected final void updateActuators(final List<NextState<?>> updates) {
		listener.ifPresent((listener) -> listener.outputChanged(owner, updates));
	}

	/**
	 * Return all devices that are contained within the autoControl.
	 *
	 * @return
	 */
	protected abstract List<IDevice<?>> getDevices();

	/**
	 * Returns the owner (creator) of the AutoControl instance
	 *
	 * @return
	 */
	public String getOwner() {
		return owner;
	}
}