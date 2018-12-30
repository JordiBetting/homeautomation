package nl.gingerbeard.automation.domoticz.transmitter;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;

import nl.gingerbeard.automation.devices.Device;
import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration;
import nl.gingerbeard.automation.state.Level;
import nl.gingerbeard.automation.state.NextState;
import nl.gingerbeard.automation.state.OnOffState;
import nl.gingerbeard.automation.state.Temperature;
import nl.gingerbeard.automation.state.Temperature.Unit;

public final class DomoticzUrlCreator {

	private static final Escaper ESCAPER = UrlEscapers.urlFragmentEscaper();
	private final DomoticzConfiguration configuration;

	public DomoticzUrlCreator(final DomoticzConfiguration configuration) {
		this.configuration = configuration;
	}

	private static enum Keys implements QueryStringItem {
		TYPE, //
		IDX, //
		SWITCHCMD, //
		PARAM, //
		LEVEL, //
		SETPOINT, //
		PROTECTED, //
		USED, //
		;

	}

	private static enum Type implements QueryStringItem { // TODO: Split enums to separate files.
		COMMAND, //
		SETUSED, //
		;

	}

	private static enum Param implements QueryStringItem {
		SWITCHLIGHT, //
		;
	}

	private static enum SwitchCMD implements QueryStringItem {
		SET_LEVEL("Set Level"), //
		;

		private final String customName;

		private SwitchCMD(final String customName) {
			this.customName = customName;
		}

		@Override
		public String getName() {
			return customName;
		}

	}

	private static interface QueryStringItem {
		String name();

		default String getName() {
			return name().toLowerCase(Locale.US);
		}

		default String getString() {
			final String string = getName();
			final String withSpaces = string.replace('_', ' ');
			return ESCAPER.escape(withSpaces);
		}
	}

	@SuppressWarnings("unchecked")
	public URL construct(final NextState<?> nextState) throws MalformedURLException {
		if (isStateType(nextState, OnOffState.class)) {
			return constructOnOffState((NextState<OnOffState>) nextState);
		} else if (isStateType(nextState, Level.class)) {
			return constructLevelState((NextState<Level>) nextState);
		} else if (isStateType(nextState, Temperature.class)) {
			return constructTemperatureState((NextState<Temperature>) nextState);
		}
		throw new MalformedURLException("Cannot construct url from unsupported state: " + nextState.get().getClass());
	}

	private URL constructTemperatureState(final NextState<Temperature> nextState) throws MalformedURLException {
		return URLBuilder.create(configuration) //
				.add(Keys.TYPE, Type.SETUSED) //
				.addIdx(nextState) //
				.add(Keys.SETPOINT, nextState.get().get(Unit.CELSIUS)) // TODO: Use settings in domoticz to translate
				.add(Keys.PROTECTED, "false")//
				.add(Keys.USED, "true")//
				.build();
	}

	private URL constructOnOffState(final NextState<OnOffState> nextState) throws MalformedURLException {
		return URLBuilder.create(configuration) //
				.add(Keys.TYPE, Type.COMMAND) //
				.add(Keys.PARAM, Param.SWITCHLIGHT)//
				.addIdx(nextState) // TODO consider IDX to be generic
				.add(Keys.SWITCHCMD, getValue(nextState))//
				.build();
	}

	private URL constructLevelState(final NextState<Level> nextState) throws MalformedURLException {
		return URLBuilder.create(configuration) //
				.add(Keys.TYPE, Type.COMMAND) //
				.add(Keys.PARAM, Param.SWITCHLIGHT) //
				.addIdx(nextState) //
				.add(Keys.SWITCHCMD, SwitchCMD.SET_LEVEL)//
				.add(Keys.LEVEL, nextState.get().getLevel()) //
				.build();
	}

	private String getValue(final NextState<OnOffState> nextState) {
		return nextState.get().name().toLowerCase(Locale.US);
	}

	private boolean isStateType(final NextState<?> nextState, final Class<?> testState) {
		return nextState.get().getClass().isAssignableFrom(testState);
	}

	private static final class URLBuilder { // TODO: Split builder to separate file.

		private final StringBuilder url;

		public URLBuilder(final DomoticzConfiguration config) {
			url = new StringBuilder();
			url.append(config.getBaseURL());
			url.append("/json.htm?");
		}

		public URLBuilder addIdx(final NextState<?> nextState) {
			final Device<?> device = nextState.getDevice();
			final int idx = device.getIdx();
			return add(Keys.IDX, idx);
		}

		public URL build() throws MalformedURLException {
			final int lastCharIndex = url.length() - 1;
			final String fullUrlString = url.substring(0, lastCharIndex); // trim off ? or &
			System.out.println(fullUrlString);
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
}
