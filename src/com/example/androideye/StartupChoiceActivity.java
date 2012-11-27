package com.example.androideye;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

/**
 * Verify if it is the first time the App is running.
 * @author Alan Zanoni Peixinho
 *
 */
public class StartupChoiceActivity extends Activity {
    
	private static final String TAG = "StartupChoice";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences mPrefs = getSharedPreferences("prefs", Context.MODE_PRIVATE);
        
        if(!mPrefs.getBoolean("has_started_before", false)) {
            // Do what ever you want to do the first time the app is run
        	Log.v(TAG, "This is the first time the app is running.");
            mPrefs.edit().putBoolean("has_started_before", true).commit();

        	startActivity(new Intent(this, Tutorial.class));
            finish();
            //Do what ever we want to do on a normal startup. This is pretty much always mean starting a new activity
        } else {
            //Remember our choice for next time
            mPrefs.edit().putBoolean("has_started_before", true).commit();
            Log.i(TAG, "We've already started the app at least once");
            

            //Do what ever we want to do on a normal startup. This is pretty much always mean starting a new activity
            startActivity(new Intent(this, UserInterface.class));
            finish();
        }

    }
    
        
}
