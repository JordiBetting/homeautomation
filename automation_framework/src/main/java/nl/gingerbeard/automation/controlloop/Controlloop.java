package nl.gingerbeard.automation.controlloop;

import nl.gingerbeard.automation.devices.Device;
import nl.gingerbeard.automation.event.Events;

public class Controlloop {

	private final Events events;

	public Controlloop(final Events events) {
		this.events = events;
	}

	// domoticz event: add change [trigger=device], commandArray['OpenURL']='www.yourdomain.com/api/movecamtopreset.cgi' with device ID of changed device
	public void triggerDeviceChanged(final Device changedDevice) {
		events.trigger(changedDevice);
	}

	// public void stateChanged() {
	//
	// }

}
