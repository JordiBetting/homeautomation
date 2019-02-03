package nl.gingerbeard.automation.declarative;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nl.gingerbeard.automation.devices.Switch;
import nl.gingerbeard.automation.logging.LogLevel;
import nl.gingerbeard.automation.logging.TestLogger;
import nl.gingerbeard.automation.state.OnOffState;

public class DeclarativeRulesTest {

	private DeclarativeRules rules;
	private TestLogger log;

	@BeforeEach
	public void createRules() {
		log = new TestLogger();
		rules = new DeclarativeRules(log);
	}

	// TODO: Output of Rules shall not be an updated device, it shall be a nextState

	@Test
	public void whenthen_single() {
		final Switch switchInput = new Switch(1);
		final Switch switchOutput = new Switch(2);

		rules.when(switchInput, OnOffState.ON).then(switchOutput, OnOffState.ON);

		switchInput.setState(OnOffState.ON);
		rules.deviceUpdated(switchInput);
		assertEquals(OnOffState.ON, switchOutput.getState());
	}

	@Test
	public void whenthen_2conditions() {
		final Switch switchInput = new Switch(1);
		final Switch switchOutput = new Switch(2);

		rules.when(switchInput, OnOffState.ON).then(switchOutput, OnOffState.ON);
		rules.when(switchInput, OnOffState.OFF).then(switchOutput, OnOffState.OFF);

		switchInput.setState(OnOffState.ON);
		rules.deviceUpdated(switchInput);
		assertEquals(OnOffState.ON, switchOutput.getState());

		switchInput.setState(OnOffState.OFF);
		rules.deviceUpdated(switchInput);
		assertEquals(OnOffState.OFF, switchOutput.getState());
	}

	@Test
	public void when_withoutThen_warningLogged() {

		final Switch switchInput = new Switch(1);
		switchInput.setState(OnOffState.ON);

		rules.when(switchInput, OnOffState.ON);
		rules.deviceUpdated(switchInput);

		log.assertContains(LogLevel.WARNING, "No action defined for idx=1, state=ON");
	}

}
