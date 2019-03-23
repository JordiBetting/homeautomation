package nl.gingerbeard.automation.domoticz.transmitter.urlcreator;

import java.net.MalformedURLException;
import java.net.URL;

import nl.gingerbeard.automation.devices.Device;
import nl.gingerbeard.automation.devices.StateDevice;
import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration;
import nl.gingerbeard.automation.domoticz.transmitter.urlcreator.domoticzapi.Keys;
import nl.gingerbeard.automation.state.NextState;

public final class URLBuilder {

	private final StringBuilder url;

	// for testing
	URLBuilder(final String baseUrl) {
		url = new StringBuilder();
		url.append(baseUrl);
		url.append("/json.htm?");
	}

	public URLBuilder(final DomoticzConfiguration config) {
		this(config.getBaseURL().toString());
	}

	public URLBuilder addIdx(final NextState<?> nextState) {
		final StateDevice<?> device = nextState.getDevice();
		// throws cast exception when it is not a device, which means it is an implemnetation error in DomoticzUrls class. Therefore, accepted.
		final int idx = ((Device<?>) device).getIdx();
		return add(Keys.IDX, idx);
	}

	public URL build() {
		final int lastCharIndex = url.length() - 1;
		final String fullUrlString = url.substring(0, lastCharIndex); // trim off ? or &
		try {
			return new URL(fullUrlString);
		} catch (final MalformedURLException e) {
			return null;
		}
	}

	public static URLBuilder create(final DomoticzConfiguration config) {
		return new URLBuilder(config);
	}

	public URLBuilder add(final QueryStringItem key, final QueryStringItem value) {
		final String keyString = key.getString();
		final String valueString = value.getString();
		add(keyString, valueString);
		return this;
	}

	public URLBuilder add(final QueryStringItem key, final Object value) {
		final String keyString = key.getString();
		final String valueString = value.toString();
		add(keyString, valueString);
		return this;
	}

	private void add(final String keyString, final String valueString) {
		url.append(keyString);
		url.append('=');
		url.append(valueString);
		url.append('&');
	}
}