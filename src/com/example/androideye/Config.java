package com.example.androideye;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class Config {

	String appDir = null;
	String descriptor = null;
	String detector = null;
	int quality;
	
	File f = null;
	
	public Config(String appDir) {
		// TODO Auto-generated constructor stub
		this.appDir = new String(appDir);
		f = new File(appDir, "config.txt");
	}
	
	public boolean saveConfiguration(String detector, String descriptor, int quality){
		
		
		try {
			f.delete();
			f.createNewFile();
			
			FileWriter fw = new FileWriter(f);
			
			fw.write(detector); fw.write('\n');
			fw.write(descriptor); fw.write('\n');
			fw.write(""+quality); fw.write('\n');
			
			fw.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return true;
	}
	
	public void loadConfiguration(){
		try {
			BufferedReader fr = new BufferedReader(new FileReader(f));
			
			detector = fr.readLine();
			descriptor = fr.readLine();
			quality = Integer.parseInt(fr.readLine());
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String getDescriptor(){
		return descriptor;
	}
	
	public String getDetector(){
		return detector;
	}
	
	public int getQuality(){
		return quality;
	}

}
