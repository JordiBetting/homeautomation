package nl.gingerbeard.automation.autocontrol;

import java.util.List;
import java.util.Optional;

import nl.gingerbeard.automation.state.NextState;
import nl.gingerbeard.automation.state.OnOffState;

public abstract class AutoControl {

	private Optional<AutoControlListener> listener = Optional.empty();

	public final void setListener(final AutoControlListener listener) {
		this.listener = Optional.of(listener);
	}

	protected final void updateActuators(final List<NextState<OnOffState>> updates) {
		listener.ifPresent((listener) -> listener.outputChanged(updates));
	}
}
