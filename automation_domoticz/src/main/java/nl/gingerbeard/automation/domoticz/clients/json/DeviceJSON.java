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
		
		@SerializedName("Level")
		public String level;
		
		@SerializedName("LevelInt")
		public String levelInt;
		
		@Override
		public String toString() {
			return String.format("%d, %s, %s, %s, %s, %s, %s, %s", idx, name, type, subtype, data, status, level, levelInt);
		}
	}
	
	@SerializedName("status")
	public String status;

	@SerializedName("result")
	public DeviceResultJSON[] result;
}
