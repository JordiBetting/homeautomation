package nl.gingerbeard.automation.devices;

public class Group extends OnOffDevice {

	public Group(int idx) {
		super(idx);
	}
	
	@Override
	public boolean reportOnUpdateOnly() {
		return false;
	}

}
