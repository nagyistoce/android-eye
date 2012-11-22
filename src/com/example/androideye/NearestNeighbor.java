package com.example.androideye;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import junit.framework.Assert;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Log;


public class NearestNeighbor{
	public static final String CLASSIFIER_FILE = new File(Database.BASE_DIR, "samples.dat").getAbsolutePath();
	public static final String ID_FILE = new File(Database.BASE_DIR,"id.dat").getAbsolutePath();
	
	FaceDescriptor descriptor;
	FaceDetect detector;
	
	int nFeatures;
	
	File trainSet;
	File trainId;
	DataOutputStream fileout;
	DataInputStream filein;
	
	FileInputStream fis = null;
	BufferedInputStream bis = null;
	FileReader fr = null;
	
	BufferedReader IDin;
	BufferedWriter IDout;
	
	String curId;
	LinkedList<Double> cur;
	
	LinkedList<Double> topDist;
	LinkedList<String> topLabel;
	
	private int consulteds = 0;
	
		
	public NearestNeighbor(FaceDetect det, FaceDescriptor desc){
		setDescriptor(desc);
		setDetector(det);
		initialize();
	}
	
	public void initialize() {
		Locale.setDefault(Locale.US);//to read the double from text file
		cur = new LinkedList<Double>();
		
		descriptor.setDetector(detector);
		
		trainSet = new File(CLASSIFIER_FILE);
		trainId = new File(ID_FILE);
		
		topDist = new LinkedList<Double>();
		topLabel = new LinkedList<String>();
		
	}
	
	public boolean isTrained(){
		return trainId.exists() && trainSet.exists();
	}
	
	public NearestNeighbor(){
		setDescriptor(new LocalBinaryPattern());
		setDetector(new SkinFaceDetector());
		initialize();
	}
	
	private void setDescriptor(FaceDescriptor d)
	{
		descriptor = d;
	}
	
	private void setDetector(FaceDetect d){
		detector = d;
	}
	
	public void train(UserInterface ui){
	
		trainSet.delete();//if exists, doesn't exist anymore
		trainId.delete();
		
		try {
			trainSet.createNewFile();
			trainId.createNewFile();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		int i = 0;
		
		Log.v("NN", "Listing people ...");
		List<File> people = Database.listPeople();
		Log.v("NN", "People number: "+people.size());
		for (File person : people) {
			Log.v("NN", "Adding "+person.getName());
			List<File> imgs = Database.listImages(person);
			for (File file : imgs) {
				Log.v("NN", "Opening image "+file.getName());
				
				Bitmap b = FaceImage.loadImage(file);
				//ui.callSpeaker("Width: "+b.getWidth()+", Height: "+b.getHeight());
				//b = FaceImage.resizeBitmap(b, 0.5, 0.5);
				String s = person.getName();//directory name is the person ID
				
				List<Rect> r = detector.findFaces(b);
				
				if(r.size()==1)
				{	
					ui.callSpeaker("Adding "+Database.personName(person.getName()));

					b = FaceImage.cropFace(b, r.get(0));
					Collection<Double> desc = descriptor.getDescriptor(b);
					
					if(i==0){
						setNumFeatures(desc.size());
					}
					if(desc!=null){
						addSample(s, desc);
						//ui.callSpeaker(String.format("%s added to classifier.", file.getName()));
						Log.v("NN", String.format("%s added to classifier.", file.getName()));
						i++;
					}
					else{
						//ui.callSpeaker(String.format("%s had an error getting descriptor.", file.getName()));
						Log.v("NN", String.format("%s had an error getting descriptor.", file.getName()));
					}

				}
				else{
					//ui.callSpeaker(String.format("%d faces detecteds", r.size()));
					if(r.size()!=0)
						Log.v("NN", String.format("%s contains %d faces, not added to classifier.", file.getName(),r.size()));
					else
						Log.v("NN", String.format("No faces found in %s, not added to classifier.", file.getName(),r.size()));
				}
			}
		}
		
		try {
			fileout.close();
			IDout.close();
			//filein = new DataInputStream(new FileInputStream(trainSet));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	//train the classifier with the dataset
		public void _train(Collection<String> id, Collection<File> imgs){
			
			assert(id.size()==imgs.size()):"The labels number and images number is different";
			
			/*if(1==1)
				return;
			*/
			trainSet.delete();//if exists, doesn't exist anymore
			trainId.delete();
			
			try {
				trainSet.createNewFile();
				trainId.createNewFile();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			Iterator<String> it = id.iterator();

			int i=0;
			for (File file : imgs) {
				Log.v("NN", "Current File: "+file.getName());
				Bitmap b = FaceImage.loadImage(file);
				//b = FaceImage.resizeBitmap(b, 0.5, 0.5);
				String s = it.next();
			
				Collection<Double> desc = descriptor.getDescriptor(b);
				
				if(desc!=null){
					
					if(i==0){
						setNumFeatures(desc.size());
					}
					addSample(s, desc);
					
					Log.v("NN", String.format("%s added to classifier.", file.getName()));
					i++;
				}
			}
			
			try {
				fileout.close();
				IDout.close();
				//filein = new DataInputStream(new FileInputStream(trainSet));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	
	//train the classifier with the dataset
	public void train(Collection<String> id, Collection<File> imgs){
		
		assert(id.size()==imgs.size()):"The labels number and images number is different";
			
		trainSet.delete();//if exists, doesn't exist anymore
		trainId.delete();
		
		try {
			trainSet.createNewFile();
			trainId.createNewFile();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		Iterator<String> it = id.iterator();

		int i=0;
		for (File file : imgs) {
			Bitmap b = FaceImage.loadImage(file);
			//b = FaceImage.resizeBitmap(b, 0.5, 0.5);
			String s = it.next();
			List<Rect> r = detector.findFaces(b);
			
			if(r.size()==1)
			{
				b = FaceImage.cropFace(b, r.get(0));
				Collection<Double> desc = descriptor.getDescriptor(b);
				
				if(i==0){
					setNumFeatures(desc.size());
				}
				if(desc!=null){
					addSample(s, desc);

					Log.v("NN", String.format("%s added to classifier.", file.getName()));
					i++;	
				}
				else{
					Log.v("NN", String.format("%s had an error getting descriptor.", file.getName()));
				}
					
			}
			else{
				if(r.size()!=0)
					Log.v("NN", String.format("%s contains %d faces, not added to classifier.", file.getName(),r.size()));
				else
					Log.v("NN", String.format("No faces found in %s, not added to classifier.", file.getName(),r.size()));
			}
		}
		
		try {
			fileout.close();
			IDout.close();
			//filein = new DataInputStream(new FileInputStream(trainSet));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//store the classifier in file
	private void setNumFeatures(int numFeatures){
		nFeatures=numFeatures;
		try {
			fileout = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(trainSet), 2*1024));
			IDout = new BufferedWriter(new FileWriter(trainId));
		}
		catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
		
		
		try {
			fileout.writeInt(numFeatures);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Log.v("Info", "Features Number Stored: "+nFeatures);
	}
	//get the classifier in file
	private void getNumFeatures(){
		
		try {
			Log.v("NN", "File stream closing ...");
			if(filein!=null){
				filein.close();
				IDin.close();
				filein = null;
				IDin = null;
			}
			Log.v("NN", String.format("File stream opening ...%s %s", trainSet.getName(), trainId.getName()));
			
			if(fis!=null){
				fis.close();
				fis=null;
			}
			if(bis!=null){
				bis.close();
				bis=null;
			}
			if(fr!=null){
				fr.close();
				fr=null;
			}
			
			System.gc();
			
			fis = new FileInputStream(trainSet);
			bis = new BufferedInputStream(fis,2*1024);
			filein = new DataInputStream(bis);//buffer-100k
			
			fr = new FileReader(trainId);
			IDin = new BufferedReader(fr, 2*1024);
			Log.v("NN", "File stream opened");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		int x = 0;
		try {
			x = filein.readInt();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		nFeatures = x;
		//Log.v("Info", "Features Number Loaded: "+nFeatures);
	}
	//store classifier in file
	private void addSample(String s, Collection<Double> c)
	{
		try{
			IDout.write(s+'\n');
			Assert.assertTrue(c.size()==nFeatures);
			for (double d: c) {
				fileout.writeFloat((float)d);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	//get the classifier in file
	private boolean getSample(){
		
		cur.clear();
		String s;
		
		try{
				s = IDin.readLine();
				if (s==null) {
					return false;
				}
				curId = s;
				for (int i = 0; i < nFeatures; i++) {
					double d = (double)filein.readFloat();
					cur.addLast(d);
				}
		}
		catch(Exception e){}
		
		return true;
	}
	
	

	
	//get the label of c sample
	private String closest(Collection<Double> c){
		String minId = null;
		double minDist = Double.MAX_VALUE;
		
		//Log.v("NN", "Comparing to database");
		
		int i=0;
		getNumFeatures();
		
		while(getSample()){
		
			//Log.v("NN", "Comparing "+i);
			i++;
			//double dist = chiSquareDistance(c, cur);
			double dist = descriptor.distance(c, cur);
			//Log.v("NN", String.format("Comparing with %s - dist = %f", curId, dist));
			if (dist<minDist) {
				minDist=dist;
				minId=new String(curId);
			}
		}
		
		//Log.v("NN", String.format("Closest label %s.", minId));
		
		/*try {
			filein.close();
			IDin.close();
		} catch (IOException e) {
			e.printStackTrace();
		}*/
		
		return minId;
	}
	
	public Collection<Double> getBestDistances(){
		LinkedList<Double> list = new LinkedList<Double>();
		
		for (Double d : topDist) {
			list.addLast(d);
		}
		
		return list;
	}
	
	//get the label of c sample
	private Collection<String> topClosest(Collection<Double> c, int topsize){
		String minId = null;
		double minDist = Double.MAX_VALUE;
		
		//Log.v("NN", "Comparing to database");
		
		topDist.clear();
		topLabel.clear();
		
		for (int i = 0; i < topsize; i++) {
			topDist.add(Double.MAX_VALUE);
			topLabel.add(null);
		}
		
		int i=0;
		getNumFeatures();
		
		while(getSample()){
		
			//Log.v("NN", "Comparing "+i);
			i++;
			//double dist = chiSquareDistance(c, cur);
			double dist = descriptor.distance(c, cur);
			//Log.v("NN", String.format("Comparing with %s - dist = %f", curId, dist));
			
			Iterator<String> itLabel = topLabel.iterator();
			Iterator<Double> itDist = topDist.iterator();
			
			for (int j = 0; j < topsize; j++) {
				String s = itLabel.next();
				double d = itDist.next();
				
				if (dist<d) {
					topDist.add(j, dist);
					topLabel.add(j, new String(curId));
					
					topDist.removeLast();
					topLabel.removeLast();
					break;
				}
			}
		}
		consulteds = i;
		return topLabel;
	}
	
	public int consultedInDatabase(){
		return consulteds;
	}
	
	public Collection<String> _classify(Bitmap image){
		
		Log.v("NN","Classifying ...");
		//image = FaceImage.resizeBitmap(image, 0.5, 0.5);//down resolution to improve the face detection time
		//List<Rect> rects = detector.findFaces(image);
		LinkedList<String> labels = new LinkedList<String>();
		
		//Log.v("NN", String.format("%d faces detecteds.", rects.size()));
		
	
		Collection<Double> desc = descriptor.getDescriptor(image);
		if(desc!=null){
			String label = closest(desc);
			if(label!=null)
				labels.addLast(label);
		}
		return labels;
	}
	
	//detect faces, extract features and return the face labels
	public Collection<String> classify(Bitmap image){
		
		Log.v("NN","Classifying ...");
		//image = FaceImage.resizeBitmap(image, 0.5, 0.5);//down resolution to improve the face detection time
		List<Rect> rects = detector.findFaces(image);
		LinkedList<String> labels = new LinkedList<String>();
		
		Log.v("NN", String.format("%d faces detecteds.", rects.size()));
		
		for (Rect rect : rects) {
			Bitmap b = FaceImage.cropFace(image, rect);
			Collection<Double> desc = descriptor.getDescriptor(b);
			String label = closest(desc);
			labels.addLast(label);
		}
		
		return labels;
	}
	
	public Collection<String> topClassify(Bitmap img, int topsize){
		Log.v("NN","Classifying ...");
		//image = FaceImage.resizeBitmap(image, 0.5, 0.5);//down resolution to improve the face detection time
		//List<Rect> rects = detector.findFaces(image);		
		//Log.v("NN", String.format("%d faces detecteds.", rects.size()));
		
		Collection<String> labels = null;
		Log.v("NN", "Descriptor");
		Collection<Double> desc = descriptor.getDescriptor(img);
		if(desc!=null){
			Log.v("NN", "Searching closest ...");
			labels = topClosest(desc, topsize);
			Log.v("NN", String.format("%d Closest founds ...", labels.size()));
		}
		else{
			//Log.v("NN", String.format("labels = %p", labels));
			return new LinkedList<String>();//empty list
		}
		Log.v("NN", "");
		return labels;
	}
	
}
