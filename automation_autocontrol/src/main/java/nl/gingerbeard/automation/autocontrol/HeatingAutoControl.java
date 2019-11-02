package nl.gingerbeard.automation.autocontrol;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;

import nl.gingerbeard.automation.autocontrol.heatingstates.HeatingState;
import nl.gingerbeard.automation.autocontrol.heatingstates.StateHeatingOff;
import nl.gingerbeard.automation.devices.IDevice;
import nl.gingerbeard.automation.devices.OnOffDevice;
import nl.gingerbeard.automation.devices.Switch;
import nl.gingerbeard.automation.devices.Thermostat;
import nl.gingerbeard.automation.event.annotations.Subscribe;
import nl.gingerbeard.automation.state.AlarmState;
import nl.gingerbeard.automation.state.NextState;
import nl.gingerbeard.automation.state.OnOffState;
import nl.gingerbeard.automation.state.State;
import nl.gingerbeard.automation.state.Temperature;
import nl.gingerbeard.automation.state.ThermostatState;
import nl.gingerbeard.automation.state.ThermostatState.ThermostatMode;
import nl.gingerbeard.automation.state.TimeOfDay;

public class HeatingAutoControl extends AutoControl {

	public static final double DEFAULT_TEMP_C_NIGHT = 20;
	public static final double DEFAULT_TEMP_C_DAY = 18;
	public static final double DEFAULT_TEMP_C_OFF = 15;

	private HeatingState currentState;
	private final List<Thermostat> thermostats = Lists.newArrayList();
	private final List<OnOffDevice> pauseDevices = Lists.newArrayList();

	private final HeatingAutoControlContext context;

	public HeatingAutoControl(State state) {
		context = new HeatingAutoControlContext(this);
		context.frameworkState = state;
		currentState = new StateHeatingOff(context);
	}

	public final void addThermostat(Thermostat thermostat) {
		thermostats.add(thermostat);
	}

	public void addPauseDevice(Switch pauseDevice) {
		pauseDevices.add(pauseDevice);
	}

	@Subscribe
	public List<NextState<?>> alarmChanged(AlarmState _void) {
		Optional<HeatingState> nextState = currentState.alarmChanged();
		return changeState(nextState);
	}

	@Subscribe
	public List<NextState<?>> timeOfDayChanged(TimeOfDay _void) {
		Optional<HeatingState> nextState = currentState.timeOfDayChanged();
		return changeState(nextState);
	}
	
	private boolean isAllPauseDevicesOff() {
		for (OnOffDevice pauseDevice : pauseDevices) {
			if (pauseDevice.getState() == OnOffState.ON) {
				return false;
			}
		}
		return true;
	}
	
	@Subscribe
	public List<NextState<?>> deviceUpdated(OnOffDevice _void) {
		Optional<HeatingState> nextState;
		if (isAllPauseDevicesOff()) {
			nextState = currentState.allPauseDevicesOff();
		} else {
			nextState = currentState.pauseDeviceOn();
		}
		return changeState(nextState);
	}

	List<NextState<?>> changeState(Optional<HeatingState> nextState) {
		List<NextState<?>> result = Lists.newArrayList();

		if (nextState.isPresent()) {
			currentState = nextState.get();
			Optional<Temperature> stateEntryResult = currentState.stateEntryResult();
			stateEntryResult.ifPresent((entryResult) -> result.addAll(createNextState(entryResult)));

			Optional<HeatingState> onEntryNextState = currentState.stateEntryNextState();
			result.addAll(changeState(onEntryNextState));
		}

		return result;
	}

	void asyncOutput(List<NextState<?>> result) {
		updateActuators(result);
	}
	
	private List<NextState<?>> createNextState(Temperature temperature) {
		List<NextState<?>> result = Lists.newArrayList();
		for (Thermostat thermostat : thermostats) {
			result.addAll(createNewStateCollection(temperature, thermostat));
		}
		return result;
	}

	private List<NextState<?>> createNewStateCollection(Temperature temperature, Thermostat thermostat) {
		ThermostatState newState = createNewState(temperature);
		List<NextState<?>> thermostatUpdates = thermostat.createNextState(newState);
		return thermostatUpdates;
	}

	private ThermostatState createNewState(Temperature temperature) {
		ThermostatState newState = new ThermostatState();
		newState.setMode(ThermostatMode.SETPOINT);
		newState.setTemperature(temperature);
		return newState;
	}

	@Override
	public List<IDevice<?>> getDevices() {
		List<IDevice<?>> out = Lists.newArrayList();
		thermostats.stream().forEach(t -> out.add(t));
		return out;
	}

	public void setOffTemperature(Temperature offTemperature) {
		context.offTemperature = offTemperature;
	}

	public void setDaytimeTemperature(Temperature daytimeTemperature) {
		context.daytimeTemperature = daytimeTemperature;
	}

	public void setNighttimeTemperature(Temperature nighttimeTemperature) {
		context.nighttimeTemperature = nighttimeTemperature;
	}

	public void setDelayOnMinutes(int delayOnMinutes) {
		context.delayOnMillis = delayOnMinutes * 60 * 1000;
	}

	public void setDelayPauseSeconds(int delayPauseSeconds) {
		context.delayPauseMillis = delayPauseSeconds * 1000;
	}

	// test interfaces
	void setDelayOnMillis(long delayOnMillis) {
		context.delayOnMillis = delayOnMillis;
	}

	HeatingAutoControlContext getContext() {
		return context;
	}

	void setDelayPauseMillis(long delayPauseMillis) {
		context.delayPauseMillis = delayPauseMillis;
	}

	Class<? extends HeatingState> getState() {
		return currentState.getClass();
	}

}
