package nl.gingerbeard.automation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import nl.gingerbeard.automation.devices.IDevice;
import nl.gingerbeard.automation.state.State;

public class Room {

	private final List<IDevice<?>> allDevices = new ArrayList<>();

	private final RoomBuilder builder = new RoomBuilder();

	private Optional<State> state = Optional.empty();

	protected class RoomBuilder {
		public RoomBuilder and(final IDevice<?> device) {
			allDevices.add(device);
			return this;
		}
	}

	void setState(final State state) {
		this.state = Optional.of(state);
	}

	protected final State getState() {
		return state.orElseThrow(() -> new IllegalStateException("State is not available when room has not been added to the automation framework."));
	}

	protected final RoomBuilder addDevice(final IDevice<?> device) {
		return builder.and(device);
	}

	public final List<IDevice<?>> getDevices() {
		return Collections.unmodifiableList(allDevices);
	}
}
