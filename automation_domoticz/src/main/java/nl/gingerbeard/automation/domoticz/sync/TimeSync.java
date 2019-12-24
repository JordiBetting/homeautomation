package nl.gingerbeard.automation.domoticz.sync;

import java.io.IOException;

import nl.gingerbeard.automation.domoticz.clients.TimeOfDayClient;
import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration;
import nl.gingerbeard.automation.logging.ILogger;
import nl.gingerbeard.automation.state.IState;
import nl.gingerbeard.automation.state.TimeOfDay;
import nl.gingerbeard.automation.state.TimeOfDayValues;

public class TimeSync {

	private TimeOfDayClient client;
	private IState state;

	public TimeSync(IState state, DomoticzConfiguration config, ILogger log) throws IOException {
		this(state, new TimeOfDayClient(config, log));
	}

	public TimeSync(IState state, TimeOfDayClient client) {
		this.state = state;
		this.client = client;
	}

	public void syncTime() throws IOException {
		TimeOfDayValues timeOfDayValues = client.createTimeOfDayValues();
		TimeOfDay timeOfDay = toTimeOfDay(timeOfDayValues);
		state.setTimeOfDay(timeOfDay);
	}

	private TimeOfDay toTimeOfDay(final TimeOfDayValues timeOfDayValues) {
		final TimeOfDay newTimeOfDay = timeOfDayValues.isDayTime() ? TimeOfDay.DAYTIME : TimeOfDay.NIGHTTIME;
		return newTimeOfDay;
	}
}
