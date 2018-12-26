package nl.gingerbeard.automation.devices;

import nl.gingerbeard.automation.state.NextState;
import nl.gingerbeard.automation.state.Percentage;

public class DimmeableLight extends Device<Percentage> {
	// TODO: consider 'PercentageDevice' as it may also count for shutters, etc. I don't know if control is the same. Check domoticz

	public DimmeableLight(final int idx) {
		super(idx);
	}

	@Override
	public boolean updateState(final String newState) {
		try {
			setState(new Percentage(Integer.parseInt(newState)));
			return true;
		} catch (final NumberFormatException e) {
			return false;
			// TODO: log
		}
	}

	@Override
	public String getDomoticzSwitchCmd(final NextState<Percentage> nextState) {
		return "Set%20Level&level=" + nextState.get().getPercentage();
	}

	@Override
	public String getDomoticzParam() {
		return "switchlight";
	}
}
