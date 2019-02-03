package nl.gingerbeard.automation.declarative;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.gingerbeard.automation.devices.Device;
import nl.gingerbeard.automation.logging.ILogger;

public final class DeclarativeRulesRegistry {

	private final Map<Device<?>, List<DeclarativeRuleBuilder>> deviceRules = new HashMap<>();
	private final ILogger log;

	public DeclarativeRulesRegistry(final ILogger log) {
		this.log = log;
	}

	void add(final Device<?> device, final DeclarativeRuleBuilder ruleBuilder) {
		final List<DeclarativeRuleBuilder> rule = getOrCreateDeviceRuleList(device);
		rule.add(ruleBuilder);
	}

	private List<DeclarativeRuleBuilder> getOrCreateDeviceRuleList(final Device<?> device) {
		List<DeclarativeRuleBuilder> rule = deviceRules.get(device);
		if (rule == null) {
			rule = new ArrayList<>();
			deviceRules.put(device, rule);
		}
		return rule;
	}

	public void execute(final Device<?> device) {
		final List<DeclarativeRuleBuilder> rules = deviceRules.get(device);
		if (rules != null) {
			executeDeviceRules(device, rules);
		}
	}

	private void executeDeviceRules(final Device<?> device, final List<DeclarativeRuleBuilder> rules) {
		final Object nextState = device.getState();
		rules.stream().forEach((rule) -> {
			if (rule.hasActions()) {
				rule.execute(nextState);
			} else {
				log.warning("No action defined for idx=" + device.getIdx() + ", state=" + nextState);
			}
		});
	}
}
