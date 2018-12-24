package nl.gingerbeard.automation.devices;

public class DimmeableLight extends OnOffDevice {

	public DimmeableLight(final int idx) {
		super(idx);
	}

	@Override
	public boolean updateState(final String newState) {
		return false; // TODO
	}
}
