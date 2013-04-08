package com.example.androideye;

import java.io.File;

import android.os.Environment;

/**
 * Store some global constants.
 * @author Alan Zanoni Peixinho
 *
 */
public class Globals {
	public static final File APP_DIR = new File(Environment.getExternalStorageDirectory(), "AndroidEye");
	public static final File BASE_DIR = new File(APP_DIR, "Database");
	
	public class PersistentInfo
	{
		public static final String PREFERENCES_FILE = "AndroidEye.PersistentInfo";
		public static final String HAS_STARTED = "HasStarted";
		public static final String INSTALLED_EXTRA = "InstalledExtraData";
	}
}
