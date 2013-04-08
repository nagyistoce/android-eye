package com.example.androideye;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

/**
 * Verify if it is the first time the App is running.
 * @author Alan Zanoni Peixinho
 *
 */
public class StartupChoiceActivity extends Activity {
    
	public static final String TAG = "StartupChoice";
	private static boolean DEBUG = false;
	
	private static final int CHECK_CODE = 12;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if(DEBUG){
	    	startActivity(new Intent(this, ExperimentRun.class));
	    	//startActivity(new Intent(this, Tutorial.class));
        	finish();
	    	return;
        }
        
        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, CHECK_CODE);

    }
    
    private void callStartActivity()
    {
        SharedPreferences mPrefs = getSharedPreferences(Globals.PersistentInfo.PREFERENCES_FILE, Context.MODE_PRIVATE);
        
        if(!mPrefs.getBoolean(Globals.PersistentInfo.HAS_STARTED, false)) {
            // Do what ever you want to do the first time the app is run
        	Log.v(TAG, "This is the first time the app is running.");

        	startActivity(new Intent(this, Tutorial.class));
            finish();
            //Do what ever we want to do on a normal startup. This is pretty much always mean starting a new activity
        } else {
            Log.i(TAG, "We've already started the app at least once");
            

            //Do what ever we want to do on a normal startup. This is pretty much always mean starting a new activity
            startActivity(new Intent(this, UserInterface.class));
            finish();
        }
    }
    

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                callStartActivity();
            }
            else
            {
            	AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setMessage("There is an error in the Text To Speech Software.");
                alert.show();
                alert.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						finish();						
					}
				});
            }
        }
    }
        
}
