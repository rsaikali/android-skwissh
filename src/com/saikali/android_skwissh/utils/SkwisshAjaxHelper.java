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
import com.turbomanage.httpclient.AbstractHttpClient;
import com.turbomanage.httpclient.BasicHttpClient;
import com.turbomanage.httpclient.HttpResponse;
import com.turbomanage.httpclient.ParameterMap;

public class SkwisshAjaxHelper {

	private String csrf_token;
	private String username;
	private String password;
	private String base_url;
	private SharedPreferences sharedPrefs;
	private BasicHttpClient httpclient;

	public SkwisshAjaxHelper(Context context) throws UnauthorizedException {
		this.sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		this.username = this.sharedPrefs.getString("skwissh_username", "test");
		this.password = this.sharedPrefs.getString("skwissh_password", "test");
		this.base_url = this.sharedPrefs.getString("skwissh_url", "http://skwissh.com/skwissh");
		Log.i(Constants.SKWISSH_TAG, this.base_url);
		this.httpclient = new BasicHttpClient();
		this.httpclient.addHeader("X-Requested-With", "XMLHttpRequest");
		this.skwisshLogin();
	}

	private void skwisshLogin() throws UnauthorizedException {
		this.httpclient.get(this.base_url + "/login/?next=/skwissh", null);
		List<HttpCookie> cookies = AbstractHttpClient.getCookieManager().getCookieStore().getCookies();
		for (int i = 0; i < cookies.size(); i++) {
			if (cookies.get(i).getName().equals("csrftoken")) {
				this.csrf_token = cookies.get(i).getValue();
				break;
			}
		}
		ParameterMap params = new ParameterMap();
		params.add("username", this.username).add("password", this.password);
		params.add("csrfmiddlewaretoken", this.csrf_token);
		HttpResponse response = this.httpclient.post(this.base_url + "/login/?next=/skwissh", params);

		String url = response.getUrl();
		if (url.contains("/login/"))
			throw new UnauthorizedException("Invalid credentials.");
	}

	public JSONArray getJSONServers(String server_group_id) throws JSONException {
		try {
			HttpResponse response = this.httpclient.get(this.base_url + "/servers/" + server_group_id + "/", null);
			return new JSONArray(response.getBodyAsString());
		} catch (Exception e) {
			Log.e(Constants.SKWISSH_TAG, "getJSONServers", e);
			return null;
		}
	}

	public JSONArray getJSONServerGroups() throws JSONException {
		try {
			HttpResponse response = this.httpclient.get(this.base_url + "/server_groups/", null);
			return new JSONArray(response.getBodyAsString());
		} catch (Exception e) {
			Log.e(Constants.SKWISSH_TAG, "getJSONServerGroups", e);
			return null;
		}
	}

	public JSONArray getJSONGraphTypes() throws JSONException {
		try {
			HttpResponse response = this.httpclient.get(this.base_url + "/graphtypes/", null);
			return new JSONArray(response.getBodyAsString());
		} catch (Exception e) {
			Log.e(Constants.SKWISSH_TAG, "getJSONGraphTypes", e);
			return null;
		}
	}

	public JSONArray getJSONSensors(String server_id) throws JSONException {
		try {
			HttpResponse response = this.httpclient.get(this.base_url + "/sensors/" + server_id + "/", null);
			return new JSONArray(response.getBodyAsString());
		} catch (Exception e) {
			Log.e(Constants.SKWISSH_TAG, "getJSONSensors", e);
			return null;
		}
	}

	public JSONArray getJSONMeasures(SkwisshServerItem server, SkwisshSensorItem sensor, String period) {
		try {
			String params = server.getId() + "/" + sensor.getId() + "/" + period + "/";
			HttpResponse response = this.httpclient.get(this.base_url + "/mesures/" + params, null);
			return new JSONArray(response.getBodyAsString());
		} catch (Exception e) {
			Log.e(Constants.SKWISSH_TAG, "getJSONSensors", e);
			return null;
		}
	}

	public class UnauthorizedException extends Exception {

		private static final long serialVersionUID = 1L;

		public UnauthorizedException() {
			super();
		}

		public UnauthorizedException(String msg) {
			super(msg);
		}
	}
}