package nl.gingerbeard.automation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import nl.gingerbeard.automation.devices.IDevice;
import nl.gingerbeard.automation.state.NextState;

public class AutoControlTest {

	private static class TestAutoControl extends AutoControl {

		void triggerListener(final List<NextState<?>> updates) {
			super.updateActuators(updates);
		}

		@Override
		protected List<IDevice<?>> getDevices() {
			return null;
		}
	}

	@Test
	public void updateListener_listenerCalled() {
		final TestAutoControl control = new TestAutoControl();
		final AutoControlListener listener = mock(AutoControlListener.class);
		control.setListener(listener);

		control.triggerListener(new ArrayList<>());

		verify(listener, times(1)).outputChanged(any(), any());
	}

	@Test
	public void noListener_noException() {
		final TestAutoControl control = new TestAutoControl();

		assertDoesNotThrow(() -> //
		control.triggerListener(new ArrayList<>())//
		);
	}

	@Test
	public void updateListener_ownerIsTestClass() {
		final TestAutoControl control = new TestAutoControl();
		final AutoControlListener listener = mock(AutoControlListener.class);
		control.setListener(listener);

		control.triggerListener(new ArrayList<>());

		verify(listener, times(1)).outputChanged(eq(this.getClass().getSimpleName()), any());
	}

	@Test
	public void owner() {
		final TestAutoControl control = new TestAutoControl();
		assertEquals(getClass().getSimpleName(), control.getOwner());
	}
}
