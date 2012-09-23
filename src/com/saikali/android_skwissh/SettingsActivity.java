package com.saikali.android_skwissh;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity {

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
		this.addPreferencesFromResource(R.xml.preferences);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		this.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}
}
