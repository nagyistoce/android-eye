package com.example.androideye;

import java.io.File;
import java.util.Collection;
import java.util.List;

import android.graphics.Bitmap;
import android.util.Pair;

/**
 * Face Descriptor interface.
 * @author Alan Zanoni Peixinho
 *
 */
public abstract class FaceDescriptor {

	public abstract void setDetector(FaceDetect detector);
	public abstract Pair<List<String>, List<Collection<Double>> > train(Collection<String> id, Collection<File> trainSet);
	
	/**
	 * Extract descriptor from a face image.
	 * @param img Face image.
	 * @return Return the face descriptor.
	 */
	public abstract Collection<Double> getDescriptor(Bitmap img);
	
	/**
	 * Time spent in features extraction.
	 * @return Return the time elapsed in the feature extraction.
	 */
	public abstract double timeElapsed();
	
	/**
	 * Compute the distance between descriptor samples.
	 * @param c1
	 * @param c2
	 * @return
	 */
	public abstract double distance(Collection<Double> c1, Collection<Double> c2);
}
