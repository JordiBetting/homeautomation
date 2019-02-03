package nl.gingerbeard.automation.declarative;

import nl.gingerbeard.automation.devices.Device;
import nl.gingerbeard.automation.devices.Switch;
import nl.gingerbeard.automation.logging.ILogger;

public class DeclarativeRules {

	private final DeclarativeRulesRegistry rules;
	private final IDeviceUpdate output;

	public DeclarativeRules(final ILogger log, final IDeviceUpdate output) {
		this.output = output;
		rules = new DeclarativeRulesRegistry(log);
	}

	public <T> DeclarativeRuleBuilder when(final Device<T> device, final T expectedState) {
		final DeclarativeRuleBuilder declarativeRuleBuilder = new DeclarativeRuleBuilder(output, expectedState);
		rules.add(device, declarativeRuleBuilder);
		return declarativeRuleBuilder;
	}

	public void deviceUpdated(final Switch switchInput) {
		rules.execute(switchInput);
	}
}
