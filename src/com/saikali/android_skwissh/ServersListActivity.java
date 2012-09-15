package com.saikali.android_skwissh;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;

import com.saikali.android_skwissh.adapters.ServersAdapter;
import com.saikali.android_skwissh.loaders.ServersLoader;

public class ServersListActivity extends Activity {

	private ExpandableListView expandableList = null;
	private ServersAdapter adapter;
	private ServersLoader serversLoader;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_servers_list);

		expandableList = (ExpandableListView) findViewById(R.id.expandableListView_servers);

		adapter = new ServersAdapter(this);
		expandableList.setAdapter(adapter);

		serversLoader = new ServersLoader(adapter);
		serversLoader.execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_servers_list, menu);
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		serversLoader = new ServersLoader(adapter);
		serversLoader.execute();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_settings:
			startActivityForResult(new Intent(this, SettingsActivity.class), 0);
			return true;
		case R.id.menu_refresh:
			serversLoader = new ServersLoader(adapter);
			serversLoader.execute();
			return true;
		}
		return false;
	}
}
