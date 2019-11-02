package nl.gingerbeard.automation.autocontrol;

import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

import com.google.common.collect.Lists;

import nl.gingerbeard.automation.devices.IDevice;
import nl.gingerbeard.automation.devices.Thermostat;
import nl.gingerbeard.automation.event.annotations.Subscribe;
import nl.gingerbeard.automation.state.AlarmState;
import nl.gingerbeard.automation.state.NextState;
import nl.gingerbeard.automation.state.State;
import nl.gingerbeard.automation.state.Temperature;
import nl.gingerbeard.automation.state.ThermostatState;
import nl.gingerbeard.automation.state.TimeOfDay;
import nl.gingerbeard.automation.state.ThermostatState.ThermostatMode;

public class HeatingAutoControl extends AutoControl {

	static final double DEFAULT_TEMP_C_NIGHT = 20;
	static final double DEFAULT_TEMP_C_DAY = 18;
	static final double DEFAULT_TEMP_C_OFF = 15;

	private HeatingState currentState = new StateHeatingOff(); // how would this work? TODO: Give this some thoughts
	private final List<Thermostat> thermostats = Lists.newArrayList();
	private State frameworkState;

	private Temperature offTemperature = Temperature.celcius(DEFAULT_TEMP_C_OFF);
	private Temperature daytimeTemperature = Temperature.celcius(DEFAULT_TEMP_C_DAY);
	private Temperature nighttimeTemperature = Temperature.celcius(DEFAULT_TEMP_C_NIGHT);
	private long delayOnMillis = 0;

	abstract class HeatingState {
		public Optional<HeatingState> alarmChanged() {
			return Optional.empty();
		}

		public Optional<HeatingState> timeOfDayChanged() {
			return Optional.empty();
		}

		public List<NextState<?>> stateEntryResult() {
			return Lists.newArrayList();
		}

		public Optional<HeatingState> stateEntryNextState() {
			return Optional.empty();
		}
	}

	class StateHeatingOff extends HeatingState {

		@Override
		public List<NextState<?>> stateEntryResult() {
			return createNextState(offTemperature);
		}

		@Override
		public Optional<HeatingState> alarmChanged() {
			if (frameworkState.getAlarmState().meets(AlarmState.DISARMED)) {
				return Optional.of(new StateHeatingOnDelay());
			} else {
				return Optional.empty();
			}
		}

	}

	class StateHeatingOnDelay extends HeatingState {
		private final Timer timer = new Timer();

		private class TimerTick extends TimerTask {
			@Override
			public void run() {
				synchronized (StateHeatingOnDelay.this) {
					updateActuators(changeState(determineNextState()));
				}
			}
		}

		public StateHeatingOnDelay() {
			if (delayOnMillis > 0) {
				timer.schedule(new TimerTick(), delayOnMillis);
			}
		}

		@Override
		public List<NextState<?>> stateEntryResult() {
			if (delayOnMillis > 0) {
				return createNextState(offTemperature);
			}
			return super.stateEntryResult();
		}
		
		@Override
		public synchronized Optional<HeatingState> alarmChanged() {
			if (!frameworkState.getAlarmState().meets(AlarmState.DISARMED)) {
				timer.cancel();
				return Optional.of(new StateHeatingOff());
			}
			return super.alarmChanged();
		}

		@Override
		public Optional<HeatingState> stateEntryNextState() {
			if (delayOnMillis == 0) {
				return determineNextState();
			}
			return super.stateEntryNextState();
		}

		private Optional<HeatingState> determineNextState() {
			if (frameworkState.getTimeOfDay() == TimeOfDay.DAYTIME) {
				return Optional.of(new StateHeatingOnDaytime());
			} else {
				return Optional.of(new StateHeatingOnNighttime());
			}
		}
	}

	class StateHeatingOnDaytime extends HeatingState {
		@Override
		public List<NextState<?>> stateEntryResult() {
			return createNextState(daytimeTemperature);
		}
		@Override
		public Optional<HeatingState> timeOfDayChanged() {
			return Optional.of(new StateHeatingOnNighttime());
		}
		@Override
		public Optional<HeatingState> alarmChanged() {
			if (!frameworkState.getAlarmState().meets(AlarmState.DISARMED)) {
				return Optional.of(new StateHeatingOff());
			}
			return super.alarmChanged();
		}
	}

	class StateHeatingOnNighttime extends HeatingState {
		@Override
		public List<NextState<?>> stateEntryResult() {
			return createNextState(nighttimeTemperature);
		}
		@Override
		public Optional<HeatingState> timeOfDayChanged() {
			return Optional.of(new StateHeatingOnDaytime());
		}
		@Override
		public Optional<HeatingState> alarmChanged() { 
			if (!frameworkState.getAlarmState().meets(AlarmState.DISARMED)) {
				return Optional.of(new StateHeatingOff());
			}
			return super.alarmChanged();
		}
	}

	public HeatingAutoControl(State state) {
		this.frameworkState = state;
	}

	@Subscribe
	public List<NextState<?>> alarmChanged(AlarmState _void) {
		Optional<HeatingState> nextState = currentState.alarmChanged();
		return changeState(nextState);
	}

	private List<NextState<?>> changeState(Optional<HeatingState> nextState) {
		List<NextState<?>> result = Lists.newArrayList();

		if (nextState.isPresent()) {
			currentState = nextState.get();
			result.addAll(currentState.stateEntryResult());

			Optional<HeatingState> onEntryNextState = currentState.stateEntryNextState();
			result.addAll(changeState(onEntryNextState));
		}

		return result;
	}

	@Subscribe
	public List<NextState<?>> timeOfDayChanged(TimeOfDay _void) {
		Optional<HeatingState> nextState = currentState.timeOfDayChanged();
		return changeState(nextState);
	}

	public final void addThermostat(Thermostat thermostat) {
		thermostats.add(thermostat);
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
		this.offTemperature = offTemperature;
	}

	public void setDaytimeTemperature(Temperature daytimeTemperature) {
		this.daytimeTemperature = daytimeTemperature;
	}

	public void setNighttimeTemperature(Temperature nighttimeTemperature) {
		this.nighttimeTemperature = nighttimeTemperature;
	}

	public void setDelayOnMinutes(int delayOnMinutes) {
		this.delayOnMillis = delayOnMinutes * 60 * 1000;
	}

	void setDelayOnMillis(long delayOnMillis) {
		this.delayOnMillis = delayOnMillis;
	}

	// test interfaces
	long getDelayOnMillis() {
		return delayOnMillis;
	}

	Class<? extends HeatingState> getState() {
		return currentState.getClass();
	}

}
