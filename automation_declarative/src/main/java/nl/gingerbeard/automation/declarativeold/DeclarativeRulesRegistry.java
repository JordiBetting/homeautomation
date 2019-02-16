package nl.gingerbeard.automation.declarativeold;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.gingerbeard.automation.devices.Device;
import nl.gingerbeard.automation.logging.ILogger;

final class DeclarativeRulesRegistry {

	private final Map<Device<?>, List<DeclarativeRule>> deviceRules = new HashMap<>();
	private final ILogger log;

	DeclarativeRulesRegistry(final ILogger log) {
		this.log = log;
	}

	void add(final Device<?> device, final DeclarativeRule ruleBuilder) {
		final List<DeclarativeRule> rule = getOrCreateDeviceRuleList(device);
		rule.add(ruleBuilder);
	}

	private List<DeclarativeRule> getOrCreateDeviceRuleList(final Device<?> device) {
		List<DeclarativeRule> rule = deviceRules.get(device);
		if (rule == null) {
			rule = new ArrayList<>();
			deviceRules.put(device, rule);
		}
		return rule;
	}

	void updateDevice(final Device<?> device) {
		final List<DeclarativeRule> rules = deviceRules.get(device);
		if (rules != null) {
			updateDeviceRules(device, rules);
		}
	}

	private void updateDeviceRules(final Device<?> device, final List<DeclarativeRule> rules) {
		final Object nextState = device.getState();
		rules.stream().forEach((rule) -> updateDeviceRule(device, nextState, rule));
	}

	private void updateDeviceRule(final Device<?> device, final Object nextState, final DeclarativeRule rule) {
		if (rule.hasActions()) {
			rule.updateDevice(device, nextState);
		} else {
			log.warning("No action defined for idx=" + device.getIdx() + ", state=" + nextState);
		}
	}
}
