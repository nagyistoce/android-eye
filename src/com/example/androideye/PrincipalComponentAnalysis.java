package com.example.androideye;

import java.io.DataInputStream;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.util.Log;

public class PrincipalComponentAnalysis implements FaceDescriptor{
	
	private static double WIDTH = 75.0;
	private static double HEIGHT = 150.0;
	
	float feats[] = null;
	float trasformMatrix[][] = null;
	float mean[]=null;
	int rgbpixels[] = null;
	float pixels[] = null;
	
	String fileName = null;
	
	long time;
	
	public PrincipalComponentAnalysis(){
		initialize(new File(Database.BASE_DIR, "PCA_95.dat").getAbsolutePath());
	}
	
	public PrincipalComponentAnalysis(File transformFile){
		initialize(transformFile.getAbsolutePath());
	}
	
	public PrincipalComponentAnalysis(String transformFile){
		initialize(transformFile);
	}
	
	private void initialize(String transformFile){
		fileName = transformFile;
		readPCA();
	}
	
	/**
	 * Read the transformation Matrix from file
	 */
	private void readPCA(){
		
		Log.v("PCA", "Reading File "+fileName);
		DataInputStream dis = FileUtils.readBinaryFile(fileName);
		
		mean = FileUtils.readFloatArray(dis);
		trasformMatrix = FileUtils.readFloatMatrix(dis);
		
	}
	
	/**
	 * PCA transform (Mean remove and Matrix multiplication).
	 * Store in feats var.
	 * @param a One dimension Matrix containing image.
	 */
	private void transform(float a[]){
		
		Log.v("PCA", "PCA transform.");
		//remove mean
		for (int i = 0; i < a.length; i++) {
			a[i]-=mean[i];
		}
		
		Log.v("PCA", "Mean removed.");
		
		int nrow = trasformMatrix.length;
		int ncol = trasformMatrix[0].length;
		
		assert(nrow==a.length):"Matrix multiplication dimensions must agree.";
		
		if(feats==null){
			feats = new float[ncol];
		}
		
		for (int i = 0; i < feats.length; i++) {
			feats[i]=0.0f;
		}
		
		Log.v("PCA", "Matrix multiplication");

		//Log.v("PCA", ""+a.length);
		//matrix multiplication
		for (int k = 0; k < nrow; k++) {
			for (int j = 0; j < ncol; j++) {
				feats[j]+=a[k]*trasformMatrix[k][j];
			}
			//Log.v("PCA", ""+k + " of "+a.length + " and " + trasformMatrix.length);
		}
		//Log.v("PCA", "Transform applied.");
		
	}
	


	public Collection<Double> getDescriptor(Bitmap img) {
		// TODO Auto-generated method stub
		
		time = System.currentTimeMillis();
		
		Bitmap resImg = FaceImage.resizeBitmap(img, WIDTH/img.getWidth(), HEIGHT/img.getHeight());
		img.recycle();
		
		Log.v("PCA", "Img size: "+resImg.getWidth() + " " + resImg.getHeight());
		
		int lenght = resImg.getWidth()*resImg.getHeight();
		
		if(rgbpixels==null || rgbpixels.length<lenght)
			rgbpixels = new int[lenght];
		if(pixels==null || pixels.length<lenght)
			pixels = new float[lenght];
		
		FaceImage.getPixelArray(resImg, rgbpixels);
		
		Log.v("PCA", ""+pixels.length);
				
		for (int i = 0; i < rgbpixels.length; i++) {
			pixels[i] = (float)FaceImage.rgb2gray(rgbpixels[i]);
		}
		
		transform(pixels);
		
		LinkedList<Double> descriptor = new LinkedList<Double>();
		for (double d : feats) {
			descriptor.addLast(d);
		}
		
		time = System.currentTimeMillis() - time;
		
		return descriptor;
	}
	

	public double timeElapsed() {
		// TODO Auto-generated method stub
		return ((double)(time))/1000.0;
	}

	public double distance(Collection<Double> c1, Collection<Double> c2) {
		// TODO Auto-generated method stub
		return MathUtils.euclideanDistance(c1, c2);
	}

}
