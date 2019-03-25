package nl.gingerbeard.automation.domoticz.transmitter.urlcreator.devicetypes;

import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration;
import nl.gingerbeard.automation.state.NextState;

public class Parameter<T> {
	final DomoticzConfiguration configuration;
	final NextState<T> nextState;

	public Parameter(final DomoticzConfiguration configuration, final NextState<T> nextState) {
		this.configuration = configuration;
		this.nextState = nextState;
	}

}
