package nl.gingerbeard.automation.domoticz.clients.json;

import com.google.gson.annotations.SerializedName;

public class DeviceJSON {

	public static class DeviceResultJSON {
		@SerializedName("Data")
		public String data;
		
		@SerializedName("Status")
		public String status;
		
		@SerializedName("Type")
		public String type;
		
		@SerializedName("SubType")
		public String subtype;
		
		@SerializedName("Name")
		public String name;
		
		@SerializedName("idx")
		public int idx;
		
		@Override
		public String toString() {
			return String.format("%d (%s) Type: %s - %s : data=%s status=%s", idx, name, type, subtype, data, status);
		}
	}
	
	@SerializedName("status")
	public String status;

	@SerializedName("result")
	public DeviceResultJSON[] result;
}
