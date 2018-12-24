package nl.gingerbeard.automation.controlloop;

import nl.gingerbeard.automation.domoticz.IDomoticzDeviceStatusChanged;
import nl.gingerbeard.automation.event.Events;
import nl.gingerbeard.automation.service.annotation.Activate;
import nl.gingerbeard.automation.service.annotation.Deactivate;
import nl.gingerbeard.automation.service.annotation.Provides;
import nl.gingerbeard.automation.service.annotation.Requires;

public class ControlloopComponent {

	@Requires
	public Events events;

	@Provides
	public IDomoticzDeviceStatusChanged listener;

	private Controlloop controlloop;

	@Activate
	public void provideListener() {
		listener = controlloop = new Controlloop(events);
	}

	@Deactivate
	public void removeListener() {
		listener = controlloop = null;
	}
}
