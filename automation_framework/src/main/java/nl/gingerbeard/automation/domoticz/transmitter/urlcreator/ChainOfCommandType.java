package nl.gingerbeard.automation.domoticz.transmitter.urlcreator;

import java.net.MalformedURLException;
import java.net.URL;

import nl.gingerbeard.automation.state.NextState;
import nl.gingerbeard.automation.util.ChainOfCommand;

abstract class ChainOfCommandType<T> extends ChainOfCommand<Parameter<T>, URL> {

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
		try {
			return builder.build();
		} catch (final MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
	}

	protected abstract void createUrl(URLBuilder urlBuilder, NextState<T> nextState);
}
