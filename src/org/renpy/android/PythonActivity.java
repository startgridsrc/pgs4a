package org.renpy.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ActivityNotFoundException;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.view.MotionEvent;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;
import android.util.Log;
import android.util.DisplayMetrics;
import android.os.Debug;


import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.File;
import java.io.IOException;
import java.nio.channels.*;

import java.util.zip.GZIPInputStream;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class PythonActivity extends Activity implements Runnable {

    // The audio thread for streaming audio...
    private static AudioThread mAudioThread = null;

    // The SDLSurfaceView we contain.
    public static SDLSurfaceView mView = null;
	public static PythonActivity mActivity = null;
	public static String mExpansionFile = null;
	
    // Did we launch our thread?
    private boolean mLaunchedThread = false;

    private ResourceManager resourceManager;

    // The path to the directory contaning our external storage.
    File externalStorage;
    File oldExternalStorage;

    // The path to the directory containing the game.
    private File mPath = null;

    boolean _isPaused = false;

    // store string of file, in case we opened a txt file using the file picker intent openFile()
    private String fileUriString;
    private String fileContentString;
    private static final int PICK_TXT_FILE = 42;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Hardware.context = this;

		this.mActivity = this;

        getWindowManager().getDefaultDisplay().getMetrics(Hardware.metrics);

        resourceManager = new ResourceManager(this);
        
        mPath = getFilesDir();

        // go to fullscreen mode
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                             WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Start showing an SDLSurfaceView.
        mView = new SDLSurfaceView(
            this,
            mPath.getAbsolutePath());
        Hardware.view = mView;

        setContentView(mView);
		getWindow().setBackgroundDrawableResource(android.R.color.black);
		if (android.os.Build.VERSION.SDK_INT>=19){
			mView.setSystemUiVisibility(SDLSurfaceView.SYSTEM_UI_FLAG_FULLSCREEN | SDLSurfaceView.SYSTEM_UI_FLAG_HIDE_NAVIGATION|SDLSurfaceView.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
		}
		if (android.os.Build.VERSION.SDK_INT>=24){
			getWindow().setDecorCaptionShade(Window.DECOR_CAPTION_SHADE_DARK);
		}
	}

    /**
     * Show an error using a toast. (Only makes sense from non-UI
     * threads.)
     */
    public void toastError(final String msg) {

        final Activity thisActivity = this;

        runOnUiThread(new Runnable () {
                public void run() {
                    Toast.makeText(thisActivity, msg, Toast.LENGTH_LONG).show();
                }
            });

        // Wait to show the error.
        synchronized (this) {
            try {
                this.wait(1000);
            } catch (InterruptedException e) {
            }
        }
    }

    public void recursiveDelete(File f) {
        if (f.isDirectory()) {
            for (File r : f.listFiles()) {
                recursiveDelete(r);
            }
        }
        f.delete();
    }


    /**
     * This determines if unpacking one the zip files included in
     * the .apk is necessary. If it is, the zip file is unpacked.
     */
    public void unpackData(final String resource, File target) {

    /**
    	 * Delete main.py unconditionally.
     */
    	new File(target, "main.py").delete();

    	
        // The version of data in memory and on disk.
        String data_version = resourceManager.getString(resource + "_version");
        String disk_version = null;

        // If no version, no unpacking is necessary.
        if (data_version == null) {
            return;
        }

        // Check the current disk version, if any.
        String filesDir = target.getAbsolutePath();
        String disk_version_fn = filesDir + "/" + resource + ".version";

        try {
            byte buf[] = new byte[64];
            InputStream is = new FileInputStream(disk_version_fn);
            int len = is.read(buf);
            disk_version = new String(buf, 0, len);
            is.close();
        } catch (Exception e) {
            disk_version = "";
        }

        // If the disk data is out of date, extract it and write the
        // version file.
        if (! data_version.equals(disk_version)) {
            Log.v("python", "Extracting " + resource + " assets.");

            // recursiveDelete(target);
            target.mkdirs();

            AssetExtract ae = new AssetExtract(this);
            if (!ae.extractTar(resource + ".mp3", target.getAbsolutePath())) {
			AssetExtract2 ae2 = new AssetExtract2(this);
            	if (!ae2.extractTar(resource + ".mp3", target.getAbsolutePath())) {
                	toastError("Could not extract " + resource + " data.");
            	}
		}

            try {
                // Write .nomedia.
                new File(target, ".nomedia").createNewFile();

                // Write version file.
                FileOutputStream os = new FileOutputStream(disk_version_fn);
                os.write(data_version.getBytes());
                os.close();
            } catch (Exception e) {
                Log.w("python", e);
				toastError("Error 27.");

            }
        }

    }

    public void run() {
    
        unpackData("private", getFilesDir());

        try {
			Log.i("python", "start loading libraries");
			System.loadLibrary("sdl");
        	System.loadLibrary("sdl_image");
        	System.loadLibrary("sdl_ttf");
        	System.loadLibrary("sdl_mixer");
			System.loadLibrary("python2.7");
			System.loadLibrary("pymodules");
        	System.loadLibrary("application");
        	System.loadLibrary("sdl_main");
			Log.i("python", "libraries loaded");
		} catch(UnsatisfiedLinkError e) {
			Log.i("python", "failed loading libraries");
			toastError("Error 28.");
		}

        try {
	    	System.load(getFilesDir() + "/lib/python2.7/lib-dynload/_io.so");
            System.load(getFilesDir() + "/lib/python2.7/lib-dynload/unicodedata.so");
            System.load(getFilesDir() + "/lib/python2.7/lib-dynload/_imaging.so");
            System.load(getFilesDir() + "/lib/python2.7/lib-dynload/_imagingft.so");
            System.load(getFilesDir() + "/lib/python2.7/lib-dynload/_imagingmath.so");
        } catch(UnsatisfiedLinkError e) {
        }

        if ( mAudioThread == null ) {
            Log.i("python", "starting audio thread");
            mAudioThread = new AudioThread(this);
        }

        runOnUiThread(new Runnable () {
        	public void run() {
            	mView.start();
            }
        });
    }

    @Override
    protected void onStop() {
        _isPaused = true;
        super.onStop();

        if (mView != null) {
            mView.onStop();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        _isPaused = false;

        if (!mLaunchedThread) {
            mLaunchedThread = true;
            new Thread(this).start();
        }

        if (mView != null) {
            mView.onStart();
			if (android.os.Build.VERSION.SDK_INT>=19){
	    		mView.setSystemUiVisibility(SDLSurfaceView.SYSTEM_UI_FLAG_FULLSCREEN |
				SDLSurfaceView.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
				SDLSurfaceView.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
			}
        }
    }

    public boolean isPaused() {
        return _isPaused;
    }

	public void openFile() {
    	Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
    	intent.addCategory(Intent.CATEGORY_OPENABLE);
    	intent.setType("text/plain");
    	startActivityForResult(intent, PICK_TXT_FILE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	if (requestCode == PICK_TXT_FILE && resultCode == Activity.RESULT_OK) {
        	Uri uri = null;
        	if (data != null) {
            	uri = data.getData();
            	fileUriString = uri.toString(); // Store the URI string
            
            	// Read the contents of the file
            	StringBuilder fileContent = new StringBuilder();
            	try (InputStream inputStream = getContentResolver().openInputStream(uri);
                 	BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                		String line;
                		while ((line = reader.readLine()) != null) {
                    		fileContent.append(line).append('\n');
                		}
            	} catch (IOException e) {
                	e.printStackTrace();
            	}

            	// Store the file contents in a variable
            	fileContentString = fileContent.toString();
        	}
    	}
	}

	public String getFileContentString() {
    	return fileContentString;
	}

	public void resetFileContentString() {
    	fileContentString = null;
	}

	protected void onDestroy() {
		if (mView != null) {
			mView.onDestroy();
		}
		//Log.i(TAG, "on destroy (exit1)");
        System.exit(0);
	}
}

