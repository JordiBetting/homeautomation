package nl.gingerbeard.domotica.rooms;

import nl.gingerbeard.automation.Room;
import nl.gingerbeard.automation.devices.DimmeableLight;
import nl.gingerbeard.automation.devices.DoorSensor;
import nl.gingerbeard.automation.event.annotations.Subscribe;
import nl.gingerbeard.automation.state.Level;
import nl.gingerbeard.automation.state.NextState;

public class Gang extends Room {

	private final DimmeableLight lamp = new DimmeableLight(276);
	private final DoorSensor sensor = new DoorSensor(438);

	public Gang() {
		addDevice(lamp).and(sensor);
	}

	@Subscribe
	public NextState<?> trigger(final DoorSensor changedSensor) {
		final NextState<Level> level = new NextState<>(lamp, new Level(42));
		return level;
	}
}
