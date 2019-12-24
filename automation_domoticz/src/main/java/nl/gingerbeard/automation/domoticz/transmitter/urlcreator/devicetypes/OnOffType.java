package nl.gingerbeard.automation.domoticz.transmitter.urlcreator.devicetypes;

import nl.gingerbeard.automation.domoticz.transmitter.urlcreator.URLBuilder;
import nl.gingerbeard.automation.domoticz.transmitter.urlcreator.domoticzapi.Keys;
import nl.gingerbeard.automation.domoticz.transmitter.urlcreator.domoticzapi.Param;
import nl.gingerbeard.automation.domoticz.transmitter.urlcreator.domoticzapi.Type;
import nl.gingerbeard.automation.state.NextState;
import nl.gingerbeard.automation.state.OnOffState;

public class OnOffType extends ChainOfCommandType<OnOffState> {

	public OnOffType() {
		super(OnOffState.class);
	}

	@Override
	protected void createUrl(final URLBuilder builder, final NextState<OnOffState> nextState) {
		builder//
				.add(Keys.TYPE, Type.COMMAND) //
				.add(Keys.PARAM, Param.SWITCHLIGHT)//
				.addIdx(nextState)//
				.add(Keys.SWITCHCMD, getValue(nextState));
	}

	private String getValue(final NextState<OnOffState> nextState) {
		if (nextState.get() == OnOffState.ON) {
			return "On";
		}
		return "Off";
	}

}
