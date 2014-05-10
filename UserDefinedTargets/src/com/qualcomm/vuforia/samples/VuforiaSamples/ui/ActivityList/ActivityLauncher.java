/*==============================================================================
 Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc.
 All Rights Reserved.
 ==============================================================================*/

package com.qualcomm.vuforia.samples.VuforiaSamples.ui.ActivityList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.qualcomm.vuforia.samples.VuforiaSamples.R;

// This activity starts activities which demonstrate the Vuforia features
public class ActivityLauncher extends Activity {

	private String mActivities[] = { "Sketch 3D Demo" };

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.activities_list);

		Intent intent = new Intent(this, AboutScreen.class);
		intent.putExtra("ABOUT_TEXT_TITLE", mActivities[0]);

		intent.putExtra("ACTIVITY_TO_LAUNCH",
				"app.UserDefinedTargets.UserDefinedTargets");
		intent.putExtra("ABOUT_TEXT", "UserDefinedTargets/UD_about.html");

		startActivity(intent);

	}

}
