package nl.gingerbeard.automation;

import java.util.List;
import java.util.Optional;

import nl.gingerbeard.automation.devices.IDevice;
import nl.gingerbeard.automation.state.NextState;

public abstract class AutoControl {

	private Optional<AutoControlListener> listener = Optional.empty();

	public final void setListener(final AutoControlListener listener) {
		this.listener = Optional.of(listener);
	}

	protected final void updateActuators(final List<NextState<?>> updates) {
		listener.ifPresent((listener) -> listener.outputChanged(updates));
	}

	/**
	 * Return all devices that are contained within the autoControl.
	 *
	 * @return
	 */
	protected abstract List<IDevice<?>> getDevices();
}
