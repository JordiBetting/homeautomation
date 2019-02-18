package nl.gingerbeard.automation.domoticz;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.gson.Gson;

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
		final HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setUseCaches(false);
		con.setRequestMethod("GET");
		con.connect();
		final int httpCode = con.getResponseCode();
		if (httpCode != 200) {
			throw new IOException("responsecode expected 200, but was: " + httpCode);
		}

		final GetSunRiseSet domoticzTime = gson.fromJson(new InputStreamReader(con.getInputStream()), GetSunRiseSet.class);

		// TODO: fully fill timeOfDayValues and provide as info to controllers.
		final int civTwilightEnd = domoticzTime.getCivTwilightEnd();
		final int civTwilightStart = domoticzTime.getCivTwilightStart();
		return new TimeOfDayValues(curtime, sunrise, sunset, civTwilightStart, civTwilightEnd);
	}

	private static class GetSunRiseSet {
		private String CivTwilightEnd;// " : "18:32",
		private String CivTwilightStart;// " : "07:16",

		@SuppressWarnings("unused") // used by Gson
		public GetSunRiseSet() {
		}

		private int toMinutes(final String time) throws IOException {
			final String[] parts = time.split(":");
			if (parts.length != 2) {
				throw new IOException("Invalid input, could not find single : in " + time);
			}
			try {
				final int hours = Integer.parseInt(parts[0]);
				final int minutes = Integer.parseInt(parts[1]);
				return hours * 60 + minutes;
			} catch (final NumberFormatException nfe) {
				throw new IOException(nfe);
			}
		}

		public int getCivTwilightEnd() throws IOException {
			return toMinutes(CivTwilightEnd);
		}

		public int getCivTwilightStart() throws IOException {
			return toMinutes(CivTwilightStart);
		}

	}
}
