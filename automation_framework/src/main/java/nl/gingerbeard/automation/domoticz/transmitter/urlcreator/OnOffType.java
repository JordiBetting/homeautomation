package nl.gingerbeard.automation.domoticz.transmitter.urlcreator;

import java.util.Locale;

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
				.addIdx(nextState) // TODO consider IDX to be generic
				.add(Keys.SWITCHCMD, getValue(nextState));
	}

	private String getValue(final NextState<OnOffState> nextState) {
		return nextState.get().name().toLowerCase(Locale.US);
	}

}
