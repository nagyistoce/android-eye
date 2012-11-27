package com.example.androideye;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.ImageButton;

/**
 * Tutorial User Interface.
 * @author Alan Zanoni Peixinho
 *
 */
public class Tutorial extends Activity implements OnTouchListener, RecognitionListener{

    private static final String TAG = "Tutorial";
    private static final String TARGET_BASE_PATH = new File(Environment.getExternalStorageDirectory(), "AndroidEye").getAbsolutePath();

    static{
    	File f = new File(TARGET_BASE_PATH);
    	if(!f.exists())
    		f.mkdir();
    }
	
	ImageButton speakButton = null;
	
	Handler mhandler = null;
	Speaker speaker = null;

	RecognizerTask rec;
	/**
	 * Thread in which the recognizer task runs.
	 */
	Thread rec_thread;
	/**
	 * Time at which current recognition started. 
	 */

	public enum State{
		PROCRASTINATING,
		LISTENING,
		BUSY,
		WAITING_ANSWER,
	};
	
	public enum Question {
		YES,
		EXIT,
		TRAIN,
		UPDATE,
		SCAN,
		NONE,
	};
	
	Question curQuestion = Question.NONE;
	State curState = State.BUSY;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
	    speakButton = (ImageButton) findViewById(R.id.speakButton);
	    speakButton.setOnTouchListener(this);
	    speakButton.requestFocus();
	     
	    setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	        
        KeyguardManager keyguardManager = (KeyguardManager)getSystemService(Activity.KEYGUARD_SERVICE);  
        KeyguardLock lock = keyguardManager.newKeyguardLock(KEYGUARD_SERVICE);
        lock.disableKeyguard();
        
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	     
	     Thread t = new Thread(){
	    	 @Override
	    	public void run() {
	    		// TODO Auto-generated method stub
	    		curState = Tutorial.State.BUSY;
	    		copyFilesToSdCard();

	    		callSpeaker("Install complete.");
	    		
	    		callSpeaker("The AndroidEye UserInterface uses the voice.");
	    		
	    		callSpeaker("To use any command you should press and hold the screen, or the action button.");
	    		
	    		callSpeaker("If you understood say: yes.");
	    		
	    		curQuestion = Question.YES;
	    		curState = Tutorial.State.WAITING_ANSWER;
	    		
	    		Log.v(TAG, "Initializing speech recognition");
	    		
	    		rec = new RecognizerTask();
	    		Log.v(TAG, "Recognizer Task OK");
	    		rec_thread = new Thread(Tutorial.this.rec);
	    		Log.v(TAG, "Recognizer Thread OK");
	    	    rec.setRecognitionListener(Tutorial.this);
	    	    Log.v(TAG, "Recognizer Listener OK");
	    	    rec_thread.start();
	    	 }
	     };
	     
	     t.start();
	     
	     /*
	         * Speech Recognition
	         * */
			
		mhandler = new Handler();

        speaker = new Speaker(Tutorial.this.getApplicationContext(), "Welcome to the Android Eye. Please wait the install process.");
        
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		
		switch(event.getAction()){
			case MotionEvent.ACTION_DOWN:
				startRec();
				break;
			case MotionEvent.ACTION_UP:
				stopRec();
				break;
		}
		return false;
	}
	
	private void stopRec(){
		speakButton.setImageResource(R.drawable.microphone_normal);				
		if(curState==State.BUSY)
				return;
		
		rec.stop();
		
		//call action
		curState = State.WAITING_ANSWER;
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {

	    if(event.getAction() == KeyEvent.ACTION_DOWN)
	    {
	        switch(keyCode)
	        {
	        	case KeyEvent.KEYCODE_DPAD_CENTER:
	        		startRec();
	            return true;
	        }
	    }

	    return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(event.getAction() == KeyEvent.ACTION_UP)
	    {
	        switch(keyCode)
	        {
	        	case KeyEvent.KEYCODE_DPAD_CENTER:
	        		stopRec();
	            return true;
	        }
	    }

		return super.onKeyUp(keyCode, event);
	}
	
	private void startRec(){
		if (curState==State.LISTENING) {
			return;//shhhh i can't talk i'm recording
		}
		
		if(curState==State.BUSY){
			callSpeaker("I am sorry. I am busy.");
			return;
		}
		
		speakButton.setImageResource(R.drawable.microphone_pressed);
		//speaker.speak("It works");
		rec.start();
		curState = State.LISTENING;
	}

	
	public void callSpeaker(String talk){
		final String s = talk;
		mhandler.post(new Runnable() {//necessary to access in different threads
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				speaker.speak(s);
			}
		});
	}
	
	@Override
	public void onPartialResults(Bundle b) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onResults(Bundle b) {
		// TODO Auto-generated method stub
		String ans = b.getString("hyp");
		
		if(ans==null)
				return;
		
		final String lower = ans.toLowerCase();//the user answer comes in uppercase
		
		//repeat the user answer
		callSpeaker(lower);
		
		switch (curQuestion) {
		case YES:
			yesAction(ans);
			break;
		case UPDATE:
			updateAction(ans);
			break;
		case TRAIN:
			trainAction(ans);
			break;
		case SCAN:
			scanAction(ans);
			break;
		case EXIT:
			exitAction(ans);
			break;
		default:
			break;
		}
	}
	
	private void exitAction(String ans){
		if(ans.contains("EXIT") || ans.contains("BYE")){
			callSpeaker("See you later.");
			Tutorial.this.finish();
		}
		else{
			callSpeaker("Use the exit command to close the app.");
		}
	}
	
	private void updateAction(String ans){
		if(ans.contains("UPDATE")){
			callSpeaker("Wonderfull.");
			callSpeaker("The train command extract informations of the contact photos.");
			callSpeaker("Say: train.");
			curQuestion = Question.TRAIN;
		}
		else{
			//callSpeaker("The train command extract informations of the contact photos.");
			callSpeaker("The update command import your contact photos and add to the AndroidEye database.");
			callSpeaker("Try again. Press and hold the screen, or the action button, and say: update.");
		}
	}
	
	private void scanAction(String ans){
		if(ans.contains("SCAN")){
			callSpeaker("My job here is done.");
			callSpeaker("Use the exit command to close the app.");
			curQuestion = Question.EXIT;
		}
		else{
			callSpeaker("Try again. Press and hold the screen, or the action button, and say: scan.");
		}
	}
	
	private void trainAction(String ans){
		if(ans.contains("TRAIN")){
			callSpeaker("Perfect.");
			callSpeaker("The scan command get a photo from the camera and recognize the face in the picture.");
			callSpeaker("Say: scan.");
			curQuestion = Question.SCAN;
		}
		else{
			callSpeaker("Try again. Press and hold the screen, or the action button, and say: train.");
		}
	}
	
	private void yesAction(String ans){
		if(ans.contains("YES"))
		{
			callSpeaker("Right.");
			callSpeaker("The update command import your contact photos and add to the AndroidEye database.");
			callSpeaker("Your turn. Press and hold the screen, or the action button, and say: update.");
			curQuestion = Question.UPDATE;
		}
		else{
			callSpeaker("Try again. Press and hold the screen, or the action button, and say: yes.");
		}
	}

	@Override
	public void onError(int err) {
		// TODO Auto-generated method stub
		
	}
	
	private void copyFilesToSdCard() {
        copyFileOrDir("");
    }

    private void copyFileOrDir(String path) {
        AssetManager assetManager = this.getAssets();
        String assets[] = null;
        try {
            Log.i(TAG, "copyFileOrDir() "+path);
            assets = assetManager.list(path);
            for(String s : assets){
            	Log.i(TAG, "path -> " +s);
            }
            if (assets.length == 0) {//is a file
                copyFile(path);
            } else {//is a directory
                File dir = new File(TARGET_BASE_PATH, path);
                Log.i(TAG, "path="+dir.getAbsolutePath());
                if (!dir.exists())
                    if (!dir.mkdirs());
                        Log.i(TAG, "could not create dir "+dir.getAbsolutePath());
                for (int i = 0; i < assets.length; ++i) {
                	copyFileOrDir( new File(path, assets[i]).getAbsolutePath().substring(1));//remove the '/'
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "I/O Exception", e);
        }
    }

    private void copyFile(String filename) {
        AssetManager assetManager = this.getAssets();
        Log.i(TAG, "Assets Manager"+assetManager.toString());

        InputStream in = null;
        OutputStream out = null;
        File newFile = null;
        try {
            Log.i(TAG, "copyFile() "+filename);
            Log.i(TAG, "Opening "+filename);
            in = assetManager.open(filename);
            Log.i(TAG, "Opened");
            String outFileName = null;
            if(filename.endsWith("sendump.png") || filename.endsWith("mdef.png"))
            	outFileName = filename.substring(0, filename.length()-4);
            else
            	outFileName = filename;
            newFile = new File(TARGET_BASE_PATH, outFileName);
            
            if(newFile.exists())
            	newFile.delete();
            newFile.createNewFile();
            
            Log.i(TAG, "New File: "+newFile.getAbsolutePath());           
            out = new FileOutputStream(newFile);
            Log.i(TAG, "target " + newFile.getAbsolutePath());
            
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
        } catch (Exception e) {
            Log.e(TAG, "Exception in copyFile() of "+newFile.getAbsolutePath());
        }

    }

}
