package com.example.androideye;

import java.io.File;
import java.util.Collection;
import java.util.List;

import android.graphics.Bitmap;
import android.util.Pair;


public abstract class FaceDescriptor {

	public abstract void setDetector(FaceDetect detector);
	public abstract Pair<List<String>, List<Collection<Double>> > train(Collection<String> id, Collection<File> trainSet);
	public abstract Collection<Double> getDescriptor(Bitmap img);
	public abstract double timeElapsed();
	
	public abstract double distance(Collection<Double> c1, Collection<Double> c2);
}
