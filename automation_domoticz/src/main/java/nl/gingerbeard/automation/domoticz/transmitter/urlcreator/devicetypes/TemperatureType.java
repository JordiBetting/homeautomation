package nl.gingerbeard.automation.domoticz.transmitter.urlcreator.devicetypes;

import nl.gingerbeard.automation.domoticz.transmitter.urlcreator.URLBuilder;
import nl.gingerbeard.automation.domoticz.transmitter.urlcreator.domoticzapi.Keys;
import nl.gingerbeard.automation.domoticz.transmitter.urlcreator.domoticzapi.Type;
import nl.gingerbeard.automation.state.NextState;
import nl.gingerbeard.automation.state.Temperature;
import nl.gingerbeard.automation.state.Temperature.Unit;

public class TemperatureType extends ChainOfCommandType<Temperature> {

	public TemperatureType() {
		super(Temperature.class);
	}

	@Override
	protected void createUrl(final URLBuilder builder, final NextState<Temperature> nextState) {
		builder//
				.add(Keys.TYPE, Type.SETUSED) //
				.addIdx(nextState) //
				.add(Keys.SETPOINT, nextState.get().get(Unit.CELSIUS)) // TODO: Use settings in domoticz to translate
				.add(Keys.PROTECTED, "false")//
				.add(Keys.USED, "true");
	}

}
