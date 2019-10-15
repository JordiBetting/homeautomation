package nl.gingerbeard.automation.devices;

public class Scene extends OnOffDevice {

	public Scene(int idx) {
		super(idx);
	}
	
	@Override
	public boolean reportOnUpdateOnly() {
		return false;
	}

}
