package nl.gingerbeard.automation.domoticz.receiver;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class UrlPatternParser {

	private static final Pattern DEVICEPATTERN = Pattern.compile("/device/([0-9]+)/([0-9a-zA-Z_]+)/?");

	private UrlPatternParser() {
		// avoid instantiation
	}

	/**
	 * Parses the idx and state from a uri. matches /device/id/action/ e.g. /device/123/close or /device/123/close/
	 *
	 * @param uri
	 *            The URI to parse (e.g. /device/4/off)
	 * @return Parsed results, or {@link Optional#empty()} when uri does not match.
	 */
	static Optional<ResponseParameters> parseParameters(final String uri) {
		Optional<ResponseParameters> responseParams = Optional.empty();

		final String lcUri = uri.toLowerCase();
		if (lcUri.startsWith("/device/")) {
			responseParams = parseDeviceParameters(uri, responseParams);
		}

		return responseParams;
	}

	private static Optional<ResponseParameters> parseDeviceParameters(final String uri, Optional<ResponseParameters> responseParams) {
		final Matcher m = DEVICEPATTERN.matcher(uri);
		final boolean matches = m.matches();
		if (matches) {
			final int idx = Integer.parseInt(m.group(1));
			final String state = m.group(2);
			responseParams = Optional.of(ResponseParameters.ofDevice(idx, state));
		}
		return responseParams;
	}
}
