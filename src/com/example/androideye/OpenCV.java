package com.example.androideye;

import android.graphics.Rect;

/**
 * Wrap some image functions using OpenCV Library.
 * 
 * @author André Marcelo Farina
 */
public class OpenCV {
	static{
		System.loadLibrary("opencv");
	}
	   
    public static native byte[] findContours(int[] data, int w, int h);

    public static native byte[] getSourceImage();

    public static native boolean setSourceImage(int[] data, int w, int h);

    public static native byte[] buscaFace();
    
    public static native boolean initFaceDetection(String cascadePath);

    public static native void releaseFaceDetection();

    public static native boolean highlightFaces();

    public static native Rect[] findAllFaces();

    public static native Rect findSingleFace();
    
    public static native void learn(Object []arr);
    
    public static native void recognize();
    
    public static native int recognizePicture();
    
    public static native void save(int[] photoData, int width, int height, String filePath);
    
    public static native boolean saveImage(int[] photoData, int width, int height, String filePath, boolean census);
    
    public static native boolean buildTrainFile(String dirPath);

}