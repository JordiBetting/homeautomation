package nl.gingerbeard.automation.devices;

public class DimmeableLight extends OnOffDevice {

	private int dimmed; // 0-100
	// TODO: make specific type

	public DimmeableLight(final int idx) {
		super(idx);
	}

	@Override
	public boolean updateState(final String newState) {
		try {
			dimmed = Integer.parseInt(newState);
			return true;
		} catch (final NumberFormatException e) {
			return false;
			// TODO: log
		}
	}

	@Override
	public String getDomoticzSwitchCmd() {
		return "Set%20Level&level=" + dimmed;
	}
}
