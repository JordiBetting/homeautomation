package nl.gingerbeard.automation.domoticz.transmitter.urlcreator.devicetypes;

import nl.gingerbeard.automation.domoticz.transmitter.urlcreator.URLBuilder;
import nl.gingerbeard.automation.domoticz.transmitter.urlcreator.domoticzapi.Keys;
import nl.gingerbeard.automation.domoticz.transmitter.urlcreator.domoticzapi.Type;
import nl.gingerbeard.automation.state.Color;
import nl.gingerbeard.automation.state.Color.ColorMode;
import nl.gingerbeard.automation.state.NextState;

public class ColorType extends ChainOfCommandType<Color> {

	public ColorType() {
		super(Color.class);
	}

	@Override
	protected void createUrl(final URLBuilder builder, final NextState<Color> nextState) {
		builder.add(Keys.TYPE, Type.COMMAND);
		builder.addIdx(nextState);
		builder.add(Keys.COLOR, createColor(nextState.get()));
		builder.add(Keys.BRIGHTNESS, nextState.get().getBrightness().getLevel());
	}

	private String createColor(final Color color) {
		final int m = getM(color);
		final int t = getT(color);
		final int r = getR(color);
		final int g = getG(color);
		final int b = getB(color);
		final int cw = getCw(color);
		final int ww = getWw(color);
		return String.format("{\"m\":%d,\"t\":%d,\"r\":%d,\"g\":%d,\"b\":%d,\"cw\":%d,\"ww\":%d}", m, t, r, g, b, cw, ww);
	}

	private int getM(final Color color) {
		return color.getMode() == ColorMode.RGB ? 3 : 2;
	}

	private int getT(final Color color) {
		return color.getMode() == ColorMode.WhiteTemperature ? kelvin2T(color.getWhiteTemperature()) : 0;
	}

	static int kelvin2T(final int whiteTemperature) {
		final int rangeK = Color.KELVIN_MAX - Color.KELVIN_MIN;
		final int deltaK = rangeK / 100;
		return 100 - (whiteTemperature - Color.KELVIN_MIN) / deltaK;
	}

	private int getR(final Color color) {
		return color.getMode() == ColorMode.RGB ? color.getRgbColor().getR() : 0;
	}

	private int getG(final Color color) {
		return color.getMode() == ColorMode.RGB ? color.getRgbColor().getG() : 0;
	}

	private int getB(final Color color) {
		return color.getMode() == ColorMode.RGB ? color.getRgbColor().getB() : 0;
	}

	private int getCw(final Color color) {
		return 0;
	}

	private int getWw(final Color color) {
		return 0;
	}

}
