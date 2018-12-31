package nl.gingerbeard.automation.devices;

public class SmokeCo2Alarm extends OnOffDevice {

	public SmokeCo2Alarm(final int idx) {
		super(idx);
	}

	@Override
	public boolean updateState(final String newState) {
		return false;
	}

}
