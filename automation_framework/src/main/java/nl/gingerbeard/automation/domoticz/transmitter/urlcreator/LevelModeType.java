package nl.gingerbeard.automation.domoticz.transmitter.urlcreator;

import nl.gingerbeard.automation.state.Level;
import nl.gingerbeard.automation.state.NextState;

public class LevelModeType extends ChainOfCommandType<Level> {

	public LevelModeType() {
		super(Level.class);
	}

	@Override
	protected void createUrl(final URLBuilder builder, final NextState<Level> nextState) {
		builder//
				.add(Keys.TYPE, Type.COMMAND) //
				.add(Keys.PARAM, Param.SWITCHLIGHT) //
				.addIdx(nextState) //
				.add(Keys.SWITCHCMD, SwitchCMD.SET_LEVEL)//
				.add(Keys.LEVEL, nextState.get().getLevel());
	}

}
