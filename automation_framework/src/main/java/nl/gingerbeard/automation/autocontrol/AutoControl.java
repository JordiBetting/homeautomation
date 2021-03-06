package nl.gingerbeard.automation.autocontrol;

import java.util.List;
import java.util.Optional;

import nl.gingerbeard.automation.devices.IDevice;
import nl.gingerbeard.automation.logging.ILogger;
import nl.gingerbeard.automation.state.IState;
import nl.gingerbeard.automation.state.NextState;

public abstract class AutoControl {

	private Optional<AutoControlListener> listener = Optional.empty();
	private final String owner;
	private IState state;
	private ILogger log;

	protected AutoControl() {
		owner = determineOwner();
	}

	private String determineOwner() {
		// 0 Java.lang.Thread
		// 1 This method
		// 2 Constructor of AutoControl
		// 3 Derived AutoControl class
		// 4 Creator of the derived AutoControl
		return Thread.currentThread().getStackTrace()[4].getClassName().replaceAll(".*\\.", "");
	}

	protected final void updateActuators(final List<NextState<?>> updates) {
		listener.ifPresent((listener) -> listener.outputChanged(owner, updates));
	}

	/**
	 * Return all devices that are contained within the autoControl.
	 *
	 * @return
	 */
	public abstract List<IDevice<?>> getDevices();

	/**
	 * Returns the owner (creator) of the AutoControl instance
	 *
	 * @return
	 */
	public final String getOwner() {
		return owner;
	}

	public final IState getState() {
		return state;
	}
	
	protected final ILogger getLogger() { 
		return log;
	}

	public void init(AutoControlListener listener, IState state, ILogger log) {
		this.listener = Optional.ofNullable(listener);
		this.state = state;
		this.log = log;
		onInit();
	}
	
	protected void onInit() {}
}