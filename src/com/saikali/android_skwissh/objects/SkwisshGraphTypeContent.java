package com.saikali.android_skwissh.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

public class SkwisshGraphTypeContent {

	public static class SkwisshGraphTypeItem {

		private String id;
		private String name;

		public SkwisshGraphTypeItem(JSONObject json) throws JSONException {
			this.id = Integer.toString(json.getInt("pk"));
			this.name = json.getJSONObject("fields").get("name").toString();
		}

		public String getId() {
			return this.id;
		}

		public String getName() {
			return this.name;
		}
	}

	public static List<SkwisshGraphTypeItem> ITEMS = new ArrayList<SkwisshGraphTypeItem>();
	public static Map<String, SkwisshGraphTypeItem> ITEM_MAP = new HashMap<String, SkwisshGraphTypeItem>();

	public static void addItem(SkwisshGraphTypeItem item) {
		ITEMS.add(item);
		ITEM_MAP.put(item.getId(), item);
	}
}
