package com.saikali.android_skwissh.objects;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.saikali.android_skwissh.objects.SkwisshServerContent.SkwisshServerItem;

public class SkwisshSensorItem {

	private String id;
	private String displayName;
	private String graphTypeId;
	private String graphTypeName;
	private String labels;
	private SkwisshServerItem server;
	private ArrayList<SkwisshMeasureItem> measures = new ArrayList<SkwisshMeasureItem>();

	public SkwisshSensorItem(JSONObject json, SkwisshServerItem server) throws JSONException {
		this.id = Integer.toString(json.getInt("pk"));
		this.displayName = json.getJSONObject("fields").get("display_name").toString();
		this.graphTypeId = json.getJSONObject("fields").get("graph_type").toString();
		this.labels = json.getJSONObject("fields").get("probe_labels").toString();
		this.server = server;
	}

	public String getId() {
		return this.id;
	}

	public String getDisplayName() {
		return this.displayName;
	}

	public String getGraphTypeId() {
		return this.graphTypeId;
	}

	public void setGraphTypeName(String graphtype) {
		this.graphTypeName = graphtype;
	}

	public String getGraphTypeName() {
		return this.graphTypeName;
	}

	public void addMeasure(SkwisshMeasureItem measure) {
		this.measures.add(measure);
	}

	public int getMeasuresCount() {
		return this.measures.size();
	}

	public ArrayList<SkwisshMeasureItem> getMeasures() {
		return this.measures;
	}

	public SkwisshServerItem getServer() {
		return this.server;
	}

	public String getLabels() {
		return this.labels;
	}
}
