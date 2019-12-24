package nl.gingerbeard.automation.domoticz.receiver;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class UrlPatternParser {

	private static final Pattern DEVICEPATTERN = Pattern.compile("/device/([0-9]+)/([0-9a-zA-Z_]+)/?");
	private static final Pattern TIMEPATTERN = Pattern.compile("/time/([0-9]{1,4})/([0-9]{1,4})/([0-9]{1,4})/?");
	private static final Pattern ALARMPATTERN = Pattern.compile("/alarm/(arm_away|arm_home|disarmed)/?");

	private UrlPatternParser() {
		// avoid instantiation
	}

	/**
	 * Parses:
	 * <ul>
	 * <li><b>Device</b>: the idx and state from a uri. Matches /device/id/action/ e.g. /device/123/close or /device/123/close/
	 * <li><b>Time</b>: the current time, sunrise time and sunset time (in minutes from start of day). Matches /time/current/sunrise/sunset e.g. /time/20/360/1200
	 * </ul>
	 *
	 * @param uri
	 *            The URI to parse (e.g. /device/4/off)
	 * @return Parsed results, or {@link Optional#empty()} when uri does not match.
	 */
	static Optional<ResponseParameters> parseParameters(final String uri) {
		Optional<ResponseParameters> responseParams = Optional.empty();

		final String lcUri = uri.toLowerCase(Locale.US);
		if (lcUri.startsWith("/device/")) {
			responseParams = parseDeviceParameters(lcUri);
		} else if (lcUri.startsWith("/time/")) {
			responseParams = parseTimeParameters(lcUri);
		} else if (lcUri.startsWith("/alarm/")) {
			responseParams = parseAlarmParameters(lcUri);
		}

		return responseParams;
	}

	private static Optional<ResponseParameters> parseAlarmParameters(final String uri) {
		Optional<ResponseParameters> responseParams = Optional.empty();

		final Matcher m = ALARMPATTERN.matcher(uri);
		if (m.matches()) {
			final String newState = m.group(1);
			responseParams = Optional.of(ResponseParameters.ofAlarm(newState));
		}

		return responseParams;
	}

	private static Optional<ResponseParameters> parseTimeParameters(final String uri) {
		Optional<ResponseParameters> responseParams = Optional.empty();

		final Matcher m = TIMEPATTERN.matcher(uri);
		if (m.matches()) {
			final int curtime = Integer.parseInt(m.group(1));
			final int sunrise = Integer.parseInt(m.group(2));
			final int sunset = Integer.parseInt(m.group(3));
			responseParams = Optional.of(ResponseParameters.ofTime(curtime, sunrise, sunset));
		}

		return responseParams;
	}

	private static Optional<ResponseParameters> parseDeviceParameters(final String uri) {
		Optional<ResponseParameters> responseParams = Optional.empty();

		final Matcher m = DEVICEPATTERN.matcher(uri);
		if (m.matches()) {
			final int idx = Integer.parseInt(m.group(1));
			final String state = m.group(2);
			responseParams = Optional.of(ResponseParameters.ofDevice(idx, state));
		}
		return responseParams;
	}
}
