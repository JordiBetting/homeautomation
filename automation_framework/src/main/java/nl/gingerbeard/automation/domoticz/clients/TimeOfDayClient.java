package nl.gingerbeard.automation.domoticz.clients;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

import com.google.common.base.Charsets;
import com.google.gson.Gson;

import nl.gingerbeard.automation.domoticz.clients.json.GetSunRiseSet;
import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration;
import nl.gingerbeard.automation.state.TimeOfDayValues;

public class TimeOfDayClient {

	private final DomoticzConfiguration config;
	private static final Gson gson = new Gson();

	public TimeOfDayClient(final DomoticzConfiguration config) {
		this.config = config;
	}

	public TimeOfDayValues createTimeOfDayValues(final int curtime, final int sunrise, final int sunset) throws IOException {
		final URL url = new URL(config.getBaseURL().toString() + "/json.htm?type=command&param=getSunRiseSet");
		final GetSunRiseSet domoticzTime = getSunRiseSet(url);
		return createTimeOfDayValues(curtime, sunrise, sunset, domoticzTime);
	}

	private GetSunRiseSet getSunRiseSet(final URL url) throws IOException {
		final InputStreamReader responseBodyReader = executeRequest(url);
		return parseJson(responseBodyReader);
	}

	private GetSunRiseSet parseJson(final InputStreamReader responseBodyReader) {
		return gson.fromJson(responseBodyReader, GetSunRiseSet.class);
	}

	private TimeOfDayValues createTimeOfDayValues(final int curtime, final int sunrise, final int sunset, final GetSunRiseSet domoticzTime) throws IOException {
		// TODO: fully fill timeOfDayValues and provide as info to controllers.
		final int civTwilightEnd = domoticzTime.getCivilTwilightEnd();
		final int civTwilightStart = domoticzTime.getCivilTwilightStart();
		return new TimeOfDayValues(curtime, sunrise, sunset, civTwilightStart, civTwilightEnd);
	}

	private InputStreamReader executeRequest(final URL url) throws IOException, ProtocolException {
		final HttpURLConnection con = createConnection(url);
		validateResponseCode(con);
		return new InputStreamReader(con.getInputStream(), Charsets.UTF_8);
	}

	private void validateResponseCode(final HttpURLConnection con) throws IOException {
		final int httpCode = con.getResponseCode();
		if (httpCode != 200) {
			throw new IOException("responsecode expected 200, but was: " + httpCode);
		}
	}

	private HttpURLConnection createConnection(final URL url) throws IOException, ProtocolException {
		final HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setUseCaches(false);
		con.setRequestMethod("GET");
		con.connect();
		return con;
	}

}
