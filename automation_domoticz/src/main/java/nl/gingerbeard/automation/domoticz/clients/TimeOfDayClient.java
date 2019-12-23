package nl.gingerbeard.automation.domoticz.clients;

import java.io.IOException;
import java.io.InputStreamReader;

import nl.gingerbeard.automation.domoticz.clients.json.GetSunRiseSet;
import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration;
import nl.gingerbeard.automation.logging.ILogger;
import nl.gingerbeard.automation.state.TimeOfDayValues;

public class TimeOfDayClient extends GetClient {

	public TimeOfDayClient(final DomoticzConfiguration config, final ILogger log) throws IOException {
		super(config, log, "/json.htm?type=command&param=getSunRiseSet");
	}

	public TimeOfDayValues createTimeOfDayValues() throws IOException {
		final GetSunRiseSet domoticzTime = getSunRiseSet();
		return createTimeOfDayValues(domoticzTime);
	}

	private GetSunRiseSet getSunRiseSet() throws IOException {
		final InputStreamReader responseBodyReader = executeRequest();
		return gson.fromJson(responseBodyReader, GetSunRiseSet.class);
	}

	private TimeOfDayValues createTimeOfDayValues(final GetSunRiseSet domoticzTime) throws IOException {
		// TODO: fully fill timeOfDayValues and provide as info to controllers.
		final int civTwilightEnd = domoticzTime.getCivilTwilightEnd();
		final int civTwilightStart = domoticzTime.getCivilTwilightStart();
		final int curtime = domoticzTime.getCurrentTime();
		final int sunrise = domoticzTime.getSunrise();
		final int sunset = domoticzTime.getSunSet();
		return new TimeOfDayValues(curtime, sunrise, sunset, civTwilightStart, civTwilightEnd);
	}

}
