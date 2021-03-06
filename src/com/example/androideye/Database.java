package com.example.androideye;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import android.util.Log;
import android.util.Pair;

/**
 * Manipulate the Android Eye database.
 * @author Alan Zanoni Peixinho
 *
 */
public class Database {
	
	/*
	 /mnt/sdcard/AndroidEye/Database
	 * */
	public static String BASE_DIR = Globals.BASE_DIR.getAbsolutePath();
	
	static{
		File d = new File(BASE_DIR);
		if(!d.exists()){
			d.mkdirs();
		}
	}
	
	
	/**
	 * List the people present in the database.
	 * @return Return a list with the people present in database.
	 */
	public static List<File> listPeople()
	{
		File dirlist= new File(BASE_DIR);
		Log.v("Database", dirlist.getName());
    	File l[]=dirlist.listFiles();
    	LinkedList<File> people = new LinkedList<File>();
    	
    	for (File file : l) {
        	if(file.isDirectory()){
        		people.addLast(file);
        	}
        }
    	
    	return people;
	}
	
	static boolean changeBaseDir(String baseDir){
		return changeBaseDir(new File(baseDir));
	}
	
	/**
	 * Change the database used in AndroidEye
	 * @param baseDir The new database directory
	 * @return 
	 * @return True in success case, or false if the directory doesn't exist
	 */
	static boolean changeBaseDir(File baseDir){
		if(baseDir.isDirectory()){
			BASE_DIR = baseDir.getAbsolutePath();
			return true;
		}
		else{
			return false;
		}
	}
	
	/**
	 * Split a list of images. Used to generate experiments database.
	 * @param img List of images.
	 * @param perc1 [0.0,1.0] The first list has perc1 of the images. The second list has (1-perc1) of the images. 
	 * @return Return a pair containing the two lists.
	 */
	public static Pair<LinkedList<File>, LinkedList<File> > split(List<File> img, double perc1){
		LinkedList<File> tempList = new LinkedList<File>(img);
		Collections.shuffle(tempList);
		
		LinkedList<File> set1 = new LinkedList<File>();
		LinkedList<File> set2 = new LinkedList<File>();
		
		int sz = img.size();
		
		int i;
		for (i = 0; i < sz*perc1; i++) {
			File f = tempList.removeFirst();
			set1.addLast(f);
		}
		
		for (;  i< sz; i++) {
			File f = tempList.removeFirst();
			set2.addLast(f);
		}
		
		return new Pair<LinkedList<File>, LinkedList<File>>(set1, set2);
	}
	
	/**
	 * Clear the database.
	 */
	public void clear(){
		File dir = new File(BASE_DIR);
		
		File list[] = dir.listFiles();
		
		for (File file : list) {
			file.delete();
		}
	}
	
	/**
	 * List all images of a person.
	 * @param personId ID (label) of person.
	 * @return Return a list with all images of <i>personId</i>.
	 */
	public static List<File> listImages(String personId)
    {
		File dir = new File(BASE_DIR, personId);
		return listImages(dir);
    }
	
	/**
	 * List all images of a person.
	 * @param personId ID (label) of person.
	 * @return Return a list with all images of <i>personId</i>.
	 */
	public static List<File> listImages(File personDir)
    {
		LinkedList<File> img = new LinkedList<File>();
		File l2[]=personDir.listFiles();
        for (File file2 : l2) {
        		
        		if(file2.getName().endsWith(".jpg") ){
        			img.addLast(file2);
        		}
		}
		return img;
    }
	
	/**
	 * Get the name of a person.
	 * @param personDir personId ID (label) of person.
	 * @return Return the real name of <i>personId</i>
	 */
	public static String personName(File personDir){
		String s = null;
		File f = new File(personDir, "name.txt");
		try {
			BufferedReader br = new BufferedReader(new FileReader(f));
			s = br.readLine();
			br.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			s = null;
		}
		
		return s;
	}
	

	/**
	 * Get the name of a person.
	 * @param personDir personId ID (label) of person.
	 * @return Return the real name of <i>personId</i>
	 */
	public static String personName(String personId){
		File dir = new File(BASE_DIR, personId);
		return personName(dir);
	}
}
