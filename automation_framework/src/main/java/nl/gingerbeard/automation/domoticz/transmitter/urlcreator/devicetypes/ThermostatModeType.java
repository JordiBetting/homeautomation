package nl.gingerbeard.automation.domoticz.transmitter.urlcreator.devicetypes;

import nl.gingerbeard.automation.domoticz.transmitter.urlcreator.ChainOfCommandType;
import nl.gingerbeard.automation.domoticz.transmitter.urlcreator.URLBuilder;
import nl.gingerbeard.automation.domoticz.transmitter.urlcreator.domoticzapi.Keys;
import nl.gingerbeard.automation.domoticz.transmitter.urlcreator.domoticzapi.Type;
import nl.gingerbeard.automation.state.NextState;
import nl.gingerbeard.automation.state.ThermostatState.ThermostatMode;

public class ThermostatModeType extends ChainOfCommandType<ThermostatMode> {

	public ThermostatModeType() {
		super(ThermostatMode.class);
	}

	@Override
	protected void createUrl(final URLBuilder builder, final NextState<ThermostatMode> nextState) {
		builder.add(Keys.TYPE, Type.SETUSED) //
				.addIdx(nextState) //
				.add(Keys.TMODE, getThermostatTMode(nextState.get())) //
				.add(Keys.PROTECTED, "false")//
				.add(Keys.USED, "true");
	}

	private int getThermostatTMode(final ThermostatMode thermostatMode) {
		if (thermostatMode == ThermostatMode.FULL_HEAT) {
			return 3;
		} else if (thermostatMode == ThermostatMode.SETPOINT) {
			return 2;
		} else { // (thermostatMode == ThermostatMode.OFF)
			return 0;
		}
	}

}
