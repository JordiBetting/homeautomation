package nl.gingerbeard.automation.domoticz.transmitter.urlcreator;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration;
import nl.gingerbeard.automation.state.Level;
import nl.gingerbeard.automation.state.NextState;
import nl.gingerbeard.automation.state.OnOffState;
import nl.gingerbeard.automation.state.Temperature;
import nl.gingerbeard.automation.state.Temperature.Unit;
import nl.gingerbeard.automation.state.ThermostatState.ThermostatMode;

public final class DomoticzUrls {

	private final DomoticzConfiguration configuration;

	public DomoticzUrls(final DomoticzConfiguration configuration) {
		this.configuration = configuration;
	}

	@SuppressWarnings("unchecked")
	public URL getUrl(final NextState<?> nextState) throws MalformedURLException {
		if (isStateType(nextState, OnOffState.class)) {
			return constructOnOffState((NextState<OnOffState>) nextState);
		} else if (isStateType(nextState, Level.class)) {
			return constructLevelState((NextState<Level>) nextState);
		} else if (isStateType(nextState, Temperature.class)) {
			return constructTemperatureState((NextState<Temperature>) nextState);
		} else if (isStateType(nextState, ThermostatMode.class)) {
			return constructThermostatMode((NextState<ThermostatMode>) nextState);
		}
		throw new MalformedURLException("Cannot construct url from unsupported state: " + nextState.get().getClass());
	}

	private URL constructThermostatMode(final NextState<ThermostatMode> nextState) throws MalformedURLException {
		return URLBuilder.create(configuration) //
				.add(Keys.TYPE, Type.SETUSED) //
				.addIdx(nextState) //
				.add(Keys.TMODE, getThermostatTMode(nextState.get())) //
				.add(Keys.PROTECTED, "false")//
				.add(Keys.USED, "true")//
				.build();
	}

	private int getThermostatTMode(final ThermostatMode thermostatMode) throws MalformedURLException {
		if (thermostatMode == ThermostatMode.FULL_HEAT) {
			return 3;
		} else if (thermostatMode == ThermostatMode.SETPOINT) {
			return 2;
		} else { // (thermostatMode == ThermostatMode.OFF)
			return 0;
		}
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

}
