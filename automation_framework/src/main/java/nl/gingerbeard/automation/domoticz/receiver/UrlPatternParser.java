package nl.gingerbeard.automation.domoticz.receiver;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class UrlPatternParser {

	private static final Pattern URIPATTERN = Pattern.compile("/([0-9]+)/([0-9a-zA-Z_]+)/?");

	private UrlPatternParser() {
		// avoid instantiation
	}

	/**
	 * Parses the idx and state from a uri. matches /id/action/ e.g. /123/close or /123/close/
	 *
	 * @param uri
	 *            The URI to parse (e.g. /4/off)
	 * @return Parsed results, or {@link Optional#empty()} when uri does not match.
	 */
	static Optional<ResponseParameters> parseParameters(final String uri) {
		final Matcher m = URIPATTERN.matcher(uri);
		final boolean matches = m.matches();
		Optional<ResponseParameters> responseParams = Optional.empty();
		if (matches) {
			responseParams = Optional.of(new ResponseParameters(Integer.parseInt(m.group(1)), m.group(2)));
		}
		return responseParams;
	}
}
