package nl.gingerbeard.automation.autocontrol.controls;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import nl.gingerbeard.automation.autocontrol.AutoControl;
import nl.gingerbeard.automation.autocontrol.timer.AutoControlTimer;
import nl.gingerbeard.automation.devices.Switch;
import nl.gingerbeard.automation.event.annotations.Subscribe;
import nl.gingerbeard.automation.state.NextState;
import nl.gingerbeard.automation.state.OnOffState;

public final class OnOffAutoControl extends AutoControl {

	private final Map<Integer, Switch> sensors = new HashMap<>();
	private final List<Switch> actuators = new ArrayList<>();
	private long delayMs = 0;
	private final AutoControlTimer timer = new AutoControlTimer();

	public void addSensor(final Switch sensor) {
		sensors.put(sensor.getIdx(), sensor);
	}

	public void addActuator(final Switch actuator) {
		actuators.add(actuator);
	}

	@Subscribe
	public void switchChanged(final Switch changedSwitch) {
		if (sensorExists(changedSwitch)) {
			sensorChanged();
		}
	}

	private void sensorChanged() {
		final OnOffState actuatorState = determineActuatorState();
		final ArrayList<NextState<OnOffState>> output = createOutput(actuatorState);
		reportResults(output, actuatorState);
	}

	private void reportResults(final ArrayList<NextState<OnOffState>> output, final OnOffState actuatorState) {
		final long delay = actuatorState == OnOffState.ON ? 0L : delayMs;
		timer.executeDelayed(() -> updateActuators(output), delay);
	}

	private ArrayList<NextState<OnOffState>> createOutput(final OnOffState actuatorState) {
		final ArrayList<NextState<OnOffState>> output = new ArrayList<>();
		actuators.stream().forEach(//
				(actuator) -> output.add(new NextState<>(actuator, actuatorState))//
		);
		return output;
	}

	private boolean sensorExists(final Switch changedSwitch) {
		return sensors.containsKey(changedSwitch.getIdx());
	}

	private OnOffState determineActuatorState() {
		for (final Switch sensor : sensors.values()) {
			if (sensor.getState() == OnOffState.ON) {
				return OnOffState.ON;
			}
		}
		return OnOffState.OFF;
	}

	public void setDelayedOff(final long delay, final TimeUnit unit) {
		delayMs = unit.toMillis(delay);
	}

}
