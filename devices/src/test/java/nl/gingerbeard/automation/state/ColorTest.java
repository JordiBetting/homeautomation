package nl.gingerbeard.automation.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashMap;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import nl.gingerbeard.automation.state.Color.ColorMode;

public class ColorTest {

	@Test
	public void createRGB_correctValuesReturned() {
		final Color rgbB = Color.fromRgb(1, 2, 3, 4);

		assertEquals(1, rgbB.getRgbColor().getR());
		assertEquals(2, rgbB.getRgbColor().getG());
		assertEquals(3, rgbB.getRgbColor().getB());
		assertEquals(4, rgbB.getBrightness().getLevel());
		assertEquals(ColorMode.RGB, rgbB.getMode());
	}

	@Test
	public void createWhiteTemperature_correctValuesReturned() {
		final Color wtB = Color.fromWhiteColorTemperature(4000, 2);

		assertEquals(4000, wtB.getWhiteTemperature());
		assertEquals(2, wtB.getBrightness().getLevel());
	}

	@Test
	public void getWhiteTemperature_rgbColor_throwsException() {
		final Color rgbB = Color.fromRgb(1, 2, 3, 4);

		try {
			rgbB.getWhiteTemperature();
			fail("Expected exception");
		} catch (final IllegalStateException e) {
			assertEquals("White temperature is not available in mode RGB", e.getMessage());
		}
	}

	@Test
	public void getRgb_whiteTemperature_throwsException() {
		final Color wtB = Color.fromWhiteColorTemperature(4000, 2);

		try {
			wtB.getRgbColor();
			fail("Expected exception");
		} catch (final IllegalStateException e) {
			assertEquals("RGB color is not available in mode WhiteTemperature", e.getMessage());
		}
	}

	@ParameterizedTest
	@ValueSource(chars = { 'r', 'g', 'b' })
	public void testRangeColorChannel(final char colorChannel) {
		final HashMap<Character, Integer> map = createRGBMap();

		// expect no exception
		createRGBColor(colorChannel, map, 0);

		// expect no exception
		createRGBColor(colorChannel, map, 255);

		try {
			createRGBColor(colorChannel, map, -1);
		} catch (final IllegalArgumentException e) {
			assertEquals("Supported colorchannel (" + colorChannel + ") value [0-255]. Provided: -1", e.getMessage());
		}

		try {
			createRGBColor(colorChannel, map, 256);
		} catch (final IllegalArgumentException e) {
			assertEquals("Supported colorchannel (" + colorChannel + ") value [0-255]. Provided: 256", e.getMessage());
		}

	}

	private void createRGBColor(final char colorChannel, final HashMap<Character, Integer> map, final int value) {
		map.put(colorChannel, value);
		Color.fromRgb(map.get('r'), map.get('g'), map.get('b'), 50); // expect no exception
	}

	private HashMap<Character, Integer> createRGBMap() {
		final HashMap<Character, Integer> map = new HashMap<>();
		map.put('r', 100);
		map.put('g', 100);
		map.put('b', 100);
		return map;
	}

	@Test
	public void createKelvinRange() {
		// expect no exception
		Color.fromWhiteColorTemperature(2700, 1);
		Color.fromWhiteColorTemperature(6500, 1);

		try {
			Color.fromWhiteColorTemperature(2699, 1);
			fail("expect exception");
		} catch (final IllegalArgumentException e) {
			assertEquals("Supported values: [2700-6500] kelvin. Provided: 2699", e.getMessage());
		}

		try {
			Color.fromWhiteColorTemperature(6501, 1);
			fail("expect exception");
		} catch (final IllegalArgumentException e) {
			assertEquals("Supported values: [2700-6500] kelvin. Provided: 6501", e.getMessage());
		}
	}

	@Test
	public void whiteColorTemperature_toString() {
		final Color color = Color.fromWhiteColorTemperature(3000, 50);

		assertEquals("Color [mode=WhiteTemperature, brightness=Level [level=50], whiteTemperature=3000]", color.toString());
	}

	@Test
	public void rgbColor_toString() {
		final Color color = Color.fromRgb(1, 2, 3, 4);

		assertEquals("Color [mode=RGB, brightness=Level [level=4] rgbColor=RGBColor [r=1, g=2, b=3]]", color.toString());
	}

}
