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
import nl.gingerbeard.automation.state.NextState;
import nl.gingerbeard.automation.state.State;
import nl.gingerbeard.automation.state.TimeOfDay;
import nl.gingerbeard.automation.state.TimeOfDayValues;

class Controlloop implements IDomoticzDeviceStatusChanged, IDomoticzTimeOfDayChanged, IDomoticzAlarmChanged {
	private final IEvents events;
	private final IDomoticzUpdateTransmitter transmitter;
	private final ILogger log;
	private final State state;

	public Controlloop(final IEvents events, final IDomoticzUpdateTransmitter transmitter, final State state, final ILogger log) {
		this.events = events;
		this.transmitter = transmitter;
		this.log = log;
		this.state = state;
	}

	// TODO domoticz event: add change [trigger=device], commandArray['OpenURL']='www.yourdomain.com/api/movecamtopreset.cgi' with device ID of changed device
	@Override
	public void statusChanged(final StateDevice<?> changedDevice) {
		final EventResult results = events.trigger(changedDevice);
		for (final NextState<?> update : filter(results)) {
			try {
				transmitter.transmitDeviceUpdate(update);
			} catch (final IOException e) {
				log.exception(e, "Failed to transmit device update: " + update);
			}
		}
		if (changedDevice instanceof Subdevice) {
			final Subdevice<?, ?> sub = (Subdevice<?, ?>) changedDevice;
			sub.getParent().ifPresent((parent) -> statusChanged(parent));
		}
	}

	private List<NextState<?>> filter(final EventResult results) {
		final List<NextState<?>> filtered = new ArrayList<>();
		for (final Object result : results.getAll()) {
			filtered.addAll(filter(result));
		}
		return filtered;
	}

	private List<NextState<?>> filter(final Object input) {
		final List<NextState<?>> filtered = new ArrayList<>();
		if (isNextState(input)) {
			filtered.add((NextState<?>) input);
		} else if (isCollection(input)) {
			for (final Object item : (Collection<?>) input) {
				filtered.addAll(filter(item)); // recursive search
			}
		}
		return filtered;
	}

	private boolean isNextState(final Object result) {
		return result.getClass().isAssignableFrom(NextState.class);
	}

	private boolean isCollection(final Object result) {
		return Collection.class.isAssignableFrom(result.getClass());
	}

	@Override
	public boolean timeChanged(final TimeOfDayValues time) {
		final TimeOfDay prevTod = state.getTimeOfDay();
		updateTimeState(time);
		if (state.getTimeOfDay() != prevTod) {
			events.trigger(state.getTimeOfDay());
		}
		return true;
	}

	private void updateTimeState(final TimeOfDayValues time) {
		final TimeOfDay newTimeOfDay = time.isDayTime() ? TimeOfDay.DAYTIME : TimeOfDay.NIGHTTIME;
		state.setTimeOfDay(newTimeOfDay);
	}

	@Override
	public boolean alarmChanged(final AlarmState newState) {
		final AlarmState curState = state.getAlarmState();
		if (curState != newState) {
			state.setAlarmState(newState);
			events.trigger(state.getAlarmState());
		}
		return true;
	}

}
