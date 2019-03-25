package nl.gingerbeard.automation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

public class AutoControlTest {

	@Test
	public void updateListener_listenerCalled() {
		final AutoControlExample control = new AutoControlExample();
		final AutoControlListener listener = mock(AutoControlListener.class);
		control.setListener(listener);

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
		control.setListener(listener);

		control.triggerListener(new ArrayList<>());

		verify(listener, times(1)).outputChanged(eq(this.getClass().getSimpleName()), any());
	}

	@Test
	public void owner() {
		final AutoControlExample control = new AutoControlExample();
		assertEquals(getClass().getSimpleName(), control.getOwner());
	}
}
