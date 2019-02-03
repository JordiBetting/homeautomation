package nl.gingerbeard.automation.declarative;

import nl.gingerbeard.automation.devices.Device;
import nl.gingerbeard.automation.devices.Switch;
import nl.gingerbeard.automation.logging.ILogger;

public class DeclarativeRules {

	private final DeclarativeRulesRegistry ruleRegistry;
	private final IDeviceUpdate output;

	public DeclarativeRules(final ILogger log, final IDeviceUpdate output) {
		this.output = output;
		ruleRegistry = new DeclarativeRulesRegistry(log);
	}

	public <T> DeclarativeRuleBuilder when(final Device<T> device, final T expectedState) {
		final DeclarativeRuleBuilder declarativeRuleBuilder = new DeclarativeRuleBuilder(ruleRegistry, device, output, expectedState);
		ruleRegistry.add(device, declarativeRuleBuilder);
		return declarativeRuleBuilder;
	}

	public void deviceUpdated(final Switch switchInput) {
		ruleRegistry.execute(switchInput);
	}
}
