package nl.gingerbeard.automation.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import nl.gingerbeard.automation.state.Temperature.Unit;

public class TemperatureTest {

	private static final double ACCURACY = 0.00001;

	@Test
	public void temperature_fromCelsius() {
		final Temperature temperature = new Temperature(20.1, Unit.CELSIUS);

		assertEquals(20.1, temperature.get(Unit.CELSIUS), ACCURACY);
		assertEquals(68.18, temperature.get(Unit.FAHRENHEIT), ACCURACY);
		assertEquals(293.25, temperature.get(Unit.KELVIN), ACCURACY);
	}

	@Test
	public void temperature_fromFahrenheit() {
		final Temperature temperature = new Temperature(42.42, Unit.FAHRENHEIT);

		assertEquals(5.78889, temperature.get(Unit.CELSIUS), ACCURACY);
		assertEquals(42.42, temperature.get(Unit.FAHRENHEIT), ACCURACY);
		assertEquals(278.93889, temperature.get(Unit.KELVIN), ACCURACY);
	}

	@Test
	public void temperature_fromKelvin() {
		final Temperature temperature = new Temperature(66.6, Unit.KELVIN);

		assertEquals(-206.55, temperature.get(Unit.CELSIUS), ACCURACY);
		assertEquals(-339.79, temperature.get(Unit.FAHRENHEIT), ACCURACY);
		assertEquals(66.6, temperature.get(Unit.KELVIN), ACCURACY);
	}

	@Test
	public void getNull_throwsException() {
		final Temperature temperature = new Temperature(66.6, Unit.KELVIN);
		try {
			temperature.get(null);
			fail("Expected exception");
		} catch (final UnsupportedOperationException e) {
			assertEquals("Unit null unknown", e.getMessage());
		}
	}

	@Test
	public void builderCelcius_returnsValue() {
		final Temperature temperature = Temperature.celcius(15);

		assertEquals(15, temperature.get(Unit.CELSIUS));
	}

	@Test
	public void builderFahrenheit_returnsValue() {
		final Temperature temperature = Temperature.fahrenheit(15);

		assertEquals(15, temperature.get(Unit.FAHRENHEIT));
	}

	@Test
	public void builderKelvin_returnsValue() {
		final Temperature temperature = Temperature.kelvin(15);

		assertEquals(15, temperature.get(Unit.KELVIN));
	}

	@Test
	public void temperature_toString() {
		assertEquals("Temperature [value=1.0, unit=CELSIUS]", Temperature.celcius(1).toString());
		assertEquals("Temperature [value=1.0, unit=KELVIN]", Temperature.kelvin(1).toString());
		assertEquals("Temperature [value=1.0, unit=FAHRENHEIT]", Temperature.fahrenheit(1).toString());
	}
}
