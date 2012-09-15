package com.saikali.android_skwissh.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.saikali.android_skwissh.objects.SkwisshServerGroupContent.SkwisshServerGroupItem;

public class SkwisshServerContent {

	public static class SkwisshServerItem {

		private String id;
		private String hostname;
		private boolean isAvailable;
		private SkwisshServerGroupItem server_group = null;
		private ArrayList<SkwisshSensorItem> SENSORS = new ArrayList<SkwisshSensorItem>();
		private Map<String, SkwisshSensorItem> SENSORS_MAP = new HashMap<String, SkwisshSensorItem>();

		public SkwisshServerItem(JSONObject json, SkwisshServerGroupItem server_group) throws JSONException {
			this.id = Integer.toString(json.getInt("pk"));
			this.server_group = server_group;
			this.hostname = json.getJSONObject("fields").get("hostname").toString();
			this.isAvailable = json.getJSONObject("fields").getBoolean("state");
		}

		public void addSensor(SkwisshSensorItem sensor) {
			this.SENSORS.add(sensor);
			this.SENSORS_MAP.put(sensor.getId(), sensor);
		}

		public void clearSensors() {
			this.SENSORS.clear();
			this.SENSORS_MAP.clear();
		}

		public String getId() {
			return this.id;
		}

		public String getHostname() {
			return this.hostname;
		}

		public Boolean isAvailable() {
			return this.isAvailable;
		}

		public ArrayList<SkwisshSensorItem> getSensors() {
			return this.SENSORS;
		}

		public SkwisshSensorItem getSensor(String sensor_id) {
			return this.SENSORS_MAP.get(sensor_id);
		}

		public SkwisshServerGroupItem getServerGroup() {
			return this.server_group;
		}
	}

	public static List<SkwisshServerItem> ITEMS = new ArrayList<SkwisshServerItem>();
	public static Map<String, SkwisshServerItem> ITEM_MAP = new HashMap<String, SkwisshServerItem>();

	public static void addItem(SkwisshServerItem item) {
		ITEMS.add(item);
		ITEM_MAP.put(item.getId(), item);
	}
}
