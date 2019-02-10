package nl.gingerbeard.automation.domoticz.receiver;

import java.util.Optional;

final class ResponseParameters {

	static enum ResponseParametersType {
		DEVICE, //
		TIME, //
		ALARM, //
		;
	}

	private final ResponseParametersType type;
	private final Optional<ResponseDeviceParameters> deviceParams;
	private final Optional<ResponseTimeParameters> timeParams;
	private final Optional<ResponseAlarmParameters> alarmParams;

	static ResponseParameters ofDevice(final int idx, final String state) {
		return new ResponseParameters(new ResponseDeviceParameters(idx, state));
	}

	static ResponseParameters ofTime(final int curtime, final int sunrise, final int sunset) {
		return new ResponseParameters(new ResponseTimeParameters(curtime, sunrise, sunset));
	}

	static ResponseParameters ofAlarm(final String newState) {
		return new ResponseParameters(new ResponseAlarmParameters(newState));
	}

	private ResponseParameters(final ResponseDeviceParameters deviceParams) {
		type = ResponseParametersType.DEVICE;
		timeParams = Optional.empty();
		this.deviceParams = Optional.of(deviceParams);
		alarmParams = Optional.empty();
	}

	private ResponseParameters(final ResponseTimeParameters timeParams) {
		type = ResponseParametersType.TIME;
		this.timeParams = Optional.of(timeParams);
		deviceParams = Optional.empty();
		alarmParams = Optional.empty();
	}

	private ResponseParameters(final ResponseAlarmParameters responseAlarmParameters) {
		type = ResponseParametersType.ALARM;
		timeParams = Optional.empty();
		deviceParams = Optional.empty();
		alarmParams = Optional.of(responseAlarmParameters);
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

	public Optional<ResponseAlarmParameters> getAlarmParametres() {
		return alarmParams;
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

	static class ResponseAlarmParameters {
		private final String alarmState;

		public ResponseAlarmParameters(final String alarmState) {
			this.alarmState = alarmState;
		}

		public String getAlarmState() {
			return alarmState;
		}

		@Override
		public String toString() {
			return "ResponseAlarmParameters [alarmState=" + alarmState + "]";
		}
	}
}