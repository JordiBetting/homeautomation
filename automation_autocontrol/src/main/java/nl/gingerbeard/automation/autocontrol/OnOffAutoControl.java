package nl.gingerbeard.automation.autocontrol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import nl.gingerbeard.automation.AutoControl;
import nl.gingerbeard.automation.autocontrol.timer.AutoControlTimer;
import nl.gingerbeard.automation.devices.Device;
import nl.gingerbeard.automation.devices.IDevice;
import nl.gingerbeard.automation.devices.StateDevice;
import nl.gingerbeard.automation.event.annotations.Subscribe;
import nl.gingerbeard.automation.state.NextState;

public final class OnOffAutoControl<DeviceType extends Device<StateType>, StateType, OutputType> extends AutoControl {

	private final Map<Integer, DeviceType> sensors = new HashMap<>();
	private final List<StateDevice<OutputType>> actuators = new ArrayList<>();
	private long delayMs = 0;
	private final AutoControlTimer timer = new AutoControlTimer();
	private final StateType inputOn;
	private final OutputType outputOn;
	private final OutputType outputOff;

	public OnOffAutoControl(final StateType inputOn, final OutputType outputOn, final OutputType outputOff) {
		this.inputOn = inputOn;
		this.outputOn = outputOn;
		this.outputOff = outputOff;
	}

	public void addSensor(final DeviceType sensor) {
		sensors.put(sensor.getIdx(), sensor);
	}

	public void addActuator(final StateDevice<OutputType> actuator) {
		actuators.add(actuator);
	}

	@Subscribe
	public void sensorChanged(final DeviceType changedSwitch) {
		if (sensorExists(changedSwitch)) {
			sensorChanged();
		}
	}

	private void sensorChanged() {
		final OutputType actuatorState = determineActuatorState();
		final List<NextState<OutputType>> output = createOutput(actuatorState);
		reportResults(output, actuatorState);
	}

	private void reportResults(final List<NextState<OutputType>> output, final OutputType actuatorState) {
		final long delay = actuatorState == outputOn ? 0L : delayMs;
		timer.executeDelayed(() -> updateActuators(toGeneric(output)), delay);
	}

	private List<NextState<?>> toGeneric(final List<NextState<OutputType>> output) {
		final List<NextState<?>> out = new ArrayList<>();
		out.addAll(output);
		return out;
	}

	private ArrayList<NextState<OutputType>> createOutput(final OutputType actuatorState) {
		final ArrayList<NextState<OutputType>> output = new ArrayList<>();
		actuators.stream().forEach(//
				(actuator) -> output.add(new NextState<>(actuator, actuatorState))//
		);
		return output;
	}

	private boolean sensorExists(final DeviceType changedSwitch) {
		return sensors.containsKey(changedSwitch.getIdx());
	}

	private OutputType determineActuatorState() {
		for (final DeviceType sensor : sensors.values()) {
			if (inputOn.equals(sensor.getState())) {
				return outputOn;
			}
		}
		return outputOff;
	}

	public void setDelayedOff(final long delay, final TimeUnit unit) {
		delayMs = unit.toMillis(delay);
	}

	@Override
	protected List<IDevice<?>> getDevices() {
		final List<IDevice<?>> devices = new ArrayList<>();
		devices.addAll(sensors.values());
		devices.addAll(actuators);
		return devices;
	}

}
