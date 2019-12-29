package nl.gingerbeard.automation.autocontrol;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;

import nl.gingerbeard.automation.autocontrol.heating.HeatingAutoControlContext;
import nl.gingerbeard.automation.autocontrol.heating.IHeatingAutoControlStateControl;
import nl.gingerbeard.automation.autocontrol.heating.states.HeatingState;
import nl.gingerbeard.automation.autocontrol.heating.states.StateHeatingOff;
import nl.gingerbeard.automation.devices.IDevice;
import nl.gingerbeard.automation.devices.OnOffDevice;
import nl.gingerbeard.automation.devices.OpenCloseDevice;
import nl.gingerbeard.automation.devices.Thermostat;
import nl.gingerbeard.automation.event.annotations.Subscribe;
import nl.gingerbeard.automation.state.AlarmState;
import nl.gingerbeard.automation.state.NextState;
import nl.gingerbeard.automation.state.OnOffState;
import nl.gingerbeard.automation.state.OpenCloseState;
import nl.gingerbeard.automation.state.Temperature;
import nl.gingerbeard.automation.state.ThermostatState;
import nl.gingerbeard.automation.state.ThermostatState.ThermostatMode;
import nl.gingerbeard.automation.state.TimeOfDay;

/**
 * AutoControl to have simple means to control heating thermostats.
 * 
 * Has 3 temperature settings:
 * <ol>
 * <li>OFF temperature, when alarm is set to an armed state
 * <li>DAY temperature, when alarm is set to disarmed and TimeOfDay is DAYTIME
 * <li>NIGHT temperature, when alarm is set to disarmed and TimeOfDay is
 * NIGHTTIME
 * </ol>
 * Turning heating on after disarm can be delayed with setting to avoid short
 * on/off times (e.g. coming home, directly go to bed). PauseDevices can be
 * added. When these OnOffDevices will turn ON (e.g. door open), the heating
 * will be set to OFF temperature. This can also be delayed (e.g. door must be
 * open for at least 2 minutes) to avoid battery drain of thermostats.
 */
public final class HeatingAutoControl extends AutoControl implements IHeatingAutoControlStateControl {

	public static final double DEFAULT_TEMP_C_NIGHT = 20;
	public static final double DEFAULT_TEMP_C_DAY = 18;
	public static final double DEFAULT_TEMP_C_OFF = 15;

	private HeatingState currentState;
	private final List<Thermostat> thermostats = Lists.newArrayList();
	private final List<OnOffDevice> pauseOnOffDevices = Lists.newArrayList();
	private final List<OpenCloseDevice> pauseOpenCloseDevices = Lists.newArrayList();

	private HeatingAutoControlContext context;

	public HeatingAutoControl() {
		context = new HeatingAutoControlContext(this, getOwner());
	}

	public void addThermostat(Thermostat thermostat) {
		thermostats.add(thermostat);
	}

	public void addPauseDevice(OnOffDevice pauseDevice) {
		pauseOnOffDevices.add(pauseDevice);
	}

	public void addPauseDevice(OpenCloseDevice pauseDevice) {
		pauseOpenCloseDevices.add(pauseDevice);
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

	@Subscribe
	public List<NextState<?>> deviceChanged(OnOffDevice _void) {
		return deviceChanged();
	}

	public List<NextState<?>> deviceChanged(OpenCloseDevice _void) {
		return deviceChanged();
	}

	private List<NextState<?>> deviceChanged() {
		boolean changed = updateContextPauseDevices();
		if (changed) {
			Optional<HeatingState> nextState = notifyPauseDeviceChanged();
			return changeState(nextState);
		}
		return Lists.newArrayList();
	}

	private Optional<HeatingState> notifyPauseDeviceChanged() {
		Optional<HeatingState> nextState;
		if (context.isAllPauseDevicesOff()) {
			nextState = currentState.allPauseDevicesOff();
			getLogger().info("HeatingAutoControl for " + getOwner() + " detected that all pause devices are OFF");
		} else {
			nextState = currentState.pauseDeviceOn();
		}
		return nextState;
	}

	private boolean updateContextPauseDevices() {
		boolean oldValue = context.isAllPauseDevicesOff();
		boolean newValue = isAllPauseDevicesOff();
		context.setAllPauseDeviceOff(newValue);
		return oldValue != newValue;
	}

	private boolean isAllPauseDevicesOff() {
		return isAllPauseDevicesOff(pauseOnOffDevices, OnOffState.OFF)
				&& isAllPauseDevicesOff(pauseOpenCloseDevices, OpenCloseState.CLOSE);
	}

	private boolean isAllPauseDevicesOff(List<? extends IDevice<?>> devices, Object state) {
		for (IDevice<?> pauseDevice : devices) {
			if (pauseDevice.getState() != state) {
				getLogger().info("HeatingAutoControl for " + getOwner() + " detected that pause device " + pauseDevice
						+ " now has state " + pauseDevice.getState());
				return false;
			}
		}
		return true;
	}

	private String getStateName(HeatingState state) {
		return state != null ? state.getClass().getSimpleName() : "Uninitialized";
	}

	@Override
	public void changeStateAsync(Optional<HeatingState> newState) {
		List<NextState<?>> result = changeState(newState);
		updateActuators(result);
	}

	private List<NextState<?>> changeState(Optional<HeatingState> nextStateOptional) {
		List<NextState<?>> result = Lists.newArrayList();

		if (nextStateOptional.isPresent()) {
			HeatingState nextState = nextStateOptional.get();
			getLogger().info("HeatingAutoControl for " + getOwner() + " changing state from "
					+ getStateName(currentState) + " to " + getStateName(nextState));
			currentState = nextState;

			Optional<HeatingState> onEntryNextState = currentState.stateEntryNextState();
			
			if(!onEntryNextState.isPresent()) {
				Optional<Temperature> stateEntryResult = currentState.stateEntryResult();
				stateEntryResult.ifPresent((entryResult) -> result.addAll(createNextState(entryResult)));
			}
			result.addAll(changeState(onEntryNextState));
		}

		return result;
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
		pauseOnOffDevices.stream().forEach(t -> out.add(t));
		pauseOpenCloseDevices.stream().forEach(t -> out.add(t));
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
		context.delayOnMillis = 60L * 1000L * delayOnMinutes;
	}

	public void setDelayPauseSeconds(int delayPauseSeconds) {
		context.delayPauseMillis = 1000L * delayPauseSeconds;
	}

	@Override
	protected void onInit() {
		context.configure(getState(), getLogger());
		context.setAllPauseDeviceOff(isAllPauseDevicesOff());
		changeStateAsync(Optional.of(new StateHeatingOff(context)));
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

	Class<? extends HeatingState> getControlState() {
		return currentState.getClass();
	}

}