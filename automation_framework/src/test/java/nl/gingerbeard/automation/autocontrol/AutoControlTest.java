package nl.gingerbeard.automation.autocontrol;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import nl.gingerbeard.automation.logging.TestLogger;
import nl.gingerbeard.automation.state.IState;
import nl.gingerbeard.automation.state.State;

public class AutoControlTest {

	@Test
	public void updateListener_listenerCalled() {
		final AutoControlExample control = new AutoControlExample();
		final AutoControlListener listener = mock(AutoControlListener.class);
		control.init(listener, new State(), new TestLogger());

		control.triggerListener(new ArrayList<>());

		verify(listener, times(1)).outputChanged(any(), any());
	}

	@Test
	public void noListener_noException() {
		final AutoControlExample control = new AutoControlExample();

		assertDoesNotThrow(() -> //
		control.triggerListener(new ArrayList<>())//
		);
	}

	@Test
	public void updateListener_ownerIsTestClass() {
		final AutoControlExample control = new AutoControlExample();
		final AutoControlListener listener = mock(AutoControlListener.class);
		control.init(listener, new State(), new TestLogger());

		control.triggerListener(new ArrayList<>());

		verify(listener, times(1)).outputChanged(eq(this.getClass().getSimpleName()), any());
	}

	@Test
	public void owner() {
		final AutoControlExample control = new AutoControlExample();
		assertEquals(getClass().getSimpleName(), control.getOwner());
	}
	
	@Test
	public void getState() {
		IState state = new State();
		final AutoControlExample control = new AutoControlExample();
		
		control.init(null, state, new TestLogger());
		
		assertEquals(state, control.getState());
	}
}
