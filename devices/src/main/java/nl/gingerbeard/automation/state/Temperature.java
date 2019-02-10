package nl.gingerbeard.automation.state;

public final class Temperature {

	public static enum Unit {
		CELSIUS, //
		FAHRENHEIT, //
		KELVIN, //
		;

		private static final int C2F_OFFSET = 32;
		private static final double C2F_FACTOR = 9.0 / 5.0;
		private static final double C2K_OFFSET = 273.15;
		private static final double F2C_FACTOR = 5.0 / 9.0;

		double toCelcius(final double value) {
			double result = value;
			switch (this) {
			case FAHRENHEIT:
				result = (value - C2F_OFFSET) * F2C_FACTOR;
				break;
			case KELVIN:
				result = value - C2K_OFFSET;
				break;
			case CELSIUS:
				break;
			}
			return result;
		}

		double toFahrenheit(final double value) {
			double result = value;
			switch (this) {
			case CELSIUS:
				result = value * C2F_FACTOR + C2F_OFFSET;
				break;
			case KELVIN:
				result = (value - C2K_OFFSET) * C2F_FACTOR + C2F_OFFSET;
				break;
			case FAHRENHEIT:
				break;
			}
			return result;
		}

		double toKelvin(final double value) {
			double result = value;
			switch (this) {
			case CELSIUS:
				result = value + C2K_OFFSET;
				break;
			case FAHRENHEIT:
				result = (value - C2F_OFFSET) * F2C_FACTOR + C2K_OFFSET;
				break;
			case KELVIN:
				break;
			}
			return result;
		}
	}

	private final Unit unit;
	private final double value;

	public Temperature(final double temperature, final Unit unit) {
		value = temperature;
		this.unit = unit;
	}

	public static Temperature celcius(final double temperature) {
		return new Temperature(temperature, Unit.CELSIUS);
	}

	public static Temperature fahrenheit(final double temperature) {
		return new Temperature(temperature, Unit.FAHRENHEIT);
	}

	public static Temperature kelvin(final double temperature) {
		return new Temperature(temperature, Unit.KELVIN);
	}

	public double get(final Unit unit) {
		if (unit == Unit.CELSIUS) {
			return this.unit.toCelcius(value);
		} else if (unit == Unit.FAHRENHEIT) {
			return this.unit.toFahrenheit(value);
		} else if (unit == Unit.KELVIN) {
			return this.unit.toKelvin(value);
		} else {
			throw new UnsupportedOperationException("Unit " + unit + " unknown");
		}
	}

	@Override
	public String toString() {
		return "Temperature [value=" + value + ", unit=" + unit + "]";
	}

}
