package nl.gingerbeard.automation.domoticz.transmitter.urlcreator;

import java.net.MalformedURLException;
import java.net.URL;

import nl.gingerbeard.automation.devices.Device;
import nl.gingerbeard.automation.devices.StateDevice;
import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration;
import nl.gingerbeard.automation.state.NextState;

final class URLBuilder {

	private final StringBuilder url;

	public URLBuilder(final DomoticzConfiguration config) {
		url = new StringBuilder();
		url.append(config.getBaseURL());
		url.append("/json.htm?");
	}

	public URLBuilder addIdx(final NextState<?> nextState) {
		final StateDevice<?> device = nextState.getDevice();
		if (device instanceof Device) {
			final int idx = ((Device<?>) device).getIdx();
			add(Keys.IDX, idx);
		}
		return this;
	}

	public URL build() throws MalformedURLException {
		final int lastCharIndex = url.length() - 1;
		final String fullUrlString = url.substring(0, lastCharIndex); // trim off ? or &
		return new URL(fullUrlString);
	}

	static URLBuilder create(final DomoticzConfiguration config) {
		return new URLBuilder(config);
	}

	URLBuilder add(final QueryStringItem key, final QueryStringItem value) {
		final String keyString = key.getString();
		final String valueString = value.getString();
		add(keyString, valueString);
		return this;
	}

	URLBuilder add(final QueryStringItem key, final Object value) {
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