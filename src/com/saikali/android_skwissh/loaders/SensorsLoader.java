package com.saikali.android_skwissh.loaders;

import org.json.JSONArray;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.saikali.android_skwissh.adapters.SensorsAdapter;
import com.saikali.android_skwissh.objects.SkwisshGraphTypeContent;
import com.saikali.android_skwissh.objects.SkwisshGraphTypeContent.SkwisshGraphTypeItem;
import com.saikali.android_skwissh.objects.SkwisshMeasureItem;
import com.saikali.android_skwissh.objects.SkwisshSensorItem;
import com.saikali.android_skwissh.utils.Constants;
import com.saikali.android_skwissh.utils.SkwisshAjaxHelper;

public class SensorsLoader extends AsyncTask<String, String, Boolean> {

	private final SensorsAdapter adapter;
	private ProgressDialog dialog;

	public SensorsLoader(SensorsAdapter adapter) {
		this.adapter = adapter;
		this.dialog = new ProgressDialog(this.adapter.context);
	}

	protected void onPreExecute() {
		this.dialog.setMessage("Loading Skwissh sensors for server " + this.adapter.getServer().getHostname() + "...");
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

			adapter.getServer().clearSensors();
			SkwisshAjaxHelper saj = new SkwisshAjaxHelper(this.adapter.context);

			JSONArray jsonGraphTypes = saj.getJSONGraphTypes();
			for (int i = 0; i < jsonGraphTypes.length(); i++) {
				SkwisshGraphTypeContent.addItem(new SkwisshGraphTypeItem(jsonGraphTypes.getJSONObject(i)));
			}

			JSONArray jsonSensors = saj.getJSONSensors(adapter.getServer().getId());
			for (int j = 0; j < jsonSensors.length(); j++) {
				SkwisshSensorItem sensor = new SkwisshSensorItem(jsonSensors.getJSONObject(j), adapter.getServer());
				sensor.setGraphTypeName(SkwisshGraphTypeContent.ITEM_MAP.get(sensor.getGraphTypeId()).getName());
				adapter.getServer().addSensor(sensor);
			}

			for (int j = 0; j < adapter.getServer().getSensors().size(); j++) {
				SkwisshSensorItem sensor = adapter.getServer().getSensors().get(j);
				publishProgress("Loading Skwissh measures...\n" + adapter.getServer().getHostname() + "\n" + sensor.getDisplayName());
				JSONArray jsonMeasures = saj.getJSONMeasures(adapter.getServer(), sensor);
				for (int k = 0; k < jsonMeasures.length(); k++)
					sensor.addMeasure(new SkwisshMeasureItem(jsonMeasures.getJSONObject(k)));
			}

			saj.skwisshLogout();
			return true;
		} catch (Exception e) {
			Log.e(Constants.SKWISSH_TAG, "SensorsLoader", e);
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
			alertDialog.setMessage("An error occured while loading Skwissh sensors for server " + this.adapter.getServer().getHostname() + ".\nPlease check your Skwissh settings...");
			alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
				}
			});
			alertDialog.show();
		}
	}
}
