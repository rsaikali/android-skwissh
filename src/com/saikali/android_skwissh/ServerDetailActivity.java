package com.saikali.android_skwissh;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;

import com.saikali.android_skwissh.adapters.SensorsAdapter;
import com.saikali.android_skwissh.loaders.SensorsLoader;
import com.saikali.android_skwissh.objects.SkwisshServerContent.SkwisshServerItem;
import com.saikali.android_skwissh.objects.SkwisshServerGroupContent;

public class ServerDetailActivity extends Activity {

	private ExpandableListView expandableList = null;
	private SensorsAdapter adapter;
	private SensorsLoader sensorsLoader;
	private SkwisshServerItem server;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_serverdetail_list);
		expandableList = (ExpandableListView) findViewById(R.id.expandableListView_sensors);

		String server_id = getIntent().getExtras().getString("server_id");
		String group_id = getIntent().getExtras().getString("group_id");
		this.server = SkwisshServerGroupContent.ITEM_MAP.get(group_id).getServer(server_id);

		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
		String period = sharedPrefs.getString("default_period", "day");
		this.setTitle(this.server.getHostname() + " activity on last " + period + ".");

		adapter = new SensorsAdapter(this, this.server);
		expandableList.setAdapter(adapter);

		sensorsLoader = new SensorsLoader(adapter);
		sensorsLoader.execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_servers_list, menu);
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
		String period = sharedPrefs.getString("default_period", "day");
		this.setTitle(this.server.getHostname() + " activity on last " + period + ".");

		sensorsLoader = new SensorsLoader(adapter);
		sensorsLoader.execute();

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_settings:
			startActivityForResult(new Intent(this, SettingsActivity.class), 0);
			return true;
		case R.id.menu_refresh:
			sensorsLoader = new SensorsLoader(adapter);
			sensorsLoader.execute();
			return true;
		}
		return false;
	}
}
