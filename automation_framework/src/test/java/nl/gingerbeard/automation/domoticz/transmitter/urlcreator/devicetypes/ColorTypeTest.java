package nl.gingerbeard.automation.domoticz.transmitter.urlcreator.devicetypes;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import nl.gingerbeard.automation.state.Color;

public class ColorTypeTest {

	private static int calc(final double factor) {
		return (int) (Color.KELVIN_MIN + (Color.KELVIN_MAX - Color.KELVIN_MIN) * factor);
	}

	@DisplayName("Kelvin to DomoticzValue conversion test")
	@ParameterizedTest(name = "{0}K should be {1}")
	// @CsvSource({ "6500, 0", "2700, 100", calc(0.5) + ", 50" })
	@MethodSource("calc")
	public void kelvin2DVtest(final int K, final int expected) {
		assertEquals(expected, ColorType.kelvin2T(K));
	}

	static Stream<Arguments> calc() {
		return Stream.of(//
				Arguments.of(2700, 100), //
				Arguments.of(calc(0.1), 90), //
				Arguments.of(calc(0.2), 80), //
				Arguments.of(calc(0.3), 70), //
				Arguments.of(calc(0.4), 60), //
				Arguments.of(calc(0.5), 50), //
				Arguments.of(calc(0.6), 40), //
				Arguments.of(calc(0.7), 30), //
				Arguments.of(calc(0.8), 20), //
				Arguments.of(calc(0.9), 10), //
				Arguments.of(6500, 0) //
		);
	}

	// Hue documentation: cold 6500K to a warm 2700K
	// 0..100, 0 is coldest, 100 is warmest
}