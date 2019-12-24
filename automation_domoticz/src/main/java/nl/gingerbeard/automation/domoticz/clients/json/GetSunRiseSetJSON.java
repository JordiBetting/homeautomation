package nl.gingerbeard.automation.domoticz.clients.json;

import java.io.IOException;

import com.google.gson.annotations.SerializedName;

public class GetSunRiseSetJSON {
	@SerializedName("CivTwilightEnd")
	private String civilTwilightEnd;

	@SerializedName("CivTwilightStart")
	private String civilTwilightStart;

	@SerializedName("Sunrise")
	private String sunrise;

	@SerializedName("Sunset")
	private String sunset;

	@SerializedName("ServerTime")
	private String serverTime;

	private int toMinutes(final String time) throws IOException {
		final String[] parts = time.split(":");
		if (parts.length < 2) {
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

	public final int getCivilTwilightEnd() throws IOException {
		return toMinutes(civilTwilightEnd);
	}

	public final int getCivilTwilightStart() throws IOException {
		return toMinutes(civilTwilightStart);
	}

	public final int getSunrise() throws IOException {
		return toMinutes(sunrise);
	}

	public final int getSunSet() throws IOException {
		return toMinutes(sunset);
	}

	public int getCurrentTime() throws IOException {
		String[] parts = serverTime.split(" ");
		if (parts.length != 2) {
			throw new IOException("Invalid input, expected hh:mm:ss in " + serverTime);
		}
		return toMinutes(parts[1]);
	}

}