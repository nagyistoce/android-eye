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
				        		 
				        		 Database.BASE_DIR = datasetEdit.getText().toString();
				        		
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
				        		
				        		String confFile = "/mnt/sdcard/data/"+confEdit.getText().toString();
				        		
				        		FaceDescriptor f = null;
				        		if(radioASM.isChecked())
				        			f = new StasmLib(confFile);
				        		else
				        			f = new LocalBinaryPattern(null, scaleW, scaleH);
				        		
				        		String pcaFile = new File(Globals.APP_DIR, "pca_95.dat").getAbsolutePath();
				        		Log.v("Experiment", "PCA File: "+pcaFile);
				        		f = new PrincipalComponentAnalysis(pcaFile);
				        		
				        		//FaceDescriptor f = new LocalBinaryPattern(null, scaleW, scaleH);
				        		//FaceDescriptor f = new PrincipalComponentAnalysis();
				        		//FaceDetect detector = new SkinFaceDetector();
				        		FaceDetect detector = null;
				        		long t = System.currentTimeMillis();
				        		
				        		handler.post(new Runnable() {
									
									@Override
									public void run() {
										// TODO Auto-generated method stub
										edit.append("Training ...\n");
									}
								});
				        		
				        		/*
				        		Pair<List<String>, List<Collection<Double>>> p = f.train(trainId, train);
				        		
				        		NearestNeighbor classifier = new NearestNeighbor();
				        		
				        		classifier.train(p.first, p.second);
				        		
				        		t = System.currentTimeMillis() - t;
				        		Log.v("PCA",""+(((double)t)/1000)+"s");
				        		
				        		LinkedList<Double> timeElapsed = new LinkedList<Double>();
				        		
				        		
				        		int nmistakes = 0;
				        		int ncorrects = 0;
				        		
				        		t=System.currentTimeMillis();
				        		
				        		classifier.rank = 0;
				        		
				        		int i = 0;
				        		Iterator<String> it = testId.iterator();
				        		for (File file : test) {
				        			String s = it.next();
				        			Bitmap b = FaceImage.loadImage(file);
				        			
				        			b = FaceImage.resizeBitmap(b, 0.5, 0.5);
				        			
				        			List<Rect> l = detector.findFaces(b);
				        			
				        			for (Rect rect : l) {
										Bitmap face = FaceImage.cropFace(b, rect);
										Collection<Double> d = f.getDescriptor(face);
										timeElapsed.add(f.timeElapsed());
										
					        			String label = classifier.classify(d, s);
					        			
					        			Log.v("Main", String.format("%d of %d", i+1, test.size()));
					        			
					        			i++;
					        			
					        			Log.v("Classifier", label+"<->"+s);
					        			if(label.equals(s)){
					        					ncorrects++;
					        					Log.v("NN", "Correct");
					        					final int cur_face = i;
					        					handler.post(new Runnable() {
					        						
					        						@Override
					        						public void run() {
					        							// TODO Auto-generated method stub
					        							edit.setText(String.format("%d -> Correct\n", cur_face));
					        						}
					        					});
					        			}
					        			else{
					        					nmistakes++;
					        					Log.v("NN", "Wrong");
					        					final int cur_face = i;
					        					handler.post(new Runnable() {
					        						
					        						@Override
					        						public void run() {
					        							// TODO Auto-generated method stub
					        							edit.setText(String.format("%d -> Incorrect\n", cur_face));
					        						}
					        					});
					        			}
					        			final int rank = classifier.rank;
					        			handler.post(new Runnable() {
					    					
					    					@Override
					    					public void run() {
					    						// TODO Auto-generated method stub
					    						edit.append(String.format("Rank = %d", rank));
					    					}
					    				});
					    				
					    				
					    				
				        		
				        			
									}
									*/
				        		
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
					        		final double descMean = MathUtils.mean(descTime);
					        		final double descStdDev = MathUtils.stdDeviation(descTime, descMean);
					        		final double descVar = MathUtils.variance(descTime, descMean);
					        		final double classifyVar = MathUtils.variance(descTime, descMean);

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
				        /*
				        Bitmap b = FaceImage.loadImage("/mnt/sdcard/database/f007/f007_5.jpg");
				        int pixels[] = new int[b.getWidth()*b.getHeight()];
				        
				        b.getPixels(pixels, 0, b.getWidth(), 0, 0, b.getWidth(), b.getHeight());
				        
				        for (int i = 0; i < pixels.length; i++) {
							Log.v("Img", ""+pixels[i]);
						}*/
				        
				        //OpenCV.save(pixels, b.getWidth(), b.getHeight(), "/mnt/sdcard/alan.bmp");
				        
					}
				}
        		);
        
        //float a[][] = new float[150][200*200];
        
        //List<String> l = Database.listID();
        
        
        //FaceDescriptor face = new LocalBinaryPattern();

       //Iterable<Double> it = face.getDescriptor(b);
       //face.getDescriptor(b);       
        
        /*try
        {
        	getVoiceAction();
        }catch(ActivityNotFoundException e)
        {
        	speakerOut.speak("Please, install speech recognition");
        	Intent browserIntent = new Intent(Intent.ACTION_VIEW,   Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.voicesearch&hl"));
        	startActivity(browserIntent);
        }*/
    
	}
	/*
	private void getVoiceAction() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Reconhecimento Voz");
        startActivityForResult(intent, VOICE_ACTION_REQUEST_CODE);
    }
	
	 protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		 if(requestCode == VOICE_ACTION_REQUEST_CODE) {
			 Log.d("Speech", "getting result resultCode=["+RESULT_OK+"]");
			 if(resultCode == RESULT_OK){
				 final ArrayList<String> lista = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
				 for(String s: lista){
					 speakerOut.speak(s);
				 }
			 }
		 }
	}*/

}