package nl.gingerbeard.automation.state;

import com.google.common.base.Preconditions;

public class Color {

	public static final int KELVIN_MAX = 6500;
	public static final int KELVIN_MIN = 2700;

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

		@Override
		public String toString() {
			return "RGBColor [r=" + r + ", g=" + g + ", b=" + b + "]";
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
		Preconditions.checkArgument(kelvin >= KELVIN_MIN && kelvin <= KELVIN_MAX, "Supported values: [2700-6500] kelvin. Provided: " + kelvin);
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

	@Override
	public String toString() {
		if (mode == ColorMode.RGB) {
			return "Color [mode=" + mode + ", brightness=" + brightness + " rgbColor=" + rgbColor + "]";
		}
		return "Color [mode=" + mode + ", brightness=" + brightness + ", whiteTemperature=" + whiteTemperature + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (brightness == null ? 0 : brightness.hashCode());
		result = prime * result + (mode == null ? 0 : mode.hashCode());
		result = prime * result + (rgbColor == null ? 0 : rgbColor.hashCode());
		result = prime * result + whiteTemperature;
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Color other = (Color) obj;
		if (brightness == null) {
			if (other.brightness != null) {
				return false;
			}
		} else if (!brightness.equals(other.brightness)) {
			return false;
		}
		if (mode != other.mode) {
			return false;
		}
		if (rgbColor == null) {
			if (other.rgbColor != null) {
				return false;
			}
		} else if (!rgbColor.equals(other.rgbColor)) {
			return false;
		}
		if (whiteTemperature != other.whiteTemperature) {
			return false;
		}
		return true;
	}

}
