package nl.gingerbeard.automation.domoticz.receiver;

import java.util.Optional;

final class ResponseParameters {

	static enum ResponseParametersType {
		DEVICE, //
		TIME, //
		;
	}

	private final ResponseParametersType type;
	private final Optional<ResponseDeviceParameters> deviceParams;
	private final Optional<ResponseTimeParameters> timeParams;

	static ResponseParameters ofDevice(final int idx, final String state) {
		return new ResponseParameters(new ResponseDeviceParameters(idx, state));
	}

	static ResponseParameters ofTime(final int curtime, final int sunrise, final int sunset) {
		return new ResponseParameters(new ResponseTimeParameters(curtime, sunrise, sunset));
	}

	private ResponseParameters(final ResponseDeviceParameters deviceParams) {
		type = ResponseParametersType.DEVICE;
		this.deviceParams = Optional.of(deviceParams);
		timeParams = Optional.empty();
	}

	private ResponseParameters(final ResponseTimeParameters timeParams) {
		type = ResponseParametersType.TIME;
		this.timeParams = Optional.of(timeParams);
		deviceParams = Optional.empty();
	}

	public ResponseParametersType getType() {
		return type;
	}

	public Optional<ResponseDeviceParameters> getDeviceParameters() {
		return deviceParams;
	}

	public Optional<ResponseTimeParameters> getTimeParameters() {
		return timeParams;
	}

	static class ResponseDeviceParameters {
		private final int idx;
		private final String state;

		ResponseDeviceParameters(final int idx, final String state) {
			this.idx = idx;
			this.state = state;
		}

		int getIdx() {
			return idx;
		}

		String getState() {
			return state;
		}

		@Override
		public String toString() {
			return "ResponseDeviceParameters [idx=" + idx + ", state=" + state + "]";
		}

	}

	static class ResponseTimeParameters {
		private final int currentTime;
		private final int sunriseTime;
		private final int sunsetTime;

		public ResponseTimeParameters(final int currentTime, final int sunriseTime, final int sunsetTime) {
			this.currentTime = currentTime;
			this.sunriseTime = sunriseTime;
			this.sunsetTime = sunsetTime;
		}

		public int getCurrentTime() {
			return currentTime;
		}

		public int getSunriseTime() {
			return sunriseTime;
		}

		public int getSunsetTime() {
			return sunsetTime;
		}

		@Override
		public String toString() {
			return "ResponseTimeParameters [currentTime=" + currentTime + ", sunriseTime=" + sunriseTime + ", sunsetTime=" + sunsetTime + "]";
		}

	}
}