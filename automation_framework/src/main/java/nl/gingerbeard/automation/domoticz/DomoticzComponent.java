package nl.gingerbeard.automation.domoticz;

import java.util.Optional;

import nl.gingerbeard.automation.service.annotation.Activate;
import nl.gingerbeard.automation.service.annotation.Deactivate;
import nl.gingerbeard.automation.service.annotation.Provides;
import nl.gingerbeard.automation.service.annotation.Requires;

public final class DomoticzComponent {
	@Requires
	public Optional<IDomoticzDeviceStatusChanged> listener;

	@Requires
	public IDomoticzEventReceiver domoticzReceiver;

	@Provides
	public IDomoticz domoticz;

	private Domoticz domoticzInstance;

	@Activate
	public void registerReceiver() {
		domoticz = domoticzInstance = new Domoticz(listener);
		domoticzReceiver.setEventListener(domoticzInstance);
	}

	@Deactivate
	public void unregisterReceiver() {
		domoticzReceiver.setEventListener(null);
		domoticz = domoticzInstance = null;
	}
}
