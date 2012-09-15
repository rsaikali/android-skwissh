package com.saikali.android_skwissh.utils;

import java.net.HttpCookie;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.saikali.android_skwissh.objects.SkwisshSensorItem;
import com.saikali.android_skwissh.objects.SkwisshServerContent.SkwisshServerItem;
import com.turbomanage.httpclient.BasicHttpClient;
import com.turbomanage.httpclient.HttpResponse;
import com.turbomanage.httpclient.ParameterMap;

public class SkwisshAjaxHelper {

	private String csrf_token;
	private String username;
	private String password;
	private String base_url;
	private SharedPreferences sharedPrefs;

	public SkwisshAjaxHelper(Context context) {
		this.sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		this.username = this.sharedPrefs.getString("skwissh_username", "");
		this.password = this.sharedPrefs.getString("skwissh_password", "");
		this.base_url = this.sharedPrefs.getString("skwissh_url", "");
		this.skwisshLogin();
	}

	private void skwisshLogin() {
		BasicHttpClient httpclient = new BasicHttpClient(this.base_url);
		httpclient.get("/login/?next=/skwissh", null);
		List<HttpCookie> cookies = BasicHttpClient.getCookieManager().getCookieStore().getCookies();
		for (int i = 0; i < cookies.size(); i++) {
			if (cookies.get(i).getName().equals("csrftoken")) {
				this.csrf_token = cookies.get(i).getValue();
				break;
			}
		}
		ParameterMap params = new ParameterMap();
		params.add("username", this.username).add("password", this.password);
		params.add("csrfmiddlewaretoken", this.csrf_token);
		httpclient.post("/login/?next=/skwissh", params);
	}

	public void skwisshLogout() {
		BasicHttpClient httpclient = new BasicHttpClient(this.base_url);
		ParameterMap params = new ParameterMap();
		params.add("csrfmiddlewaretoken", this.csrf_token);
		httpclient.post("/logout/", params);
	}

	public JSONArray getJSONServers(String server_group_id) throws JSONException {
		try {
			BasicHttpClient httpclient = new BasicHttpClient(this.base_url);
			httpclient.addHeader("X-Requested-With", "XMLHttpRequest");
			HttpResponse response = httpclient.get("/servers/" + server_group_id + "/", null);
			return new JSONArray(response.getBodyAsString());
		} catch (Exception e) {
			Log.e(Constants.SKWISSH_TAG, "getJSONServers", e);
			return null;
		}
	}

	public JSONArray getJSONServerGroups() throws JSONException {
		try {
			BasicHttpClient httpclient = new BasicHttpClient(this.base_url);
			httpclient.addHeader("X-Requested-With", "XMLHttpRequest");
			HttpResponse response = httpclient.get("/server_groups/", null);
			return new JSONArray(response.getBodyAsString());
		} catch (Exception e) {
			Log.e(Constants.SKWISSH_TAG, "getJSONServerGroups", e);
			return null;
		}
	}

	public JSONArray getJSONGraphTypes() throws JSONException {
		try {
			BasicHttpClient httpclient = new BasicHttpClient(this.base_url);
			httpclient.addHeader("X-Requested-With", "XMLHttpRequest");
			HttpResponse response = httpclient.get("/graphtypes/", null);
			return new JSONArray(response.getBodyAsString());
		} catch (Exception e) {
			Log.e(Constants.SKWISSH_TAG, "getJSONGraphTypes", e);
			return null;
		}
	}

	public JSONArray getJSONSensors(String server_id) throws JSONException {
		try {
			BasicHttpClient httpclient = new BasicHttpClient(this.base_url);
			httpclient.addHeader("X-Requested-With", "XMLHttpRequest");
			HttpResponse response = httpclient.get("/sensors/" + server_id + "/", null);
			return new JSONArray(response.getBodyAsString());
		} catch (Exception e) {
			Log.e(Constants.SKWISSH_TAG, "getJSONSensors", e);
			return null;
		}
	}

	public JSONArray getJSONMeasures(SkwisshServerItem server, SkwisshSensorItem sensor) {
		String period = this.sharedPrefs.getString("default_period", "day");
		try {
			BasicHttpClient httpclient = new BasicHttpClient(this.base_url);
			httpclient.addHeader("X-Requested-With", "XMLHttpRequest");
			String params = server.getId() + "/" + sensor.getId() + "/" + period + "/";
			HttpResponse response = httpclient.get("/mesures/" + params, null);
			return new JSONArray(response.getBodyAsString());
		} catch (Exception e) {
			Log.e(Constants.SKWISSH_TAG, "getJSONSensors", e);
			return null;
		}
	}
}