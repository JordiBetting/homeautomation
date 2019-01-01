package nl.gingerbeard.automation.state;

import com.google.common.base.Preconditions;

public class Color {

	public static enum ColorMode {
		WhiteTemperature, RGB
	}

	public static class RGBColor {
		private final int r, g, b;

		public RGBColor(final int r, final int g, final int b) {
			checkRGBValue(r, 'r');
			checkRGBValue(g, 'g');
			checkRGBValue(b, 'b');
			this.r = r;
			this.g = g;
			this.b = b;
		}

		private void checkRGBValue(final int value, final char colorChannel) {
			Preconditions.checkArgument(value >= 0 && value <= 255, "Supported colorchannel (" + colorChannel + ") value [0-255]. Provided: " + value);
		}

		public int getR() {
			return r;
		}

		public int getG() {
			return g;
		}

		public int getB() {
			return b;
		}
	}

	private final ColorMode mode;
	private final Level brightness;
	private int whiteTemperature;
	private RGBColor rgbColor;

	private Color(final ColorMode mode, final Level brightness) {
		this.mode = mode;
		this.brightness = brightness;
	}

	private Color(final int whiteTemperature, final Level brightness) {
		this(ColorMode.WhiteTemperature, brightness);
		this.whiteTemperature = whiteTemperature;
	}

	public Color(final RGBColor rgbColor, final Level brightness) {
		this(ColorMode.RGB, brightness);
		this.rgbColor = rgbColor;
	}

	public static Color fromRgb(final int r, final int g, final int b, final int brightness) {
		return new Color(new RGBColor(r, g, b), new Level(brightness));
	}

	public static Color fromWhiteColorTemperature(final int kelvin, final int brightness) {
		Preconditions.checkArgument(kelvin >= 2700 && kelvin <= 6500, "Supported values: [2700-6500] kelvin. Provided: " + kelvin);
		return new Color(kelvin, new Level(brightness));
	}

	public int getWhiteTemperature() {
		Preconditions.checkState(mode == ColorMode.WhiteTemperature, "White temperature is not available in mode " + mode);
		return whiteTemperature;
	}

	public RGBColor getRgbColor() {
		Preconditions.checkState(mode == ColorMode.RGB, "RGB color is not available in mode " + mode);
		return rgbColor;
	}

	public ColorMode getMode() {
		return mode;
	}

	public Level getBrightness() {
		return brightness;
	}

}
