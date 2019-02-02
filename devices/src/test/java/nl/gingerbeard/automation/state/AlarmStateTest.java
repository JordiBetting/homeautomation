package nl.gingerbeard.automation.state;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class AlarmStateTest {

	@Test
	public void alarmStateMeets() {
		assertEquals(true, AlarmState.ARM_AWAY.meets(AlarmState.ARM_AWAY));
		assertEquals(false, AlarmState.ARM_AWAY.meets(AlarmState.ARM_HOME));
		assertEquals(true, AlarmState.ARM_AWAY.meets(AlarmState.ALWAYS));
		assertEquals(true, AlarmState.ARM_AWAY.meets(AlarmState.ARMED));
		assertEquals(false, AlarmState.ARM_AWAY.meets(AlarmState.DISARMED));

		assertEquals(false, AlarmState.ARM_HOME.meets(AlarmState.ARM_AWAY));
		assertEquals(true, AlarmState.ARM_HOME.meets(AlarmState.ARM_HOME));
		assertEquals(true, AlarmState.ARM_HOME.meets(AlarmState.ALWAYS));
		assertEquals(true, AlarmState.ARM_HOME.meets(AlarmState.ARMED));
		assertEquals(false, AlarmState.ARM_HOME.meets(AlarmState.DISARMED));

		assertEquals(true, AlarmState.ALWAYS.meets(AlarmState.ARM_AWAY));
		assertEquals(true, AlarmState.ALWAYS.meets(AlarmState.ARM_HOME));
		assertEquals(true, AlarmState.ALWAYS.meets(AlarmState.ALWAYS));
		assertEquals(true, AlarmState.ALWAYS.meets(AlarmState.ARMED));
		assertEquals(true, AlarmState.ALWAYS.meets(AlarmState.DISARMED));

		assertEquals(false, AlarmState.ARMED.meets(AlarmState.ARM_AWAY));
		assertEquals(false, AlarmState.ARMED.meets(AlarmState.ARM_HOME));
		assertEquals(true, AlarmState.ARMED.meets(AlarmState.ALWAYS));
		assertEquals(true, AlarmState.ARMED.meets(AlarmState.ARMED));
		assertEquals(false, AlarmState.ARMED.meets(AlarmState.DISARMED));

		assertEquals(false, AlarmState.DISARMED.meets(AlarmState.ARM_AWAY));
		assertEquals(false, AlarmState.DISARMED.meets(AlarmState.ARM_HOME));
		assertEquals(true, AlarmState.DISARMED.meets(AlarmState.ALWAYS));
		assertEquals(false, AlarmState.DISARMED.meets(AlarmState.ARMED));
		assertEquals(true, AlarmState.DISARMED.meets(AlarmState.DISARMED));
	}
}
