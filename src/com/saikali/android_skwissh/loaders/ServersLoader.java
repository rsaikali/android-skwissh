package com.saikali.android_skwissh.loaders;

import org.json.JSONArray;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.saikali.android_skwissh.adapters.ServersAdapter;
import com.saikali.android_skwissh.objects.SkwisshGraphTypeContent;
import com.saikali.android_skwissh.objects.SkwisshGraphTypeContent.SkwisshGraphTypeItem;
import com.saikali.android_skwissh.objects.SkwisshServerContent.SkwisshServerItem;
import com.saikali.android_skwissh.objects.SkwisshServerGroupContent;
import com.saikali.android_skwissh.objects.SkwisshServerGroupContent.SkwisshServerGroupItem;
import com.saikali.android_skwissh.utils.Constants;
import com.saikali.android_skwissh.utils.SkwisshAjaxHelper;

public class ServersLoader extends AsyncTask<String, String, Boolean> {

	private final ServersAdapter adapter;
	private ProgressDialog dialog;

	public ServersLoader(ServersAdapter adapter) {
		this.adapter = adapter;
		this.dialog = new ProgressDialog(this.adapter.context);
	}

	protected void onPreExecute() {
		this.dialog.setMessage("Loading Skwissh data...");
		this.dialog.show();
	}

	@Override
	protected void onProgressUpdate(String... values) {
		this.dialog.setMessage(values[0]);
		super.onProgressUpdate(values);
	}

	@Override
	protected Boolean doInBackground(String... params) {
		try {
			SkwisshServerGroupContent.ITEMS.clear();
			SkwisshServerGroupContent.ITEM_MAP.clear();

			SkwisshAjaxHelper saj = new SkwisshAjaxHelper(this.adapter.context);

			JSONArray jsonGraphTypes = saj.getJSONGraphTypes();
			for (int i = 0; i < jsonGraphTypes.length(); i++) {
				SkwisshGraphTypeContent.addItem(new SkwisshGraphTypeItem(jsonGraphTypes.getJSONObject(i)));
			}

			JSONArray jsonServerGroups = saj.getJSONServerGroups();
			for (int l = 0; l < jsonServerGroups.length(); l++) {
				SkwisshServerGroupItem server_group = new SkwisshServerGroupItem(jsonServerGroups.getJSONObject(l));

				JSONArray jsonServers = saj.getJSONServers(server_group.getId());
				for (int i = 0; i < jsonServers.length(); i++) {

					SkwisshServerItem server = new SkwisshServerItem(jsonServers.getJSONObject(i), server_group);
					publishProgress("Loading Skwissh servers...\n" + server_group.getName() + "\n" + server.getHostname());
					server_group.addServer(server);

					// JSONArray jsonSensors =
					// saj.getJSONSensors(server.getId());
					// for (int j = 0; j < jsonSensors.length(); j++) {
					// SkwisshSensorItem sensor = new
					// SkwisshSensorItem(jsonSensors.getJSONObject(j), server);
					// sensor.setGraphTypeName(SkwisshGraphTypeContent.ITEM_MAP.get(sensor.getGraphTypeId()).getName());
					// server.addSensor(sensor);
					// }
					//
					// for (int j = 0; j < server.getSensors().size(); j++) {
					// SkwisshSensorItem sensor = server.getSensors().get(j);
					// publishProgress("Loading Skwissh measures...\n" +
					// server.getHostname() + "\n" + sensor.getDisplayName());
					// JSONArray jsonMeasures = saj.getJSONMeasures(server,
					// sensor);
					// for (int k = 0; k < jsonMeasures.length(); k++)
					// sensor.addMeasure(new
					// SkwisshMeasureItem(jsonMeasures.getJSONObject(k)));
					// }

				}
				if (server_group.getServers().size() != 0)
					SkwisshServerGroupContent.addItem(server_group);
			}

			SkwisshServerGroupItem server_group = new SkwisshServerGroupItem();
			JSONArray jsonServers = saj.getJSONServers("999999");
			for (int i = 0; i < jsonServers.length(); i++) {
				SkwisshServerItem server = new SkwisshServerItem(jsonServers.getJSONObject(i), server_group);
				publishProgress("Loading Skwissh servers...\n" + server_group.getName() + "\n" + server.getHostname());
				server_group.addServer(server);
			}
			if (server_group.getServers().size() != 0)
				SkwisshServerGroupContent.addItem(server_group);

			saj.skwisshLogout();
			return true;
		} catch (Exception e) {
			Log.e(Constants.SKWISSH_TAG, "ServersLoader", e);
			return false;
		}
	}

	@Override
	protected void onPostExecute(Boolean success) {
		if (this.dialog.isShowing())
			this.dialog.dismiss();

		if (success) {
			Toast.makeText(this.adapter.context, "Skwissh data loaded successfully", Toast.LENGTH_LONG).show();
			this.adapter.updateEntries();
		} else {
			AlertDialog alertDialog = new AlertDialog.Builder(this.adapter.context).create();
			alertDialog.setTitle("Error");
			alertDialog.setMessage("An error occured while loading Skwissh data.\nPlease check your Skwissh settings...");
			alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
				}
			});
			alertDialog.show();
		}
	}
}
