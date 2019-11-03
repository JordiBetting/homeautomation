package nl.gingerbeard.automation.devices;

import java.util.Set;

public class OnkyoReceiver extends CompositeDevice<OnkyoReceiverState>{

	protected OnkyoReceiver() {
		// TODO idx does not make sense. For this device, I believe I should use an IP. Subdevices?
		// TODO: Create integration tests to TDD the handling of events
		super(Set.of(new OnkyoZoneMain(1), new OnkyoZone2(1))); 
		setState(new OnkyoReceiverState());
	}
	

}
