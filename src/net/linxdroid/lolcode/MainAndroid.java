package net.linxdroid.lolcode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;

import net.linxdroid.lolcode.console.gui.ConsoleActivity;
import net.linxdroid.lolcode.R;
import net.linxdroid.lolcode.editor.Editor;
import net.linxdroid.lolcode.filebrowser.FileManager;
import net.linxdroid.lolcode.filebrowser.SaveFile;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class MainAndroid extends SherlockActivity {
	
	Editor editor;
	String mVersion = "0.01";
	String currentFile = Environment.getExternalStorageDirectory() + "/lolcode/Untitled.lol";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		editor = (Editor)findViewById(R.id.editor);
		this.setTitle("Untitled Document");
		
		Bundle extras = getIntent().getExtras(); 
		if(extras !=null)
		{
			String file = extras.getString("File");
			readFile(file);
			currentFile = file;
			this.setTitle(extras.getString("Name"));
		}
		
		copyFilesToSdCard();
	}
	
	private void copyFilesToSdCard() {
		String version = "0";
		double currentVersion = 0;
		double fileVersion = 0;
		try {
			File myFile = new File(Environment.getExternalStorageDirectory() + "/lolcode/.libs/version.txt");
			FileInputStream fIn = new FileInputStream(myFile);
			BufferedReader myReader = new BufferedReader(
					new InputStreamReader(fIn));
			String aDataRow = "";
			String aBuffer = "";
			while ((aDataRow = myReader.readLine()) != null) {
				aBuffer += aDataRow + "\n";
			}
			version = aBuffer;
			currentVersion = Double.parseDouble(mVersion);
			fileVersion = Double.parseDouble(version);
			myReader.close();
			Toast.makeText(getBaseContext(),
					"Done reading SD 'mysdfile.txt'",
					Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Toast.makeText(getBaseContext(), "Error reading version.txt, making new one.",
					Toast.LENGTH_SHORT).show();
		    copyFileOrDir("");
		}
		if(currentVersion > fileVersion){
			copyFileOrDir("");
		}else if(currentVersion <= fileVersion){
			//Nothing to see here. Move along.
		}
	}

	private void copyFileOrDir(String path) {
	    AssetManager assetManager = this.getAssets();
	    String assets[] = null;
	    try {
	        Log.i("tag", "copyFileOrDir() "+path);
	        assets = assetManager.list(path);
	        if (assets.length == 0) {
	            copyFile(path);
	        } else {
	            String fullPath =  Environment.getExternalStorageDirectory() + "/lolcode/.libs/" + path;
	            Log.i("COPYFILEORDIR", "path="+fullPath);
	            File dir = new File(fullPath);
	            if (!dir.exists() && !path.startsWith("images") && !path.startsWith("sounds") && !path.startsWith("webkit"))
	                if (!dir.mkdirs());
	                    Log.i("tag", "could not create dir "+fullPath);
	            for (int i = 0; i < assets.length; ++i) {
	                String p;
	                if (path.equals(""))
	                    p = "";
	                else 
	                    p = path + "/";

	                if (!path.startsWith("images") && !path.startsWith("sounds") && !path.startsWith("webkit"))
	                    copyFileOrDir( p + assets[i]);
	            }
	        }
	    } catch (IOException ex) {
	        Log.e("COPYFILEORDIR", "I/O Exception", ex);
	    }
	}

	private void copyFile(String filename) {
	    AssetManager assetManager = this.getAssets();

	    InputStream in = null;
	    OutputStream out = null;
	    String newFileName = null;
	    try {
	        Log.i("tag", "copyFile() "+filename);
	        in = assetManager.open(filename);
	        if (filename.endsWith(".jpg")) // extension was added to avoid compression on APK file
	            newFileName = Environment.getExternalStorageDirectory() + "/lolcode/.libs/" + filename.substring(0, filename.length()-4);
	        else
	            newFileName = Environment.getExternalStorageDirectory() + "/lolcode/.libs/" + filename;
	        out = new FileOutputStream(newFileName);

	        byte[] buffer = new byte[1024];
	        int read;
	        while ((read = in.read(buffer)) != -1) {
	            out.write(buffer, 0, read);
	        }
	        in.close();
	        in = null;
	        out.flush();
	        out.close();
	        out = null;
	    } catch (Exception e) {
	        Log.e("tag", "Exception in copyFile() of "+newFileName);
	        Log.e("tag", "Exception in copyFile() "+e.toString());
	    }

	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
		
        menu.add(0, 1, 0, "Run")
            .setIcon(R.drawable.debug)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);


		SubMenu options = menu.addSubMenu("Options");
        options.add(0, 2, 0, "Open...");
        options.add(0, 3, 0, "Save");
        options.add(0, 4, 0, "Save As...");
        
        MenuItem subMenu1Item = options.getItem();
        subMenu1Item.setIcon(R.drawable.abs__ic_menu_moreoverflow_holo_dark);
        subMenu1Item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        
        return true;
    }
	
	public boolean onOptionsItemSelected(MenuItem item) {
	      // Handle item selection
	      switch (item.getItemId()) {
	      case 1:
	          Intent i = new Intent(this, ConsoleActivity.class);
	          i.putExtra("File", currentFile);
	          startActivity(i);
	          return true;
	      case 2:
	    	  Intent j = new Intent(this, FileManager.class);
	    	  startActivity(j);
	    	  finish();
	    	  return true;
	      case 4:
	    	  Intent k = new Intent(this, SaveFile.class);
	    	  k.putExtra("Content", editor.getText().toString());
	    	  startActivity(k);
	    	  finish();
	    	  return true;
	      case 3:
	    	  saveFile(currentFile);
	    	  return true;
	      }
	      return false;
	}
	
	public void saveFile(String path){
		// write on SD card file data in the editor
		try {
			File myFile = new File(path);
			myFile.createNewFile();
			FileOutputStream fOut = new FileOutputStream(myFile);
			OutputStreamWriter myOutWriter = 
					new OutputStreamWriter(fOut);
			myOutWriter.append(editor.getText());
			myOutWriter.close();
			fOut.close();
			Toast.makeText(getBaseContext(),
					"File saved.",
					Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Toast.makeText(getBaseContext(), e.getMessage(),
					Toast.LENGTH_SHORT).show();
		}
	}
	
	public void readFile(String path){
		try {
			File myFile = new File(path);
			FileInputStream fIn = new FileInputStream(myFile);
			BufferedReader myReader = new BufferedReader(
					new InputStreamReader(fIn));
			String aDataRow = "";
			String aBuffer = "";
			while ((aDataRow = myReader.readLine()) != null) {
				aBuffer += aDataRow + "\n";
			}
			editor.setText(aBuffer);
			myReader.close();
			Toast.makeText(getBaseContext(),
					"Done reading SD 'mysdfile.txt'",
					Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Toast.makeText(getBaseContext(), e.getMessage(),
					Toast.LENGTH_SHORT).show();
		}
	}
}
