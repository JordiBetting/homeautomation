package nl.gingerbeard.automation.controlloop;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import nl.gingerbeard.automation.devices.StateDevice;
import nl.gingerbeard.automation.devices.Subdevice;
import nl.gingerbeard.automation.domoticz.IDomoticzAlarmChanged;
import nl.gingerbeard.automation.domoticz.IDomoticzDeviceStatusChanged;
import nl.gingerbeard.automation.domoticz.IDomoticzTimeOfDayChanged;
import nl.gingerbeard.automation.domoticz.transmitter.IDomoticzUpdateTransmitter;
import nl.gingerbeard.automation.event.EventResult;
import nl.gingerbeard.automation.event.IEvents;
import nl.gingerbeard.automation.logging.ILogger;
import nl.gingerbeard.automation.state.AlarmState;
import nl.gingerbeard.automation.state.IState;
import nl.gingerbeard.automation.state.NextState;
import nl.gingerbeard.automation.state.Time;
import nl.gingerbeard.automation.state.TimeOfDay;
import nl.gingerbeard.automation.state.TimeOfDayValues;

class Controlloop implements IDomoticzDeviceStatusChanged, IDomoticzTimeOfDayChanged, IDomoticzAlarmChanged {
	private final IEvents events;
	private final IDomoticzUpdateTransmitter transmitter;
	private final ILogger log;
	private final ILogger tracelog;
	private final IState state;

	public Controlloop(final IEvents events, final IDomoticzUpdateTransmitter transmitter, final IState state, final ILogger log) {
		this.events = events;
		this.transmitter = transmitter;
		this.log = log;
		tracelog = log.createContext("trace");
		this.state = state;
	}

	@Override
	public void statusChanged(final StateDevice<?> changedDevice) {
		final EventResult results = events.trigger(changedDevice);
		processEventResult(results);
		if (changedDevice instanceof Subdevice) {
			final Subdevice<?, ?> sub = (Subdevice<?, ?>) changedDevice;
			sub.getParent().ifPresent((parent) -> statusChanged(parent));
		}
	}

	private void processEventResult(final EventResult results) {
		for (final NextState<?> update : filter(results)) {
			try {
				tracelog.info(update.getTrigger() + ": " + update);
				transmitter.transmitDeviceUpdate(update);
			} catch (final IOException e) {
				log.exception(e, "Failed to transmit device update: " + update);
			}
		}
	}

	private List<NextState<?>> filter(final EventResult results) {
		final List<NextState<?>> filtered = new ArrayList<>();
		results.getAll().stream().forEach((result) -> filtered.addAll(filter(result)));
		return filtered;
	}

	private List<NextState<?>> filter(final Object input) {
		final List<NextState<?>> filtered = new ArrayList<>();
		if (isNextState(input) && isDifferentState((NextState<?>) input)) {
			filtered.add((NextState<?>) input);
		} else if (isCollection(input)) {
			((Collection<?>) input).stream().forEach((item) -> filtered.addAll(filter(item)));
		}
		return filtered;
	}

	private boolean isDifferentState(final NextState<?> nextState) {
		return !nextState.get().equals(nextState.getDevice().getState());
	}

	private boolean isNextState(final Object result) {
		return result.getClass().isAssignableFrom(NextState.class);
	}

	private boolean isCollection(final Object result) {
		return Collection.class.isAssignableFrom(result.getClass());
	}

	@Override
	public void timeChanged(final TimeOfDayValues time) {
		final TimeOfDay prevTod = state.getTimeOfDay();
		updateTimeState(time);
		if (state.getTimeOfDay() != prevTod) {
			final EventResult result = events.trigger(state.getTimeOfDay());
			processEventResult(result);
		}
	}

	private void updateTimeState(final TimeOfDayValues time) {
		final TimeOfDay newTimeOfDay = time.isDayTime() ? TimeOfDay.DAYTIME : TimeOfDay.NIGHTTIME;
		state.setTimeOfDay(newTimeOfDay);
	}

	@Override
	public void alarmChanged(final AlarmState newState) {
		final AlarmState curState = state.getAlarmState();
		if (curState != newState) {
			state.setAlarmState(newState);
			final EventResult results = events.trigger(state.getAlarmState());
			processEventResult(results);
		}
	}

}
