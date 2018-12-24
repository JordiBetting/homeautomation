package nl.gingerbeard.automation.devices;

public class Switch extends OnOffDevice {

	public Switch(final int idx, final int batteryDomoticzId) {
		super(idx, batteryDomoticzId);
	}

	public Switch(final int idx) {
		super(idx);
	}

}
