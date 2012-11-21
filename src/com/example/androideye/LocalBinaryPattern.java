package com.example.androideye;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.Log;
import android.util.Pair;


public class LocalBinaryPattern extends FaceDescriptor{

	public static final int WIDTH = 100;
	public static final int HEIGHT = 200;
	
	double windowW, windowH;
	long time;
	double feats[];
	int pixels[];
	FaceDetect detector = null;
	
	private void initialize(double windowW, double windowH)
	{
		Log.v("LBP", "Initializing Local Binary Pattern Face Descriptor");
		this.windowW = windowW;
		this.windowH = windowH;
				
		time = 0;
		feats = new double[256];
		pixels = null;
	}
	
	public LocalBinaryPattern()
	{
		setDetector(new SkinFaceDetector());
		initialize(1.0, 1.0);
	}
	
	public LocalBinaryPattern(FaceDetect detect, double windowPercentage) {
		setDetector(detect);
		initialize(windowPercentage, windowPercentage);
	}
	
	public LocalBinaryPattern(FaceDetect detect, double windowW, double windowH) {
		setDetector(detect);
		initialize(windowW, windowH);
	}
	
	public LocalBinaryPattern(FaceDetect detect) {
		// TODO Auto-generated constructor stub
		setDetector(detect);
		initialize(1.0, 1.0);//a janela possui o tamanho da imagem
	}
	
	@Override
	public Pair<List<String>, List<Collection<Double>> > train(Collection<String> id, Collection<File> trainSet) {
		// TODO Auto-generated method stub
		
		LinkedList<Collection<Double> > list = new LinkedList<Collection<Double>>();
		LinkedList<String> listId = new LinkedList<String>();
		
		Iterator<String> it = id.iterator();
		
		for (File file : trainSet) {
			Log.v("LBP", "Extracting LBP of "+file.getName());
			Bitmap b = FaceImage.loadImage(file);
			b = FaceImage.resizeBitmap(b, 0.5, 0.5);
			String s = it.next();
			List<Rect> r = detector.findFaces(b);
			
			if(r.size()==1)
			{
				b = FaceImage.cropFace(b, r.get(0));
				listId.addLast(s);
				list.addLast(getDescriptor(b));
			}
		}
		
		return new Pair<List<String>, List<Collection<Double>> > (listId, list);
	}
	
	private int gray(int color)
	{
		return (int) ((0.2126*Color.red(color)+0.7152*Color.green(color)+0.0722*Color.blue(color))%256);
	}
	
	private int getPixel(int pixels[], int x, int y, int width)
	{
		return pixels[y*width+x];
	}
	
	private Collection<Double> lbp(Bitmap img){
		
		for(int i=0; i<feats.length; ++i)
			feats[i] = 0.0;
	
		if(pixels==null || pixels.length<(img.getHeight()*img.getWidth()))
		{
			pixels = new int[img.getWidth()*img.getHeight()];
		}
		
		int width = img.getWidth();
		int height = img.getHeight();
		
		long t = System.currentTimeMillis();
		img.getPixels(pixels, 0, width, 0, 0, width, height);
		t = System.currentTimeMillis() - t;
		
		Log.v("getdata", ""+(double)(t)/1000.0);
		
		for (int i = 0; i < pixels.length; i++) {
			pixels[i] = gray(pixels[i]);
		}
		
		for(int i=1; i<width-1; ++i)
		{
			for (int j = 1; j < height-1 ; ++j) {
				
				int output = 0;
				int cur = getPixel(pixels, i, j, width);
				
				if (getPixel(pixels, i-1, j-1, width)>=cur) {
					output = 1;
				}
				else
				{
					output = 0;
				}
				
				if (getPixel(pixels, i, j-1, width)>=cur) {
					output = (output << 1) + 1;
				}
				else
				{
					output = (output << 1) + 0;
				}
				
				if (getPixel(pixels, i+1, j-1, width)>=cur) {
					output = (output << 1) + 1;
				}
				else
				{
					output = (output << 1) + 0;
				}
				
				if (getPixel(pixels, i+1, j, width)>=cur) {
					output = (output << 1) + 1;
				}
				else
				{
					output = (output << 1) + 0;
				}
				
				if (getPixel(pixels, i+1, j+1, width)>=cur) {
					output = (output << 1) + 1;
				}
				else
				{
					output = (output << 1) + 0;
				}
				
				if (getPixel(pixels, i, j+1, width)>=cur) {
					output = (output << 1) + 1;
				}
				else
				{
					output = (output << 1) + 0;
				}
								
				if (getPixel(pixels, i-1, j+1, width)>=cur) {
					output = (output << 1) + 1;
				}
				else
				{
					output = (output << 1) + 0;
				}
				
				if (getPixel(pixels, i-1, j, width)>=cur) {
					output = (output << 1) + 1;
				}
				else
				{
					output = (output << 1) + 0;
				}
				feats[output]++;
			}
		}
		
		double sum = 0.0;
		
		for (int k = 0; k < feats.length; k++) {
			sum+=feats[k];
		}
		
		//normalize array to work with images in different sizes
		for (int k = 0; k < feats.length; k++) {
			feats[k]/=sum;
		}
		
		

		LinkedList<Double> list = new LinkedList<Double>();
		
		for (int i = 0; i < feats.length; i++) {
			list.addLast(feats[i]);
		}

		return list;
	}

	@Override
	public Collection<Double> getDescriptor(Bitmap img) {
		// TODO Auto-generated method stub
		
		Bitmap resImg = FaceImage.resizeBitmap(img, WIDTH/img.getWidth());
		
		if(windowW==1.0 && windowH==1.0)//if the window size is 1.0 is not necessary allocate new Bitmaps
		{
			Log.v("LBP", "Run Local Binary Pattern without window");
			
			time = System.currentTimeMillis();
			Collection<Double> it = lbp(resImg);
			time = System.currentTimeMillis() - time;
			
			return it;
		}
		
		time = System.currentTimeMillis();		
		int wInc = (int)(resImg.getWidth()*windowW);
		int hInc = (int)(resImg.getHeight()*windowH);
		
		Log.v("LBP", String.format("Run Local Binary Pattern with windowSize (%d, %d)",wInc, hInc));
		LinkedList<Double> features = new LinkedList<Double>();
		
		for (int i = 0; i+wInc <= resImg.getWidth(); i+=wInc) {
			for (int j = 0; j+hInc <= resImg.getHeight(); j+=hInc) {
				//long t = System.currentTimeMillis();
				Bitmap subImg = Bitmap.createBitmap(resImg, i, j, wInc, hInc);
				//t = System.currentTimeMillis() - t;
				//Log.v("Subimage", ""+(double)(t)/1000.0);
				Iterable<Double> it = lbp(subImg);
				subImg.recycle();
				for (Double double1 : it) {
					features.addLast(double1);
				}
			}
		}
		
		time = System.currentTimeMillis() - time;
		
		resImg.recycle();
		
		return features;
	}

	@Override
	public double timeElapsed() {
		// TODO Auto-generated method stub
		return ((double)time)/1000;
	}

	public void setDetector(FaceDetect f) {
		// TODO Auto-generated method stub
		detector = f;
	}
	
	//chi-square distance between samples
	private double chiSquareDistance(Collection<Double> i1, Collection<Double> i2){
		
		double dist = 0.0;
		double diff;
		double v1, v2;
		double sum;
		assert(i1.size()==i2.size()):"Nao tem o mesmo numero de caracteristicas";
		
		Iterator<Double> it1 = i1.iterator();
		Iterator<Double> it2 = i2.iterator();
		
		//Log.v("Info", String.format("Features Number - distance function: %d %d", i1.size(), i2.size()));
		
		int nFeatures = i1.size();
		for (int i = 0; i < nFeatures; i++)
		{
		   v1 = it1.next();
		   v2 = it2.next();
		   
		   diff = v1 - v2;
		   sum = v1 + v2;
		   
		   //Log.v("Chi Square", String.format("%f, %f", v1, v2));
		   
		   if(sum>0.0)
			   dist += (diff*diff)/sum;
		}//while
		
		return 0.5*dist;
	}

	@Override
	public double distance(Collection<Double> c1, Collection<Double> c2) {
		// TODO Auto-generated method stub
		return chiSquareDistance(c1, c2);
	}

}
