package com.example.androideye;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

/**
 *  Get information of the phone contacts.
 * @author Everton Fernandes da Silva
 *
 */
public class ContactPicker {
	
	ContentResolver resolver = null;
	Cursor cursor = null;
	private final int PICK_CONTACT = 1;
	Intent intentContact = null;
	int cont;
	
	int maxSamples;
	private BufferedReader r1;
	private BufferedReader r2;
	
	
	/**
	 * Creates an instance of ContactPicker.
	 * @param cr ContentResolver.
	 * @param maxSamples Maximum number of samples stored in database.
	 */
	public ContactPicker(ContentResolver cr, int maxSamples) {
		// TODO Auto-generated constructor stub
  		resolver = cr;
  		
  		this.maxSamples = Math.max(maxSamples, 1);
  		
  		Log.v("ContactPicker", "Creating intent");
  		intentContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
  		
  		Log.v("ContactPicker", "Getting uri");
    	Uri uri = ContactsContract.Contacts.CONTENT_URI;
    	String[] projection = new String[] {
    			ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME };
    	String selection = ContactsContract.Contacts.IN_VISIBLE_GROUP;
        String[] selectionArgs = null;
        String sortOrder = ContactsContract.Contacts.DISPLAY_NAME;
    	
        Log.v("ContactPicker", "OK");
        cont = 0;
        
    	//Cursor cursor =  managedQuery(intent.getData(), null, null, null, null);
    	//Cursor cursor =  resolver.query(intent.getData(), null, null, null, null);
    	
        Log.v("cp", "Executing query ...");
        cursor =  resolver.query(uri, projection, selection, selectionArgs, sortOrder);
        Log.v("cp", "OK");
	}
	
	/**
	 * Call the next contact.
	 * @return Return <code>false</code> if the contact list is over.
	 */
	public boolean nextContact(){
		
		if(cont==0){
			if(!cursor.moveToFirst()){
				return false;
			}
		}
		long contactId = cursor.getLong(cursor.getColumnIndex(ContactsContract.Contacts._ID));
		
    	String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
	    loadContactPhoto(resolver,contactId);
	    
	    File dir = new File(Globals.BASE_DIR, Long.toString(contactId));
	    File nameFile = new File(dir, "name.txt");
	    
	    if(!nameFile.exists())
	    {
	    	try {
				nameFile.createNewFile();
				BufferedWriter out = new BufferedWriter(new FileWriter(nameFile));
				out.write(name);
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	    
	    Log.v("ContactPicker", name);
	    
	    cont++;
	    
		return cursor.moveToNext();
	}
	
	/**
	 * Get the index of current contact.
	 * @return Return the index of the current contact.
	 */
	public int curContact(){
		return cursor.getPosition();
	}
	
	/**
	 * Get the number of contacts.
	 * @return Return the number of contacts.
	 */
	public int contactsNumber(){
		return cursor.getCount();
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent intent) 
    {

      if (requestCode == PICK_CONTACT)
      {         
			getContacts(intent);          
    	  	//getContactPhoto();
      }
    }

	/**
	 * Load a contact photo and store in the database.
	 * @param cr
	 * @param id
	 */
    private void loadContactPhoto(ContentResolver cr, long id) 
    {
        Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id);
        InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(cr, uri);
        
        if (input == null) {
             return;
         }
        
        try {
        	
        	File d = new File(Globals.BASE_DIR, Long.toString(id));
        	
        	if (!d.exists()) {
				d.mkdir();
			}
        	
        	long time = System.currentTimeMillis();
        	File f = new File(d, Long.toString(time)+".jpg");
        	
        	if(!f.exists()){
        		f.createNewFile();
        	}
        	
			FileOutputStream fout = new FileOutputStream(f);
			BitmapFactory.decodeStream(input).compress(Bitmap.CompressFormat.JPEG, 100, fout);
	    	fout.flush();
			fout.close();
			
			//delete repeated files
			File[] filelist = d.listFiles();
			
			int count = filelist.length;
			
			//remove duplicates
			for (File file : filelist) {
				if(!f.equals(file)){
					if(equalContent(f, file))
						file.delete();
					count--;
				}
			}
			
			filelist = d.listFiles();
			count = filelist.length;
			
			//if exceeds the sample limit it removes the oldest sample
			if(count>maxSamples){
				File oldest = null;
				long timeoldest = System.currentTimeMillis();
				for (File file : filelist) {
					long t = file.lastModified();
					if(t<timeoldest){
						timeoldest = t;
						oldest = file;
					}
				}
				oldest.delete();
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
    }
    
    
    /**
     * Compare if two files have the same content.
     * @param f1 First file.
     * @param f2 Second file.
     * @return Return <code>true</code> if the files are equal.
     */
    private boolean equalContent(File f1, File f2){
    	
    	try {
			r1 = new BufferedReader(new FileReader(f1) );
			r2 = new BufferedReader(new FileReader(f2) );
	    	
			String s1, s2;
			
	    	for(;;){
	    		s1 = r1.readLine();
	    		s2 = r2.readLine();
	    		
	    		if(!s1.equals(s2)){
	    			return false;
	    		}
	    		
	    		if(s1==null || s2==null)
	    			break;
	    	}
	    	
	    	
    	} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}    	
    	
    	
    	return true;
    }
    
    /**
     * Get all contacts and store in the database.
     * @param intent
     */
    private void getContacts(Intent intent)
    //public void getContactPhoto()
    {    	
    	
        //Cursor cursor =  managedQuery(uri, projection, selection, selectionArgs, sortOrder);
    	    	
    	if (cursor.moveToFirst())
    	{
    		
    		
    		do 
		    {   
		    	long contactId = cursor.getLong(cursor.getColumnIndex(ContactsContract.Contacts._ID));
	
		    	String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
			    loadContactPhoto(resolver,contactId);
			    
			    
			    Log.v("ContactPicker", name);
			    
			    cont++;
			    
		    }while(cursor.moveToNext());
    	}
	    
    	Log.d("CONTADOR", ""+cont);
	    cursor.close();
	}   

	
}
