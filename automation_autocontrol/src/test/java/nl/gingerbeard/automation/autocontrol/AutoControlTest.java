package nl.gingerbeard.automation.autocontrol;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import nl.gingerbeard.automation.state.NextState;
import nl.gingerbeard.automation.state.OnOffState;

public class AutoControlTest {

	private static class TestAutoControl extends AutoControl {

		void triggerListener(final List<NextState<OnOffState>> updates) {
			super.updateActuators(updates);
		}
	}

	@Test
	public void updateListener_listenerCalled() {
		final TestAutoControl control = new TestAutoControl();
		final AutoControlListener listener = mock(AutoControlListener.class);
		control.setListener(listener);

		control.triggerListener(new ArrayList<>());

		verify(listener, times(1)).outputChanged(any());
	}

	@Test
	public void noListener_noException() {
		final TestAutoControl control = new TestAutoControl();
		assertDoesNotThrow(() -> //
		control.triggerListener(new ArrayList<>())//
		);
	}
}
