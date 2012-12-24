package com.example.androideye;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

public class ExperimentRun extends Activity{
    /** Called when the activity is first created. */
    
	final static int VOICE_ACTION_REQUEST_CODE = 1;
	
	EditText edit = null;
	TextToSpeech tts;
	Button btn;
	Speaker speakerOut = null;
	EditText scaleEditW = null;
	EditText scaleEditH = null;
	EditText topEdit = null;
	EditText confEdit = null;
	EditText datasetEdit = null;
	RadioButton radioLBP = null;
	RadioButton radioASM = null;
	RadioButton radioPCA = null;
	
	
	private Handler handler = null;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.experiment_main);
        
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
       
        KeyguardManager keyguardManager = (KeyguardManager)getSystemService(Activity.KEYGUARD_SERVICE);  
        KeyguardLock lock = keyguardManager.newKeyguardLock(KEYGUARD_SERVICE);
        
        
        lock.disableKeyguard();
        
        edit = (EditText) findViewById(R.id.editText1);
        scaleEditW = (EditText) findViewById(R.id.width);
        

        scaleEditW.setText("1.0");
        
        scaleEditH = (EditText) findViewById(R.id.height);

        scaleEditH.setText("1.0");
        
        topEdit = (EditText) findViewById(R.id.top);
        
        confEdit = (EditText) findViewById(R.id.model);
        
        datasetEdit = (EditText) findViewById(R.id.dataset);
        
        radioLBP = (RadioButton) findViewById(R.id.radioLBP);
        radioASM = (RadioButton) findViewById(R.id.radioASM);
        radioPCA = (RadioButton) findViewById(R.id.radioPCA);

        
        handler = new Handler();
        speakerOut = new Speaker(ExperimentRun.this);
        
        btn = (Button) findViewById(R.id.button1);
                
        btn.setOnClickListener(
        		new OnClickListener() {
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						Thread t = new Thread(){
				        	@SuppressWarnings("deprecation")
							public void run()
				        	{
				        		
				        		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
				        		 PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
				        		 wl.acquire();
				        		 
				        		 if (!Database.changeBaseDir(datasetEdit.getText().toString()) ){
				        			 handler.post(new Runnable() {
											
											@Override
											public void run() {
												// TODO Auto-generated method stub
												edit.setText("Invalid Database");
											}
										});
				        			 return;
				        		 }
				        		
				        		List<File> people = Database.listPeople();
				        		
				        		LinkedList<File> train = new LinkedList<File>();
				        		LinkedList<File> test = new LinkedList<File>();
				        		
				        		LinkedList<String> trainId = new LinkedList<String>();
				        		LinkedList<String> testId = new LinkedList<String>();
				        		
				        		handler.post(new Runnable() {
									
									@Override
									public void run() {
										// TODO Auto-generated method stub
										edit.setText("Listing images from "+Database.BASE_DIR+" ...\n");
									}
								});
				        		
				        		for (File person : people) {
									List<File> img = Database.listImages(person);
									
									Pair<LinkedList<File>, LinkedList<File>> p = Database.split(img, 0.5);
									
									for (File file : p.first) {
										trainId.addLast(person.getName());
										train.addLast(file);
										//testId.addLast(person.getName());
										//test.addLast(file);
									}
									
									for (File file : p.second) {
										testId.addLast(person.getName());
										test.addLast(file);
										//trainId.addLast(person.getName());
										//train.addLast(file);
									}
								}
				        		
				        		//train set and test set createds
				        		double scaleW = Double.parseDouble(scaleEditW.getText().toString());
				        		double scaleH = Double.parseDouble(scaleEditH.getText().toString());
				        		
				        		String confFileASM = "/mnt/sdcard/data/"+confEdit.getText().toString();
				        		String confFilePCA = "/mnt/sdcard/AndroidEye/"+confEdit.getText().toString();
				        		
				        		FaceDescriptor f = null;
				        		
				        		if(radioASM.isChecked()){
				        			f = new StasmLib(confFileASM);
				        		}
				        		else if(radioLBP.isChecked()){
				        			f = new LocalBinaryPattern(null, scaleW, scaleH);
				        		}
				        		else if(radioPCA.isChecked()){
				        			Log.v("experiment", "Its PCA Baby.");
					        		f = new PrincipalComponentAnalysis(confFilePCA);
				        		}

				        		FaceDetect detector = null;
				        		long t = System.currentTimeMillis();
				        		
				        		handler.post(new Runnable() {
									
									@Override
									public void run() {
										// TODO Auto-generated method stub
										edit.append("Training ...\n");
									}
								});
				        						        		
				        			LinkedList<Double> classifyTime = new LinkedList<Double>();
				        			LinkedList<Double> descTime = new LinkedList<Double>();
				        			
				        			NearestNeighbor classifier = new NearestNeighbor(detector, f);
				        			
				        			classifier._train(trainId, train);
				        			
				        			Iterator<String> it = testId.iterator();
				        			
				        			int top = Integer.parseInt(topEdit.getText().toString());
				        			

				        			int []ncorrects = new int[top];
				        			int []nmistakes = new int[top];
				        			
				        			for (int j = 0; j < top; j++) {
										ncorrects[j]=nmistakes[j]=0;
									}
				        			
				        			int curTest=1;
				        			for (File file:test) {
				        				Bitmap faceImage = FaceImage.loadImage(file);
				        				
				        				Log.v("Main", "Current File: "+file.getName());
				        				
				        				String s = it.next();
				        				
				        				t = System.currentTimeMillis();
				        				//Collection<String> founds = classifier._classify(faceImage);
										Collection<String> founds = classifier.topClassify(faceImage, top);
				        				t = System.currentTimeMillis() - t;
										if(founds.size()>0){
											classifyTime.add(((double)t)/1000);
											descTime.add(f.timeElapsed());
										}
										
										Log.v("MyOUT", "classify time: "+((double)t)/1000);
										
										final double time = (double)(t)/1000;
										final double desctime = f.timeElapsed();
				        				final String filename = file.getName();
										final String strFounds = ""+founds;
				        				final int cur = curTest;
										
										handler.post(new Runnable() {
											
											@Override
											public void run() {
												// TODO Auto-generated method stub
												//speakerOut.speak();
												
												StringBuffer s = new StringBuffer();
												s.append("\n\nClassifying ");
												s.append(filename);
												s.append("\nCur = ");
												s.append(cur);
												s.append("\nPrev classification time: ");
												s.append(time);
												s.append("s\nPrev descriptor time: ");
												s.append(desctime);
												s.append("s\n");
												s.append(strFounds);
												
												edit.setText(s.toString());
;											}
										});
										
										curTest++;
				        				
										Log.v("Main", "Classifieds: "+founds.size());
										int i=0;
										int idx=top;
										for (String string : founds) {
											if(s.equals(string)){
												idx=i;
												break;
											}
											i++;
										}
										
										for (int j = 0; j < idx; j++) {
											nmistakes[j]++;
										}
										for(int j=idx; j<top; j++){
											ncorrects[j]++;
										}
										
									}
				        			
					        		final int ntotal = ncorrects[0]+nmistakes[0];
					        		//final int rank = classifier.rank;
					        		final double classifyMean = MathUtils.mean(classifyTime);
					        		final double classifyStdDev = MathUtils.stdDeviation(classifyTime, classifyMean);
					        		final double classifyVar = MathUtils.variance(classifyTime, classifyMean);
					        		
					        		final double descMean = MathUtils.mean(descTime);
					        		final double descStdDev = MathUtils.stdDeviation(descTime, descMean);
					        		final double descVar = MathUtils.variance(descTime, descMean);

					        		for (int i = 0; i < top; i++) {
					        			final double acc = ((double)ncorrects[i])/(ncorrects[i]+nmistakes[i]);
					        			final int curTop = i+1;
						        		handler.post(new Runnable() {
											@Override
											public void run() {
												// TODO Auto-generated method stub
												edit.append(String.format("\nTop %d\nTotal Comparison = %d\nAccuracy = %f\nMean Time Classify = %f\nStdDev = %f\nVariance = %f\n\nMean Time Descriptor = %f\nStdDev = %f\nVariance = %f\n", curTop, ntotal, acc, classifyMean, classifyStdDev, classifyVar, descMean, descStdDev, descVar));
											}
										});
						        		
					        		}
					        		
					        		Log.v("Main", String.format("Corrects = %d Wrongs = %d", ncorrects[0], nmistakes[0]));
					        		t = System.currentTimeMillis() - t;
					        		Log.v("Main",""+(((double)t)/1000)+"s");
					        		
					        		wl.release();
									
								}
				        };
				        
				        t.setPriority(Thread.MAX_PRIORITY);
				        t.start();
				        
					}
				}
        		);
    
	}
	

}