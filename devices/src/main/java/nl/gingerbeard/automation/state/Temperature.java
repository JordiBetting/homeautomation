package nl.gingerbeard.automation.state;

import com.google.common.base.Preconditions;

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
		Preconditions.checkArgument(unit != null, "Unit shall be provided");
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + unit.hashCode();
		long temp;
		temp = Double.doubleToLongBits(value);
		result = prime * result + (int) (temp ^ temp >>> 32);
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
		if (!(obj instanceof Temperature)) {
			return false;
		}
		final Temperature other = (Temperature) obj;
		if (unit != other.unit) {
			return false;
		}
		final double otherC = other.unit.toCelcius(other.value);
		final double thisC = unit.toCelcius(value);

		if (Double.doubleToLongBits(thisC) != Double.doubleToLongBits(otherC)) {
			return false;
		}
		return true;
	}

}
