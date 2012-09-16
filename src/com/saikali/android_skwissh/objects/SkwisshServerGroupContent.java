package com.saikali.android_skwissh.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.saikali.android_skwissh.objects.SkwisshServerContent.SkwisshServerItem;

public class SkwisshServerGroupContent {

	public static class SkwisshServerGroupItem {

		private String id;
		private String name;
		private ArrayList<SkwisshServerItem> SERVERS = new ArrayList<SkwisshServerItem>();
		private Map<String, SkwisshServerItem> SERVERS_MAP = new HashMap<String, SkwisshServerItem>();

		public SkwisshServerGroupItem(JSONObject json) throws JSONException {
			this.id = Integer.toString(json.getInt("pk"));
			this.name = json.getJSONObject("fields").get("name").toString();
		}

		public SkwisshServerGroupItem() {
			this.id = "-1";
			this.name = "Uncategorized";
		}

		public void addServer(SkwisshServerItem sensor) {
			this.SERVERS.add(sensor);
			this.SERVERS_MAP.put(sensor.getId(), sensor);
		}

		public ArrayList<SkwisshServerItem> getServers() {
			return this.SERVERS;
		}

		public SkwisshServerItem getServer(String server_id) {
			return this.SERVERS_MAP.get(server_id);
		}

		public String getId() {
			return this.id;
		}

		public String getName() {
			return this.name;
		}
	}

	public static List<SkwisshServerGroupItem> ITEMS = new ArrayList<SkwisshServerGroupItem>();
	public static Map<String, SkwisshServerGroupItem> ITEM_MAP = new HashMap<String, SkwisshServerGroupItem>();

	public static void addItem(SkwisshServerGroupItem item) {
		ITEMS.add(item);
		ITEM_MAP.put(item.getId(), item);
	}
}
