package nl.gingerbeard.automation.autocontrol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.gingerbeard.automation.Room;
import nl.gingerbeard.automation.devices.Switch;
import nl.gingerbeard.automation.event.annotations.Subscribe;
import nl.gingerbeard.automation.state.NextState;
import nl.gingerbeard.automation.state.OnOffState;

public class AutoControl extends Room {

	private final Map<Integer, Switch> sensors = new HashMap<>();
	private final List<Switch> actuators = new ArrayList<>();

	public void addSensor(final Switch sensor) {
		sensors.put(sensor.getIdx(), sensor);
	}

	public void addActuator(final Switch actuator) {
		actuators.add(actuator);
	}

	@Subscribe
	public List<NextState<OnOffState>> switchChanged(final Switch changedSwitch) {
		final OnOffState actuatorState = determineActuatorState();
		final ArrayList<NextState<OnOffState>> output = new ArrayList<>();
		if (sensors.containsKey(changedSwitch.getIdx())) {
			actuators.stream().forEach(//
					(actuator) -> output.add(new NextState<>(actuator, actuatorState))//
			);
		}
		return output;
	}

	private OnOffState determineActuatorState() {
		for (final Switch sensor : sensors.values()) {
			if (sensor.getState() == OnOffState.ON) {
				return OnOffState.ON;
			}
		}
		return OnOffState.OFF;
	}

}
