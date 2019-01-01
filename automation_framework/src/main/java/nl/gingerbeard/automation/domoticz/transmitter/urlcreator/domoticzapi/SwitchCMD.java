package nl.gingerbeard.automation.domoticz.transmitter.urlcreator.domoticzapi;

import nl.gingerbeard.automation.domoticz.transmitter.urlcreator.QueryStringItem;

public enum SwitchCMD implements QueryStringItem {
	SET_LEVEL("Set Level"), //
	;

	private final String customName;

	SwitchCMD(final String customName) {
		this.customName = customName;
	}

	@Override
	public String getName() {
		return customName;
	}

}