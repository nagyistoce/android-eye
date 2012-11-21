package com.example.androideye;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;


public class FaceImage {
	
	/*static{
		options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		
	}
	*/
	
	public static Bitmap loadImage(String filename)
	{
		
		Bitmap b = BitmapFactory.decodeFile(filename);
		return b;
	}
	
	public static Bitmap loadImage(File f)
	{
		return loadImage(f.getAbsolutePath());
	}
	
	public static Bitmap resizeBitmap(Bitmap bm, double scale) {
		return resizeBitmap(bm, scale, scale);
	}
	
	public static Bitmap resizeBitmap(Bitmap bm, double scaleWidth, double scaleHeight) {
		int width = bm.getWidth();
		int height = bm.getHeight();
		
		// create a matrix for the manipulation
		Matrix matrix = new Matrix();
	
		// resize the bit map	
		matrix.postScale((float)scaleWidth, (float)scaleHeight);
	
		Log.v("Scale","Matrix ready");
		// recreate the new Bitmap
		Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
		
		Log.v("Scale", "Bitmap resized");
		
		return resizedBitmap;
	}
	
	public static Rect resizeRect(Rect r, double scale){
		
		return new Rect((int)(r.left*scale), (int)(r.top*scale), (int)(r.right*scale), (int)(r.bottom*scale));
	}
	
	public static void saveImage(Bitmap b, String filename)
	{
		saveImage(b, new File(filename));
	}
	
	public static void saveImage(Bitmap bitmap, File file) {
		// TODO Auto-generated method stub
		
		if(file.exists()){
			try {
				file.delete();
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		//uma saga epica para apenas salvar a imagem apos marcar a face
			try {
				FileOutputStream fout = new FileOutputStream(file);
		    	bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fout);
		    	fout.flush();
				fout.close();
		    	
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	
	public static Bitmap cropFace(Bitmap b, Rect r)
	{
		Log.v("Croping", "Rect: ("+r.left+", "+r.top+") ("+r.right+", "+r.bottom+")");
		Log.v("Croping", "Image: ("+b.getWidth()+", "+b.getHeight()+")");
		
    	return Bitmap.createBitmap(b, r.left, r.top, r.width(), r.height());
	}
	
	public static Bitmap drawRect(Bitmap b, Rect r, int color){
		
		Bitmap bitmap = Bitmap.createBitmap(b.getWidth(), b.getHeight(), b.getConfig());
		Canvas canvas = new Canvas(bitmap);
		canvas.drawBitmap(b, new Matrix(), null);
		
		Paint paint = new Paint();
        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        
		canvas.drawRect(r, paint);
		
		return bitmap;
	}
	
	public static Bitmap drawRects(Bitmap b, Collection<Rect> r, int color){
		
		Bitmap bitmap = Bitmap.createBitmap(b.getWidth(), b.getHeight(), b.getConfig());
		Canvas canvas = new Canvas(bitmap);
		canvas.drawBitmap(b, new Matrix(), null);
		
		Paint paint = new Paint();
        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        
		for (Iterator<Rect> iterator = r.iterator(); iterator.hasNext();) {
			Rect rect = (Rect) iterator.next();
			canvas.drawRect(rect, paint);
		}
		
		return bitmap;
	}

}
