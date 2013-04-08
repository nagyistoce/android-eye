package com.example.androideye;

import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.widget.Toast;

/**
 * Wrapper to the Text to Speech Android tool
 * @author Alan Zanoni Peixinho
 *
 */
public class Speaker implements OnInitListener{
	
	private TextToSpeech tts;
	Context c = null;
	String fs = null;
	
	/**
	 * Initialize text to speech
	 * @param context Activity Context.
	 */
	private void initialize(Context context){
		c = context;
		
		Log.v("Speaker", "Creating Text To Speech");
        tts = new TextToSpeech(c, this);
        Log.v("Speaker", "TTS variable: " + tts);
	}
	/**
	 * Initialize text to speech
	 * @param context Activity Context.
	 * @param firstSpeak String spoken after text to speech initialization.
	 */
	public Speaker(Context context, String firstSpeak)
	{
		fs = new String(firstSpeak);
		initialize(context);
	}
	
	/**
	 * Initialize text to speech
	 * @param context context Activity Context.
	 */
	public Speaker(Context context)
	{
		initialize(context);
	}
	
	/**
	 * Count the number of words in a string.
	 * @param s String to be count.
	 * @return Return the number of words in the string.
	 */
	private int wordCount(String s){//fast words number estimate
		int sz = s.length();
		int count=1;
		for (int i = 0; i < sz; i++) {
			if(Character.isSpace(s.charAt(i)))
				count++;
		}
		return count;
	}
	
	/**
	 * Speak a string.
	 * @param text String to be spoken.
	 */
    public void speak(String text)
    {
    	Log.v("Speaker", "Saying: "+text);
    	
    	int words = wordCount(text);
    	int time;
    	if(words>=5)
    		time = Toast.LENGTH_LONG;
    	else
    		time=Toast.LENGTH_SHORT;
    	Toast toast = Toast.makeText(c, text, time);
    	
        tts.speak(text, TextToSpeech.QUEUE_ADD, null);
        toast.show();
    }

	@Override
	public void onInit(int initStatus) {
		// TODO Auto-generated method stub
		Log.v("Speaker", "Initializing ...");
		
		tts.setLanguage(Locale.US);
		Log.v("Speaker", "It seems its working ...");
		if(fs!=null)
			speak(fs);
		fs = null;//free to garbage collector
	}
    
}
