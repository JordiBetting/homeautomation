package nl.gingerbeard.automation.domoticz.receiver;

import java.util.Optional;

final class ResponseParameters {

	static enum ResponseParametersType {
		DEVICE, //
		;
	}

	private final ResponseParametersType type;
	private final ResponseDeviceParameters deviceParams;

	private ResponseParameters(final ResponseDeviceParameters deviceParams) {
		type = ResponseParametersType.DEVICE;
		this.deviceParams = deviceParams;
	}

	static ResponseParameters ofDevice(final int idx, final String state) {
		return new ResponseParameters(new ResponseDeviceParameters(idx, state));
	}

	public ResponseParametersType getType() {
		return type;
	}

	public Optional<ResponseDeviceParameters> getDeviceParameters() {
		if (type == ResponseParametersType.DEVICE) {
			return Optional.of(deviceParams);
		}
		return Optional.empty();
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
	}
}