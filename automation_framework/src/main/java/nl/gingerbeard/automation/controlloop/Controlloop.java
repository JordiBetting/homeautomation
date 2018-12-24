package nl.gingerbeard.automation.controlloop;

import nl.gingerbeard.automation.devices.Device;
import nl.gingerbeard.automation.domoticz.IDomoticzDeviceStatusChanged;
import nl.gingerbeard.automation.event.Events;
import nl.gingerbeard.automation.service.annotation.Activate;
import nl.gingerbeard.automation.service.annotation.Deactivate;
import nl.gingerbeard.automation.service.annotation.Provides;
import nl.gingerbeard.automation.service.annotation.Requires;

public class Controlloop implements IDomoticzDeviceStatusChanged {

	@Requires
	public Events events;

	@Provides
	public IDomoticzDeviceStatusChanged listener;

	@Activate
	public void provideListener() {
		listener = this;
	}

	@Deactivate
	public void removeListener() {
		listener = null;
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
