package nl.gingerbeard.automation.autocontrol;

import java.util.List;
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
import nl.gingerbeard.automation.state.ThermostatState.ThermostatMode;
import nl.gingerbeard.automation.state.TimeOfDay;

public class HeatingAutoControl extends AutoControl {

	static final double DEFAULT_TEMP_C_NIGHT = 20;
	static final double DEFAULT_TEMP_C_DAY = 18;
	static final double DEFAULT_TEMP_C_OFF = 15;

	private static enum HeatingAutoControlState {
		OFF, DELAY_BEFORE_ON, ON_DAYTIME, ON_EVENING
	}

	// internals
	private volatile HeatingAutoControlState state = HeatingAutoControlState.OFF;
	private final Object timerLock = new Object();
	private volatile Timer timer = new Timer();
	private final State frameworkState;

	// user settings
	private final List<Thermostat> thermostats = Lists.newArrayList();
	private Temperature offTemperature = Temperature.celcius(DEFAULT_TEMP_C_OFF);
	private Temperature daytimeTemperature= Temperature.celcius(DEFAULT_TEMP_C_DAY);
	private Temperature nighttimeTemperature= Temperature.celcius(DEFAULT_TEMP_C_NIGHT);
	private int delayOnMillis = 0;

	public HeatingAutoControl(State state) {
		this.frameworkState = state;
	}

	public final void addThermostat(Thermostat thermostat) {
		thermostats.add(thermostat);
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
	
	void setDelayOnMillis(int delayOnMillis) {
		this.delayOnMillis = delayOnMillis;
	}

	@Override
	public List<IDevice<?>> getDevices() {
		List<IDevice<?>> out = Lists.newArrayList();
		thermostats.stream().forEach(t -> out.add(t));
		return out;
	}

	@Subscribe
	public List<NextState<?>> alarmChanged(AlarmState newState) {
		if (state == HeatingAutoControlState.OFF && newState.meets(AlarmState.DISARMED)) {
			if (delayOnMillis == 0) {
				return timeOfDayChanged(null);
			} else {
				state = HeatingAutoControlState.DELAY_BEFORE_ON;
				startTimer();
			}
		} else {
			stopTimer();
			state = HeatingAutoControlState.OFF;
		}
		return execute();

	}

	private void startTimer() {
		timer.schedule(new TimerTask() { // TODO: refactor/cleanup
			@Override
			public void run() {
				// reset timer
				stopTimer();

				// update state
				if (frameworkState.getTimeOfDay() == TimeOfDay.DAYTIME) {
					state = HeatingAutoControlState.ON_DAYTIME;
				} else {
					state = HeatingAutoControlState.ON_EVENING;
				}
				
				// calculate devices
			    HeatingAutoControl.super.updateActuators(execute());
			}
		}, delayOnMillis);
	}

	private void stopTimer() {
		synchronized (timerLock) {
			if (timer != null) {
				timer.cancel();
				timer = null;
			}
		}
	}

	@Subscribe
	public List<NextState<?>> timeOfDayChanged(TimeOfDay timeofday) {
		if (state == HeatingAutoControlState.ON_DAYTIME) {
			state = HeatingAutoControlState.ON_EVENING;
			return execute();
		} else if (state == HeatingAutoControlState.ON_EVENING) {
			state = HeatingAutoControlState.ON_DAYTIME;
			return execute();
		} else if (state == HeatingAutoControlState.OFF) {
			if (frameworkState.getTimeOfDay() == TimeOfDay.DAYTIME) {
				state = HeatingAutoControlState.ON_DAYTIME;
			} else {
				state = HeatingAutoControlState.ON_EVENING;
			}
			return execute();
		}
		return null;
	}

	private List<NextState<?>> execute() {
		List<NextState<?>> result = null;
		switch (state) {
		case OFF:
		case DELAY_BEFORE_ON:
			result = createNextState(offTemperature);
			break;
		case ON_DAYTIME:
			result = createNextState(daytimeTemperature);
			break;
		case ON_EVENING:
			result = createNextState(nighttimeTemperature);
			break;
		}
		return result;
	}

	private List<NextState<?>> createNextState(Temperature temperature) {
		return createNextState(ThermostatMode.SETPOINT, temperature);
	}

	private List<NextState<?>> createNextState(ThermostatMode mode, Temperature temperature) {
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
}
