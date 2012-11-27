package com.example.androideye;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.graphics.Bitmap;
import android.util.Log;
import android.util.Pair;


/**
 * Active Shape Model face descriptor, uses the Stasm library
 * 
 * @author Alan Zanoni Peixinho
 *
 */
public class StasmLib extends FaceDescriptor{
	
	private final String TEMP_FILE = "/mnt/sdcard/alan.bmp";//Database.BASE_DIR+"temp.bmp";
	
	String confFile = null;
	private long time;
	
	static{
		System.loadLibrary("stasm");
	}
	public static native int add(int x);
	public static native int[] hello(String s);
	public static native double[] getFeatures(String fileName, String confFile);
	public static native double[] getPoints(int[] img, int width, int height);
	
	/**
	 * Creates an instance of {@link StasmLib} using the default stasm config file.
	 */
	public StasmLib() {
		// TODO Auto-generated constructor stub
		confFile = "/mnt/sdcard/data/mu-68-1d.conf";
	}
	
	/**
	 * Creates an instance of {@link StasmLib} using the indicated stasm config file.
	 * @param confFile Stasm config file
	 */
	public StasmLib(String confFile){
		this.confFile = new String(confFile);
	}
	
	@Override
	public void setDetector(FaceDetect detector) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public Pair<List<String>, List<Collection<Double>>> train(
			Collection<String> id, Collection<File> trainSet) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Collection<Double> getDescriptor(Bitmap img) {
				
		int width = img.getWidth();
		int height = img.getHeight();
		
		int pixels[] = new int[width*height];
		
		img.getPixels(pixels, 0, width, 0, 0, width, height);
		
		Log.v("Stasm", "Saving ...");
		OpenCV.save(pixels, width, height, TEMP_FILE);
		
		
		time = System.currentTimeMillis();
		
		Log.v("Stasm", "Searching ...");
		double f[] = getFeatures(TEMP_FILE, confFile);
		
		if(f==null)
				return null;
		
		
		Log.v("Stasm", "Maping in Collection ...");
		LinkedList<Double> feats = new LinkedList<Double>();
		for (double d : f) {
			feats.addLast(d);
		}
		Log.v("Stasm", "Feats Size = "+feats.size());
		//Log.v("Stasm", ""+feats);

		
		//Log.v("Stasm", "Deleting temp file ...");
		//File file = new File(TEMP_FILE);
		//file.delete();
		
		time = System.currentTimeMillis() - time;
		

		Log.v("STASM", "Time spent: "+((double)time)/1000.0);
		
		f = null;
		System.gc();
		
		return feats;
	}
	
	@Override
	public double timeElapsed() {
		// TODO Auto-generated method stub
		return (double)(time)/1000;
	}
	
	//euclidean distance between samples
	@SuppressWarnings("unused")
	/**
	 * Computes the euclidean distance between two samples.
	 * @param i1 First sample descriptor.
	 * @param i2 Second sample descriptor.
	 * @return Return the euclidean distance between the samples.
	 */
	private double euclideanDistance(Collection<Double> i1, Collection<Double> i2){
		double dist = 0.0;
		double value;
		
		assert(i1.size()==i2.size()):"Nao tem o mesmo numero de caracteristicas";
		
		Iterator<Double> it1 = i1.iterator();
		Iterator<Double> it2 = i2.iterator();
		
		while(it1.hasNext() && it2.hasNext()) 
		{
		   value = it1.next() - it2.next();
		   
		   dist += value*value;
		}//while
		
		return Math.sqrt(dist);
	}
	
	@Override
	public double distance(Collection<Double> c1, Collection<Double> c2) {
		// TODO Auto-generated method stub
		return euclideanDistance(c1, c2);
	}
}
