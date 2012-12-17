package com.example.androideye;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import android.graphics.Bitmap;
import android.util.Log;

/**
 * Local Binary Pattern descriptor.
 * @author Alan Zanoni Peixinho.
 *
 */
public class LocalBinaryPattern implements FaceDescriptor{

	public static final int WIDTH = 75;
	public static final int HEIGHT = 150;
	
	double windowW, windowH;
	long time;
	double feats[];
	int pixels[];
	
	/**
	 * Initialize some variables.
	 * @param windowW Window width.
	 * @param windowH Window height.
	 */
	private void initialize(double windowW, double windowH)
	{
		Log.v("LBP", "Initializing Local Binary Pattern Face Descriptor");
		this.windowW = windowW;
		this.windowH = windowH;
				
		time = 0;
		feats = new double[256];
		pixels = null;
	}
	
	/**
	 * Creates an instance of {@link LocalBinaryPattern} without windows.
	 */
	public LocalBinaryPattern()
	{
		initialize(1.0, 1.0);
	}
	
	/**
	 * Creates an instance of {@link LocalBinaryPattern} with windows.
	 * @param detect Face detector used.
	 * @param windowPercentage Windows size percentage used in descriptor.
	 */
	public LocalBinaryPattern(FaceDetect detect, double windowPercentage) {
		initialize(windowPercentage, windowPercentage);
	}
	
	/**
	 * Creates an instance of {@link LocalBinaryPattern} with windows.
	 * @param detect Face detector used.
	 * @param windowW Window width.
	 * @param windowH Window height.
	 */
	public LocalBinaryPattern(FaceDetect detect, double windowW, double windowH) {
		initialize(windowW, windowH);
	}
	
	/**
	 * Creates an instance of {@link LocalBinaryPattern} with windows.
	 * @param detect Face detector used.
	 */
	public LocalBinaryPattern(FaceDetect detect) {
		// TODO Auto-generated constructor stub
		initialize(1.0, 1.0);//a janela possui o tamanho da imagem
	}
	
	
	/**
	 * Get the (x,y) pixel in the image array.
	 * @param pixels Image array.
	 * @param x Pixel line.
	 * @param y Pixel Column.
	 * @param width Image width.
	 * @return Return the pixel value at (x,y).
	 */
	private int getPixel(int pixels[], int x, int y, int width)
	{
		return pixels[y*width+x];
	}
	
	/**
	 * Extract Local Binary Pattern histogram in the image.
	 * @param img Image to be used.
	 * @return Return the Local Binary Pattern Histogram.
	 */
	
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
			pixels[i] = (int) FaceImage.rgb2gray(pixels[i]);
			//Log.v("LBP", "pixel["+i+"] = " + pixels[i]);
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

	public Collection<Double> getDescriptor(Bitmap img) {
		// TODO Auto-generated method stub
		
		Bitmap resImg = FaceImage.resizeBitmap(img, (double)WIDTH/img.getWidth());
		
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

	public double timeElapsed() {
		// TODO Auto-generated method stub
		return ((double)time)/1000;
	}
	
	public double distance(Collection<Double> c1, Collection<Double> c2) {
		// TODO Auto-generated method stub
		return MathUtils.chiSquareDistance(c1, c2);
	}

}
