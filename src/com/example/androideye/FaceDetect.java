package com.example.androideye;

import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Rect;


public interface FaceDetect {
	public List<Rect> findFaces(Bitmap img);
	public double timeElapsed();//return the time elapsed in the last face detection
	public int normalSize();
	public Bitmap normalizeSize(Bitmap b);
}
