package com.example.androideye;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.ImageButton;


/**
 * Main Android Eye User Interface.
 * @author Alan Zanoni Peixinho
 *
 */
//Please forgive me for this code. I know it is ugly :P, but works
public class UserInterface extends Activity implements OnTouchListener, RecognitionListener, SurfaceHolder{

	public final static String APP_DIR = Globals.APP_DIR.getAbsolutePath();
	
	public static final int MAX_SAMPLES = 10;
	public static final int TOP = 3;
	public static final double DETECT_SCALE = 0.5;

	ImageButton speakButton = null;
	
	Camera.PictureCallback jpegPictureCallback = null;
	Camera.ShutterCallback shutterCallback = null;
	PictureCallback previewCallback = null;
	
	Bitmap bitmap = null;
	
	Handler mhandler = null;
	public Speaker speaker = null;
	NearestNeighbor classifier = null;
	FaceDetect detector = null;
	FaceDescriptor descriptor = null;
	
	
	Camera camera = null;
	
	/**
	 * Recognizer task, which runs in a worker thread.
	 */
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
		WHAT_DO,
		EXIT,
		TRAIN,
		UPDATE,
		NONE,
	};
	
	Question curQuestion = Question.WHAT_DO;
	State curState = State.WAITING_ANSWER;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       
        /*
         * Disable App Rotation, Key Lock and Screen shutdown
         * */
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        KeyguardManager keyguardManager = (KeyguardManager)getSystemService(Activity.KEYGUARD_SERVICE);  
        KeyguardLock lock = keyguardManager.newKeyguardLock(KEYGUARD_SERVICE);
        lock.disableKeyguard();
        
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        setContentView(R.layout.activity_main);
        speakButton = (ImageButton) findViewById(R.id.speakButton);
        speakButton.setOnTouchListener(this);
        speakButton.requestFocus();
        
        
        /*
         * Speech Recognition
         * */
        rec = new RecognizerTask();
		rec_thread = new Thread(this.rec);
        rec.setRecognitionListener(this);
		rec_thread.start();
		
		mhandler = new Handler();
		
		
		/*
		 * Face Recognition
		 * */
		detector = new SkinFaceDetector();
		descriptor = new LocalBinaryPattern(detector, 0.5, 0.25);
		classifier = new NearestNeighbor(detector, descriptor);
		
		/*
		 * Get Image From Camera
		 * */
		jpegPictureCallback = new Camera.PictureCallback() {
			

			@Override
			public void onPictureTaken(byte[] data,
					android.hardware.Camera camera) {
				// TODO Auto-generated method stub
				Log.v("CAMERA", "Calling jpeg callback.");
								
				// load bitmap from resources
				Bitmap bMap = BitmapFactory.decodeByteArray(data, 0, data.length);
				
				//rotate if the image is horizontal
				int orientation;
				 // others devices
		        if(bMap.getHeight() < bMap.getWidth()){
		            orientation = 90;
		        } else {
		            orientation = 0;
		        }

		        if (orientation != 0) {
		            Matrix matrix = new Matrix();
		            matrix.postRotate(orientation);
		            bitmap = Bitmap.createBitmap(bMap, 0, 0, bMap.getWidth(),
		                    bMap.getHeight(), matrix, true);
		        } else
		            bitmap = Bitmap.createScaledBitmap(bMap, bMap.getWidth(),
		                    bMap.getHeight(), true);
		      
		        recognize(bitmap);
		        
				camera.stopPreview();
				camera.release();
			}
		};
		
		previewCallback = new Camera.PictureCallback() {
			
			@Override
			public void onPictureTaken(byte[] data, Camera camera) {
				// TODO Auto-generated method stub
				
			}
		};
		
		shutterCallback = new Camera.ShutterCallback() {
			
			@Override
			public void onShutter() {
				// TODO Auto-generated method stub
				
			}
		};
		/*
         * Text to Speech
         * */
		String s = null;
		if(classifier.isTrained())
				s = "Welcome to Android Eye.\nWhat you want to do?";
		else
				s = "Your database is empty. Call the update and train commands.";
        speaker = new Speaker(UserInterface.this.getApplicationContext(), s);        
    }
    
    private void recognize(Bitmap bitmap){
    	 	
    	final Bitmap bmp = bitmap;
    	//final Bitmap bmp = FaceImage.loadImage(new File(APP_DIR, "camera2.jpg"));
    	if (!classifier.isTrained()) {
    		callSpeaker("I am sorry. You need to train first.");
    		callSpeaker("What you want to do?");
	        curQuestion = Question.WHAT_DO;
			curState = UserInterface.State.WAITING_ANSWER;
    		return;
    	}
    	
    	Thread t = new Thread(){
    		public void run() {
    			
    			Bitmap bscale = FaceImage.resizeBitmap(bmp, DETECT_SCALE);
    			    			
    			callSpeaker("Detecting faces.");
    	        
    	        Collection<Rect> faces = detector.findFaces(bscale);
    	        
    	        bscale.recycle();
    	        
    	        int facesNum = faces.size();
    	        if(facesNum==1)
    	        {
    	        	callSpeaker("Found 1 face.");
    	       	}
    	        else if(facesNum==0){
    	        	callSpeaker("No faces found.");
    	        }
    	        else{
    	        	callSpeaker((String.format("Found %d faces.", facesNum)));
    	        }
    	        
    	        int curFace = 1;
    	        
    	        for (Rect rect : faces) {
    				//call classify        	        
    	        	Rect scaleRect = FaceImage.resizeRect(rect, 1.0/DETECT_SCALE);
    	        	Bitmap cropped = FaceImage.cropFace(bmp, scaleRect);
    	        	
    	        	FaceImage.saveImage(cropped, new File(APP_DIR, "face"+curFace+".jpg"));
    	        	
    		        Collection<String> labels =  classifier.topClassify(cropped, TOP);//the 2 closest people
    		        cropped.recycle();
    		        
    		        callSpeaker(String.format("Face Number %d is probably", curFace++));
    				int curLabel = 0;
    				Collection<Double> dists = classifier.getBestDistances();
    				Iterator<Double> distsIt = dists.iterator();
    				
    				for (String label : labels) {
    					double d = distsIt.next();
    					
    					/*if(d>THRESHOLD){
    						if(curLabel==0){
    							callSpeaker("I do not know this person.");
    						}
    						else{
    							break;
    						}
    					}*/
    					
    					String name = Database.personName(label);
    					if(curLabel==0){
    						callSpeaker(String.format("%s with distance %.3f", name, d));
    					}
    					else{
    						callSpeaker(String.format("or %s with distance %.3f", name, d));
    					}
    					curLabel++;
    				}
    				
    				callSpeaker(String.format("Consulteds %d faces.", classifier.consultedInDatabase()));
    				
    			}
    	        
    	        
    	        callSpeaker("What you want to do?");
    	        curQuestion = Question.WHAT_DO;
    			curState = UserInterface.State.WAITING_ANSWER;
    		}
    	};
    	t.start();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
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
	
	private void whatDoAction(String ans){
		if(ans.contains("SCAN")){
			scanCamera();
		}
		else if(ans.contains("BYE") || ans.contains("EXIT")){
			curState = State.WAITING_ANSWER;
			curQuestion = Question.EXIT;
			//call speaker "Do you really want to exit?"
			callSpeaker("Do you really want to exit?");
		}
		else if(ans.contains("TRAIN")){
			curState = State.WAITING_ANSWER;
			curQuestion = Question.TRAIN;
			callSpeaker("It is recommended connect to a power source.");
			callSpeaker("Do you really want to train?");
		}
		else if(ans.contains("UPDATE")){
			curState = State.WAITING_ANSWER;
			curQuestion = Question.UPDATE;
			callSpeaker("Do you really want to update?");
		}		
		else{
			callSpeaker("I am sorry. What you want to do?");
			curState = State.WAITING_ANSWER;
		}
	}
	
	private void exitAction(String ans){
		if(ans.contains("YES")){
			callSpeaker("See you later.");
			
			UserInterface.this.finish();
		}
		else if(ans.contains("NO")){
			curState = State.WAITING_ANSWER;
			curQuestion = Question.WHAT_DO;

			callSpeaker("Ok Then. What you want to do?");
		}
		else{
			callSpeaker("I am sorry. Do you want to exit?");
			curState = State.WAITING_ANSWER;
		}
	}
	
	private void updateAction(String ans){
		if(ans.contains("YES")){
			callSpeaker("Starting the update.");
			updateDatabase();
			curQuestion = Question.WHAT_DO;
		}
		else if(ans.contains("NO")){
			curState = State.WAITING_ANSWER;
			curQuestion = Question.WHAT_DO;

			callSpeaker("Ok Then. What you want to do?");
		}
		else{
			callSpeaker("I am sorry. Do you want to update?");
			curState = State.WAITING_ANSWER;
		}
	}
	
	private void trainAction(String ans){
		if(ans.contains("YES")){
			callSpeaker("Training.");
			train();
		}
		else if(ans.contains("NO")){
			curState = State.WAITING_ANSWER;
			curQuestion = Question.WHAT_DO;

			callSpeaker("Ok Then. What you want to do?");
		}
		else{
			callSpeaker("I am sorry. Do you want to train?");
			curState = State.WAITING_ANSWER;
		}
	}	
	
	private void scanCamera(){
		curState = State.BUSY;
		
		callSpeaker("Getting image.");
		camera = Camera.open();
		
		try {			
			camera.setPreviewDisplay(this);
			camera.startPreview();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		callSpeaker("Taking a picture.");
		camera.takePicture(shutterCallback, previewCallback, jpegPictureCallback);
	}
	
	private void train(){
		curState = State.BUSY;
		
		Thread t = new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				
				classifier.train(UserInterface.this);
				//classifier.train(UserInterface.this);
				
				//play notification sound
				try {
			        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
			        r.play();
			    } catch (Exception e) {}
				
				callSpeaker("Training complete.");
				
				//training finished
				curState = State.WAITING_ANSWER;
				curQuestion = Question.WHAT_DO;
				callSpeaker("What you want to do?");
			}
		});
		
		t.setPriority(Thread.MAX_PRIORITY);
		
		t.start();
		
	}
	
	private void updateDatabase(){
		callSpeaker("It is time to update the database.");
		//call update
		curState = State.BUSY;
		
		Thread t = new Thread(new Runnable() {
					
					@Override
					public void run() {
						ContactPicker cp = new ContactPicker(UserInterface.this.getContentResolver(), MAX_SAMPLES);
						callSpeaker("Getting list ...");
						
						int sz=cp.contactsNumber();
						int i=0;
						double perc=0.0;
						while(cp.nextContact()){
							
							if(i>=0.25*sz && perc<0.25){
								perc=0.25;
								callSpeaker("25% complete.");
							}
							else if(i>=0.5*sz && perc<0.5){
								perc=0.5;
								callSpeaker("50% complete.");
							}
							else if(i>=0.75*sz && perc<0.75){
								perc=0.75;
								callSpeaker("75% complete.");
							}
							
							i++;
						}
					
						callSpeaker("Update complete.");
						
						//play notification sound
						try {
					        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
					        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
					        r.play();
					    } catch (Exception e) {}
						
						callSpeaker("Update complete.");
						curState = State.WAITING_ANSWER;
						callSpeaker("What you want to do?");
					}
		});
		
		t.setPriority(Thread.MAX_PRIORITY);
		t.start();

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
		case WHAT_DO:
			whatDoAction(ans);
			break;
		case EXIT:
			exitAction(ans);
			break;
		case UPDATE:
			updateAction(ans);
			break;
		case TRAIN:
			trainAction(ans);
			break;
		default:
			break;
		}
		
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
	public void onError(int err) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addCallback(Callback callback) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Surface getSurface() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Rect getSurfaceFrame() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isCreating() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Canvas lockCanvas() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Canvas lockCanvas(Rect dirty) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeCallback(Callback callback) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setFixedSize(int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setFormat(int format) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setKeepScreenOn(boolean screenOn) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setSizeFromLayout() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setType(int type) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unlockCanvasAndPost(Canvas canvas) {
		// TODO Auto-generated method stub
		
	}
}
