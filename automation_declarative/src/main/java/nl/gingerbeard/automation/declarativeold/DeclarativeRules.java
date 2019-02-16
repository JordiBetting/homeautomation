package nl.gingerbeard.automation.declarativeold;

import nl.gingerbeard.automation.devices.Device;
import nl.gingerbeard.automation.logging.ILogger;

public class DeclarativeRules {

	private final DeclarativeRulesRegistry ruleRegistry;
	private final IDeviceUpdate output;

	public DeclarativeRules(final ILogger log, final IDeviceUpdate output) {
		this.output = output;
		ruleRegistry = new DeclarativeRulesRegistry(log);
	}

	public <T> DeclarativeRule when(final Device<T> device, final T expectedState) {
		final DeclarativeRule declarativeRule = new DeclarativeRule(ruleRegistry, device, output, expectedState);
		ruleRegistry.add(device, declarativeRule);
		return declarativeRule;
	}

	public void updateDevice(final Device<?> switchInput) {
		ruleRegistry.updateDevice(switchInput);
	}
}
