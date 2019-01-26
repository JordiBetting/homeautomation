package nl.gingerbeard.domotica.rooms;

import nl.gingerbeard.automation.Room;
import nl.gingerbeard.automation.devices.DimmeableLight;
import nl.gingerbeard.automation.devices.DoorSensor;
import nl.gingerbeard.automation.event.annotations.Subscribe;
import nl.gingerbeard.automation.state.Level;
import nl.gingerbeard.automation.state.NextState;
import nl.gingerbeard.automation.state.OpenCloseState;

public class Gang extends Room {

	private final DimmeableLight lamp = new DimmeableLight(276);
	private final DoorSensor sensor = new DoorSensor(438);

	public Gang() {
		addDevice(lamp).and(sensor);
	}

	@Subscribe
	public NextState<?> trigger(final DoorSensor changedSensor) {
		// if (changedSensor == sensor) { TODO: Add test to see that this works
		int level = 0;
		if (changedSensor.getState() == OpenCloseState.OPEN) {
			level = 42;
		}
		return new NextState<>(lamp, new Level(level));
		// }
		// return null;
	}
}
