package nl.gingerbeard.automation.controlloop;

import nl.gingerbeard.automation.devices.Device;
import nl.gingerbeard.automation.domoticz.IDomoticzDeviceStatusChanged;
import nl.gingerbeard.automation.event.IEvents;

class Controlloop implements IDomoticzDeviceStatusChanged {
	private final IEvents events;

	public Controlloop(final IEvents events) {
		this.events = events;
	}

	// domoticz event: add change [trigger=device], commandArray['OpenURL']='www.yourdomain.com/api/movecamtopreset.cgi' with device ID of changed device
	@Override
	public void statusChanged(final Device<?> changedDevice) {
		events.trigger(changedDevice);
	}

	// public void stateChanged() {
	//
	// }

}
