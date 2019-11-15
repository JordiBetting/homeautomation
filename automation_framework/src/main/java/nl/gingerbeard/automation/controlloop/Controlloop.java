package nl.gingerbeard.automation.controlloop;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import nl.gingerbeard.automation.devices.OnkyoReceiver.OnkyoSubdevice;
import nl.gingerbeard.automation.devices.StateDevice;
import nl.gingerbeard.automation.devices.Subdevice;
import nl.gingerbeard.automation.domoticz.api.DomoticzApi;
import nl.gingerbeard.automation.domoticz.api.DomoticzException;
import nl.gingerbeard.automation.domoticz.api.IDomoticzAlarmChanged;
import nl.gingerbeard.automation.domoticz.api.IDomoticzDeviceStatusChanged;
import nl.gingerbeard.automation.domoticz.api.IDomoticzTimeOfDayChanged;
import nl.gingerbeard.automation.event.EventResult;
import nl.gingerbeard.automation.event.IEvents;
import nl.gingerbeard.automation.logging.ILogger;
import nl.gingerbeard.automation.onkyo.IOnkyoTransmitter;
import nl.gingerbeard.automation.state.AlarmState;
import nl.gingerbeard.automation.state.IState;
import nl.gingerbeard.automation.state.NextState;
import nl.gingerbeard.automation.state.TimeOfDayValues;

class Controlloop implements IDomoticzDeviceStatusChanged, IDomoticzTimeOfDayChanged, IDomoticzAlarmChanged {
	private final IEvents events;
	private final DomoticzApi domoticz;
	private final ILogger log;
	private final ILogger tracelog;
	private final IState state;
	private IOnkyoTransmitter onkyoTransmitter;

	public Controlloop(final IEvents events, final DomoticzApi domoticz, final IState state,
			final ILogger log, IOnkyoTransmitter onkyoTransmitter) {
		this.events = events;
		this.domoticz = domoticz;
		this.log = log;
		this.onkyoTransmitter = onkyoTransmitter;
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
				if (isOnkyoDevice(update)) { // TODO: Chain of responsibility of transmitters
					onkyoTransmitter.transmit(update);
				} else {
					domoticz.transmitDeviceUpdate(update);
				}
			} catch (final DomoticzException e) {
				log.exception(e, "Failed to transmit device update: " + update);
			}
		}
	}

	private boolean isOnkyoDevice(NextState<?> update) {
		return OnkyoSubdevice.class.isAssignableFrom(update.getDevice().getClass());
	}

	private List<NextState<?>> filter(final EventResult results) {
		final List<NextState<?>> filtered = new ArrayList<>();
		results.getAll().stream().forEach((result) -> filtered.addAll(filter(result)));
		return filtered;
	}

	private List<NextState<?>> filter(final Object input) {
		final List<NextState<?>> filtered = new ArrayList<>();
		if (isNextState(input) && mustReportNextState((NextState<?>) input)) {
			filtered.add((NextState<?>) input);
		} else if (isCollection(input)) {
			((Collection<?>) input).stream().forEach((item) -> filtered.addAll(filter(item)));
		}
		return filtered;
	}

	private boolean mustReportNextState(final NextState<?> nextState) {
		return isDifferentState(nextState) || isAlwaysReported(nextState);
	}

	private boolean isAlwaysReported(final NextState<?> nextState) {
		return !nextState.getDevice().reportOnUpdateOnly();
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
		final EventResult result = events.trigger(state.getTimeOfDay());
		processEventResult(result);
	}


	@Override
	public void alarmChanged(final AlarmState newState) {
		final EventResult results = events.trigger(newState);
		processEventResult(results);
	}


}
