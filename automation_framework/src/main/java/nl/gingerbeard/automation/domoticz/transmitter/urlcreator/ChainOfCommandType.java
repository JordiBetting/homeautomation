package nl.gingerbeard.automation.domoticz.transmitter.urlcreator;

import java.net.URL;

import nl.gingerbeard.automation.state.NextState;
import nl.gingerbeard.automation.util.ChainOfResponsibility;

public abstract class ChainOfCommandType<T> extends ChainOfResponsibility<Parameter<T>, URL> {

	private final Class<T> backingClass;

	protected ChainOfCommandType(final Class<T> backingClass) {
		this.backingClass = backingClass;
	}

	@Override
	protected final boolean matches(final Parameter<T> item) {
		return isStateType(item.nextState, backingClass);
	}

	private boolean isStateType(final NextState<?> nextState, final Class<?> testState) {
		return nextState.get().getClass().isAssignableFrom(testState);
	}

	@Override
	protected final URL doWork(final Parameter<T> param) {
		final URLBuilder builder = new URLBuilder(param.configuration);
		createUrl(builder, param.nextState);
		return builder.build();
	}

	protected abstract void createUrl(URLBuilder urlBuilder, NextState<T> nextState);
}
