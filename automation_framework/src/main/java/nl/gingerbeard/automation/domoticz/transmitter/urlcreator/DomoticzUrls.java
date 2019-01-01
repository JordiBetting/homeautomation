package nl.gingerbeard.automation.domoticz.transmitter.urlcreator;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.Set;

import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration;
import nl.gingerbeard.automation.state.NextState;
import nl.gingerbeard.automation.util.ChainOfResponsibility;
import nl.gingerbeard.automation.util.ReflectionUtil;

public final class DomoticzUrls {

	@SuppressWarnings("rawtypes")
	private final ChainOfResponsibility<Parameter, URL> chainOfCommand;
	private final DomoticzConfiguration configuration;

	public DomoticzUrls(final DomoticzConfiguration configuration) {
		this.configuration = configuration;
		chainOfCommand = createChainOfCommand();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private ChainOfResponsibility<Parameter, URL> createChainOfCommand() {
		final String deviceTypesPackageName = "nl.gingerbeard.automation.domoticz.transmitter.urlcreator.devicetypes";
		final Set<ChainOfCommandType> types = ReflectionUtil.createInstancesBySubtype(deviceTypesPackageName, ChainOfCommandType.class);
		final ChainOfResponsibility.Builder<Parameter, URL> builder = ChainOfResponsibility.builder();
		types.stream().forEach((type) -> builder.add(type));
		return builder.build();
	}

	public <T> URL getUrl(final NextState<T> nextState) throws MalformedURLException {
		final Parameter<T> item = new Parameter<>(configuration, nextState);
		final Optional<URL> execute = chainOfCommand.execute(item);
		return execute.orElseThrow(//
				() -> new MalformedURLException("Cannot construct url from unsupported state: " + nextState.get().getClass()));
	}

}
