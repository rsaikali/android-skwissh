package com.saikali.android_skwissh.objects;

import java.text.ParseException;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.saikali.android_skwissh.utils.Constants;
import com.saikali.android_skwissh.utils.ISO8601DateParser;

public class SkwisshMeasureItem {

	private String id;
	private String server_id;
	private String sensor_id;
	private String value;
	private Date timestamp;

	public SkwisshMeasureItem(JSONObject json) {
		try {
			this.id = Integer.toString(json.getInt("pk"));

			this.server_id = json.getJSONObject("fields").getString("server");

			this.sensor_id = json.getJSONObject("fields").getString("probe");
			this.value = json.getJSONObject("fields").getString("value");
			this.timestamp = ISO8601DateParser.parse(json.getJSONObject("fields").getString("timestamp"));
		} catch (JSONException e) {
			Log.e(Constants.SKWISSH_TAG, "Error loading JSON measure " + this.id, e);
		} catch (ParseException e) {
			Log.e(Constants.SKWISSH_TAG, "Error loading Date measure " + this.id, e);
		}
	}

	public String getId() {
		return this.id;
	}

	public String getServerId() {
		return this.server_id;
	}

	public String getSensorId() {
		return this.sensor_id;
	}

	public String getValue() {
		return this.value;
	}

	public Date getTimestamp() {
		return this.timestamp;
	}
}