package com.gncbrown.GetMeBack.Utilities;

import android.util.Log;

public class JavaClassLoader extends ClassLoader {
	private static final String TAG = JavaClassLoader.class.getSimpleName();
	
	public Class getClass(String classBinName){
		Class loadedMyClass = null;
		try {
			
			// Create a new JavaClassLoader 
			ClassLoader classLoader = this.getClass().getClassLoader();
			
			// Load the target class using its binary name
	        loadedMyClass = classLoader.loadClass(classBinName);
	        Log.d(TAG, "Loaded class name: " + loadedMyClass.getName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return loadedMyClass;
	}
}