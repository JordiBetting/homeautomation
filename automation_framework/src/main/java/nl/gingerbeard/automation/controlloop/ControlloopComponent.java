package nl.gingerbeard.automation.controlloop;

import nl.gingerbeard.automation.domoticz.IDomoticzDeviceStatusChanged;
import nl.gingerbeard.automation.event.IEvents;
import nl.gingerbeard.automation.service.annotation.Activate;
import nl.gingerbeard.automation.service.annotation.Deactivate;
import nl.gingerbeard.automation.service.annotation.Provides;
import nl.gingerbeard.automation.service.annotation.Requires;

public class ControlloopComponent {

	@Requires
	public IEvents events;

	@Provides
	public IDomoticzDeviceStatusChanged listener;

	@Activate
	public void provideListener() {
		listener = new Controlloop(events);
	}

	@Deactivate
	public void removeListener() {
		listener = null;
	}
}
