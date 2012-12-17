package com.example.androideye;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.Collection;
import java.util.LinkedList;



public class FileUtils {
	
	public static final int BUFFER_SIZE = 2*1024;//2K

	/**
	 * In the freaking Java we need a 100 code lines to read a simple file.
	 * @param fileName The file to be read.
	 * @return Return the file content.
	 */
	public static String readFile(String fileName){
		try{
			FileReader in = new FileReader(fileName);
	        StringBuilder contents = new StringBuilder();
	        char[] buffer = new char[4096];
	        int read = 0;
	        
	        do {
	            contents.append(buffer, 0, read);
	            read = in.read(buffer);
	        } while (read >= 0);
	        
	        in.close();
	        
	        return contents.toString();
		}catch(Exception e){
			e.printStackTrace();
		}
		//in fail case
		return null;
	}
	
	/**
	 * In the freaking Java we need a 100 code lines to read a simple file.
	 * @param file The file to be read.
	 * @return Return the file content.
	 */
	public static String readFile(File file){
		return readFile(file.getAbsolutePath());
	}
	
	/**
	 * Open a binary file to read.
	 * @param filename File to be opened.
	 * @return Return a data input stream.
	 */
	public static DataInputStream readBinaryFile(String filename){
		try{			
			FileInputStream fis = null;
			BufferedInputStream bis = null;
			DataInputStream filein = null;
			
			System.gc();
			
			fis = new FileInputStream(filename);
			bis = new BufferedInputStream(fis, BUFFER_SIZE);
			filein = new DataInputStream(bis);//buffer-100k
			
			return filein;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;//error
	}
	
	/**
	 * Open a binary file to read.
	 * @param filename File to be opened.
	 * @return Return a data input stream.
	 */
	public static DataInputStream readBinaryFile(File file){
		return readBinaryFile(file.getAbsolutePath());
	}
	
	public static DataOutputStream writeBinaryFile(String filename){
		
		try{
			
			FileOutputStream fos = null;
			BufferedOutputStream bos = null;
			DataOutputStream fileout = null;
			
			System.gc();
			
			fos = new FileOutputStream(filename);
			bos = new BufferedOutputStream(fos, BUFFER_SIZE);
			fileout = new DataOutputStream(bos);//buffer-100k
			
			return fileout;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;//error
	}
	
	public static DataOutputStream writeBinaryFile(File file){
		return writeBinaryFile(file.getAbsolutePath());
	}
	
	public static float[] readFloatArray(DataInputStream dis){
		try{
			int sz = dis.readInt();
			
			float a[] = new float[sz];
			
			for (int i = 0; i < sz; i++) {
				float f = dis.readFloat();
				a[i] = f;
			}
			
			return a;
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return null;//error
	}
	
	public static void writeFloatArray(DataOutputStream dout, float a[]){
		try{
			dout.writeInt(a.length);
			for (float f : a) {
				dout.writeFloat(f);
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static Collection<Double> readFloatCollection(DataInputStream dis){
		try{
			int sz = dis.readInt();
			LinkedList<Double> c = new LinkedList<Double>();
			for (int i = 0; i < sz; i++) {
				float f = dis.readFloat();
				c.addLast((double)f);
			}
			
			return c;
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return null;//error
	}
	
	@SuppressWarnings("unchecked")
	public static void writeFloatCollection(DataOutputStream dout, Collection<Double> c){
		try{
			dout.writeInt(c.size());
			for (Number d : c) {
				dout.writeFloat(d.floatValue());
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static  float[][] readFloatMatrix(DataInputStream dis){
		try{
			int nrows=dis.readInt();
			int ncols=dis.readInt();
			
			float [][]f = new float[nrows][ncols];
			
			for (int i = 0; i < nrows; i++) {
				for (int j = 0; j < ncols; j++) {
					f[i][j] = dis.readFloat();
				}
			}
			
			return f;
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return null;//error
	}
	
	public static  void writeFloatMatrix(DataOutputStream dos, float [][]f){
		int nrows=f.length;
		int ncols=f[0].length;
		try{
			dos.writeInt(nrows);
			dos.writeInt(ncols);
			
			for (int i = 0; i < nrows; i++) {
				for (int j = 0; j < ncols; j++) {
					dos.writeFloat(f[i][j]);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
}
