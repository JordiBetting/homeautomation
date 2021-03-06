package nl.gingerbeard.automation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import nl.gingerbeard.automation.autocontrol.AutoControl;
import nl.gingerbeard.automation.devices.IDevice;
import nl.gingerbeard.automation.state.IState;

public class Room {

	private final List<IDevice<?>> allDevices = new ArrayList<>();
	private final List<AutoControl> allAutoControls = new ArrayList<>();

	private final RoomBuilder builder = new RoomBuilder();

	private Optional<IState> state = Optional.empty();

	protected class RoomBuilder {
		public RoomBuilder and(final IDevice<?> device) {
			allDevices.add(device);
			return this;
		}
	}

	final void init(final IState state) {
		this.state = Optional.of(state);
		onInit();
	}
	
	protected void onInit() {}

	protected final IState getState() {
		return state.orElseThrow(() -> new IllegalStateException("State is not available when room has not been added to the automation framework."));
	}

	protected final RoomBuilder addDevice(final IDevice<?> device) {
		return builder.and(device);
	}

	public final List<IDevice<?>> getDevices() {
		List<IDevice<?>> allOwnedDevices = new ArrayList<>();
		allOwnedDevices.addAll(allDevices);
		allAutoControls.forEach((ac) -> allOwnedDevices.addAll(ac.getDevices()));
		return Collections.unmodifiableList(allOwnedDevices);
	}

	public void addAutoControl(final AutoControl autoControl) {
		allAutoControls.add(autoControl);
	}

	public List<AutoControl> getAutoControls() {
		return Collections.unmodifiableList(allAutoControls);
	}

}
