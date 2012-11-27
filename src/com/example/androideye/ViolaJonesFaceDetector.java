package com.example.androideye;

import java.util.LinkedList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Rect;


public class ViolaJonesFaceDetector implements FaceDetect {

	
	LinkedList<Rect> faces;
	long time;
	
	public ViolaJonesFaceDetector()
	{
        OpenCV.initFaceDetection("/mnt/sdcard/haarcascade_frontalface_alt.xml");
        
        faces = new LinkedList<Rect>();
        time = 0;
	}
	
	@Override
	public List<Rect> findFaces(Bitmap img) {
		
		int width, height;
		faces.clear();
		
		width = img.getWidth();
    	height = img.getHeight();
    	int img_data[] = new int[width*height];
    	
    	img.getPixels(img_data, 0, width, 0, 0, width, height);
    	
    	OpenCV.setSourceImage(img_data, width, height);
    	
    	time = System.currentTimeMillis();
    	Rect[] r = OpenCV.findAllFaces();
    	time = System.currentTimeMillis() - time;
    	
    	if(r!=null){
    		for (int i = 0; i < r.length; i++) {
    			faces.add(r[i]);
    		}
    	}
    	
		// TODO Auto-generated method stub
		return faces;
	}
	
	public int normalSize()
	{
		return 30*30;
	}

	public Bitmap normalizeSize(Bitmap b)
	{
		return FaceImage.resizeBitmap(b, 30, 30);
	}

	@Override
	public double timeElapsed() {
		// TODO Auto-generated method stub
		return ((double)time)/1000;
	}

}
