package nl.gingerbeard.automation.domoticz.transmitter.urlcreator.devicetypes;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import nl.gingerbeard.automation.devices.WhiteAmbianceLight;
import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration;
import nl.gingerbeard.automation.domoticz.transmitter.urlcreator.URLBuilder;
import nl.gingerbeard.automation.state.Color;
import nl.gingerbeard.automation.state.NextState;

public class ColorTypeTest {

	@DisplayName("Kelvin to DomoticzValue conversion test")
	@ParameterizedTest(name = "{0}K should be {1}")
	@MethodSource("kelvin2DVtest_inputs")
	public void kelvin2DVtest(final int K, final int expected) {
		assertEquals(expected, ColorType.kelvin2T(K));
	}

	static Stream<Arguments> kelvin2DVtest_inputs() {
		return Stream.of(//
				Arguments.of(Color.KELVIN_MIN, 100), //
				Arguments.of(calcKelvinValue(0.9), 90), //
				Arguments.of(calcKelvinValue(0.8), 80), //
				Arguments.of(calcKelvinValue(0.7), 70), //
				Arguments.of(calcKelvinValue(0.6), 60), //
				Arguments.of(calcKelvinValue(0.5), 50), //
				Arguments.of(calcKelvinValue(0.4), 40), //
				Arguments.of(calcKelvinValue(0.3), 30), //
				Arguments.of(calcKelvinValue(0.2), 20), //
				Arguments.of(calcKelvinValue(0.1), 10), //
				Arguments.of(Color.KELVIN_MAX, 0) //
		);
	}

	private static final int calcKelvinValue(final double factor) {
		return (int) (Color.KELVIN_MIN + (Color.KELVIN_MAX - Color.KELVIN_MIN) * (1.0 - factor));
	}

	private static DomoticzConfiguration config;

	@BeforeAll
	public static void initConfig() throws MalformedURLException {
		config = new DomoticzConfiguration(1, new URL("http://localhost"));
	}

	@Test
	public void createUrl() throws MalformedURLException {
		final NextState<Color> nextState = new NextState<>(new WhiteAmbianceLight(0), Color.fromWhiteColorTemperature(Color.KELVIN_MAX, 50));
		final ColorType colorType = new ColorType();
		final URLBuilder builder = new URLBuilder(config);

		colorType.createUrl(builder, nextState);
		final URL result = builder.build();

		assertEquals("http://localhost/json.htm?type=command&idx=0&color={\"m\":2,\"t\":0,\"r\":0,\"g\":0,\"b\":0,\"cw\":0,\"ww\":0}&brightness=50", result.toString());
	}
}