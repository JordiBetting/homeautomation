package nl.gingerbeard.automation.domoticz.clients;

import java.io.IOException;
import java.io.InputStreamReader;

import nl.gingerbeard.automation.domoticz.clients.json.GetSunRiseSet;
import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration;
import nl.gingerbeard.automation.state.TimeOfDayValues;

public class TimeOfDayClient extends GetClient {
	
	public TimeOfDayClient(final DomoticzConfiguration config) throws IOException {
		super(config, "/json.htm?type=command&param=getSunRiseSet");
	}

	public TimeOfDayValues createTimeOfDayValues(final int curtime, final int sunrise, final int sunset) throws IOException {
		final GetSunRiseSet domoticzTime = getSunRiseSet();
		return createTimeOfDayValues(curtime, sunrise, sunset, domoticzTime);
	}

	private GetSunRiseSet getSunRiseSet() throws IOException {
		final InputStreamReader responseBodyReader = executeRequest();
		return gson.fromJson(responseBodyReader, GetSunRiseSet.class);
	}

	private TimeOfDayValues createTimeOfDayValues(final int curtime, final int sunrise, final int sunset, final GetSunRiseSet domoticzTime) throws IOException {
		// TODO: fully fill timeOfDayValues and provide as info to controllers.
		final int civTwilightEnd = domoticzTime.getCivilTwilightEnd();
		final int civTwilightStart = domoticzTime.getCivilTwilightStart();
		return new TimeOfDayValues(curtime, sunrise, sunset, civTwilightStart, civTwilightEnd);
	}



}
