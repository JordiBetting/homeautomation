package nl.gingerbeard.automation.declarative;

import nl.gingerbeard.automation.devices.Device;

public interface DeclarativeLogic {

	public static ConditionLogic when(final Condition condition) {

	}

	public static <T> Condition deviceHasState(final Device<T> device, final T state) {
		return null;
	}
}
