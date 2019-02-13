package nl.gingerbeard.automation.domoticz.transmitter.urlcreator.devicetypes;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import nl.gingerbeard.automation.devices.Shutters;
import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration;
import nl.gingerbeard.automation.domoticz.transmitter.urlcreator.URLBuilder;
import nl.gingerbeard.automation.state.NextState;
import nl.gingerbeard.automation.state.OpenCloseState;

public class OpenCloseTypeTest {

	private static DomoticzConfiguration config;

	@BeforeAll
	public static void initConfig() throws MalformedURLException {
		config = new DomoticzConfiguration(1, new URL("http://localhost"));
	}

	@Test
	public void open_urlCorrect() {
		final OpenCloseType type = new OpenCloseType();
		final URLBuilder builder = new URLBuilder(config);
		final NextState<OpenCloseState> nextState = new NextState<>(new Shutters(42), OpenCloseState.OPEN);

		type.createUrl(builder, nextState);
		final URL result = builder.build();

		assertEquals("http://localhost/json.htm?type=command&param=switchlight&idx=42&switchcmd=On&level=0", result.toString());
	}

	@Test
	public void close_urlCorrect() {
		final OpenCloseType type = new OpenCloseType();
		final URLBuilder builder = new URLBuilder(config);
		final NextState<OpenCloseState> nextState = new NextState<>(new Shutters(42), OpenCloseState.CLOSE);

		type.createUrl(builder, nextState);
		final URL result = builder.build();

		assertEquals("http://localhost/json.htm?type=command&param=switchlight&idx=42&switchcmd=Off&level=0", result.toString());
	}
}
