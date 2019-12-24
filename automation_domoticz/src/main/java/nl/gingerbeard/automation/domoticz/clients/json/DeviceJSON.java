package nl.gingerbeard.automation.domoticz.clients.json;

import com.google.gson.annotations.SerializedName;

public class DeviceJSON {

	public static class DeviceResultJSON {
		@SerializedName("Status")
		public String status;
		
		@SerializedName("Name")
		public String name;
		
		@SerializedName("Level")
		public String level;
		
		@SerializedName("LevelInt")
		public int levelInt;
		
	}
	
	@SerializedName("status")
	public String status;

	@SerializedName("result")
	public DeviceResultJSON[] result;
}
