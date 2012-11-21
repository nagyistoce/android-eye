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


public class Database {
	
	/*
	 /mnt/sdcard/AndroidEye/Database
	 * */
	public static String BASE_DIR = new File(UserInterface.APP_DIR, "Database").getAbsolutePath();
	
	static{
		File d = new File(BASE_DIR);
		if(!d.exists()){
			d.mkdirs();
		}
	}
	
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
	
	public static Pair<LinkedList<File>, LinkedList<File> > split(List<File> img, double perc1){
		LinkedList<File> tempList = new LinkedList<File>(img);
		Collections.shuffle(tempList, new Random(42));
		
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
	
	public void clear(){
		File dir = new File(BASE_DIR);
		
		File list[] = dir.listFiles();
		
		for (File file : list) {
			file.delete();
		}
	}
	
	public static List<File> listImages(String personId)
    {
		File dir = new File(BASE_DIR, personId);
		return listImages(dir);
    }
	
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
	
	public static String personName(File personDir){
		String s = null;
		File f = new File(personDir, "name.txt");
		try {
			BufferedReader br = new BufferedReader(new FileReader(f));
			s = br.readLine();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			s = null;
		}
		return s;
	}
	
	public static String personName(String personId){
		File dir = new File(BASE_DIR, personId);
		return personName(dir);
	}
}
