package com.example.androideye;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.widget.Toast;


public class Speaker implements OnInitListener{
	
	private TextToSpeech tts;
	Context c = null;
	String fs = null;
	
	private void initialize(Context context){
		c = context;
		Log.v("Speaker", "Creating Text To Speech");
        tts = new TextToSpeech(c, this);
	}
	
	public Speaker(Context context, String firstSpeak)
	{
		fs = new String(firstSpeak);
		initialize(context);
	}
	
	public Speaker(Context context)
	{
		initialize(context);
	}
	
	private int wordCount(String s){//fast words number estimate
		int sz = s.length();
		int count=1;
		for (int i = 0; i < sz; i++) {
			if(Character.isSpace(s.charAt(i)))
				count++;
		}
		return count;
	}
	
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
	public void onInit(int arg0) {
		// TODO Auto-generated method stub
		Log.v("Speaker", "Initializing ...");
		if(fs!=null)
			speak(fs);
		fs = null;//free to garbage collector
	}
    
}
