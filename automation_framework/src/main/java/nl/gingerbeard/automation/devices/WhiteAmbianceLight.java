package nl.gingerbeard.automation.devices;

import nl.gingerbeard.automation.state.Color;

public class WhiteAmbianceLight extends Device<Color> {

	// Hue documentation: cold 6500K to a warm 2700K

	// Domoticz API:
	// /json.htm?type=command&param=setkelvinlevel&idx=99&kelvin=1 
	//	Example: color={"m":3,"t":0,"r":0,"g":0,"b":50,"cw":0,"ww":0}
	//
	// If field is not valid, value shall be 0
	// 
	//	Range of kelvin parameter: 0..100, 0 is coldest, 100 is warmest
	//	ColorMode {
	//		ColorModeNone = 0,   // Illegal
	//		ColorModeWhite = 1,  // White. Valid fields: none
	//		ColorModeTemp = 2,   // White with color temperature. Valid fields: t
	//		ColorModeRGB = 3,    // Color. Valid fields: r, g, b.
	//		ColorModeCustom = 4, // Custom (color + white). Valid fields: r, g, b, cw, ww, depending on device capabilities
	//		ColorModeLast = ColorModeCustom,
	//	};
	//
	//	Color {
	//		ColorMode m;
	//		uint8_t t;     // Range:0..255, Color temperature (warm / cold ratio, 0 is coldest, 255 is warmest)
	//		uint8_t r;     // Range:0..255, Red level
	//		uint8_t g;     // Range:0..255, Green level
	//		uint8_t b;     // Range:0..255, Blue level
	//		uint8_t cw;    // Range:0..255, Cold white level
	//		uint8_t ww;    // Range:0..255, Warm white level (also used as level for monochrome white)
	//	}


	public WhiteAmbianceLight(final int idx) {
		super(idx);
	}

	@Override
	public boolean updateState(final String newState) {
		// TODO Auto-generated method stub
		return false;
	}

}
