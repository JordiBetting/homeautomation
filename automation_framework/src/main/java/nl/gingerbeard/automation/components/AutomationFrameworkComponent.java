package nl.gingerbeard.automation.components;

import nl.gingerbeard.automation.AutomationFramework;
import nl.gingerbeard.automation.IAutomationFrameworkInterface;
import nl.gingerbeard.automation.autocontrol.AutoControlToDomoticz;
import nl.gingerbeard.automation.deviceregistry.IDeviceRegistry;
import nl.gingerbeard.automation.domoticz.transmitter.IDomoticzUpdateTransmitter;
import nl.gingerbeard.automation.event.IEvents;
import nl.gingerbeard.automation.logging.ILogger;
import nl.gingerbeard.automation.service.annotation.Activate;
import nl.gingerbeard.automation.service.annotation.Deactivate;
import nl.gingerbeard.automation.service.annotation.Provides;
import nl.gingerbeard.automation.service.annotation.Requires;
import nl.gingerbeard.automation.state.IState;

public class AutomationFrameworkComponent {

	@Requires
	public IEvents events;

	@Provides
	public IAutomationFrameworkInterface framework;

	@Requires
	public IDeviceRegistry deviceRegistry;

	@Requires
	public IState state;

	@Requires
	public ILogger logger;

	@Requires
	public IDomoticzUpdateTransmitter transmitter;

	@Activate
	public void createFramework() {
		final AutoControlToDomoticz autoControlToDomoticz = new AutoControlToDomoticz(logger, transmitter);
		framework = new AutomationFramework(events, deviceRegistry, state, autoControlToDomoticz, logger);
	}

	@Deactivate
	public void removeFramework() {
		framework = null;
	}

}
