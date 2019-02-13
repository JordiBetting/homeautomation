package nl.gingerbeard.automation.domoticz.transmitter.urlcreator.devicetypes;

import nl.gingerbeard.automation.domoticz.transmitter.urlcreator.ChainOfCommandType;
import nl.gingerbeard.automation.domoticz.transmitter.urlcreator.QueryStringItem;
import nl.gingerbeard.automation.domoticz.transmitter.urlcreator.URLBuilder;
import nl.gingerbeard.automation.domoticz.transmitter.urlcreator.domoticzapi.Keys;
import nl.gingerbeard.automation.domoticz.transmitter.urlcreator.domoticzapi.Param;
import nl.gingerbeard.automation.domoticz.transmitter.urlcreator.domoticzapi.SwitchCMD;
import nl.gingerbeard.automation.domoticz.transmitter.urlcreator.domoticzapi.Type;
import nl.gingerbeard.automation.state.NextState;
import nl.gingerbeard.automation.state.OpenCloseState;

public class OpenCloseType extends ChainOfCommandType<OpenCloseState> {

	public OpenCloseType() {
		super(OpenCloseState.class);
	}

	@Override
	protected void createUrl(final URLBuilder builder, final NextState<OpenCloseState> nextState) {
		// tested with shutters
		builder//
				.add(Keys.TYPE, Type.COMMAND) //
				.add(Keys.PARAM, Param.SWITCHLIGHT) //
				.addIdx(nextState) //
				.add(Keys.SWITCHCMD, getSwitchCmd(nextState.get()))//
				.add(Keys.LEVEL, 0);
	}

	private QueryStringItem getSwitchCmd(final OpenCloseState state) {
		return state == OpenCloseState.CLOSE ? SwitchCMD.OFF : SwitchCMD.ON;
	}

}
