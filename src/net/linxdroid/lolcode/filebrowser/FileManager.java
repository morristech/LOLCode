package net.linxdroid.lolcode.filebrowser;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.text.DateFormat; 

import net.linxdroid.lolcode.MainAndroid;
import net.linxdroid.lolcode.R;

import android.os.Bundle; 
import android.os.Environment;
import android.app.Activity;
import android.content.Intent; 
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class FileManager extends Activity {

	private File currentDir;
    private FileArrayAdapter adapter;
    private ListView fileList;
	private ArrayList<String> mDirs = new ArrayList<String>();
	private static String returnedItems = "Error/in/returnedItems";
    private static String[] dir = returnedItems.split(",");
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.filemanager);
		setListView((ListView)findViewById(R.id.fileList));
        currentDir = new File(Environment.getExternalStorageDirectory().toString() + "/lolcode/");
        fill(currentDir);
    }
    
	public void setListView(ListView listView) {
		this.fileList = listView;
	}
    
	private void fill(File f){
		try{
			mDirs.add(f.getCanonicalPath());
		}catch(Exception e){
			Log.d("FILL_PATH", e.toString());
		}
    	File[] dirs = f.listFiles(); 
    	if(f.getPath() == "/sdcard/lolcode/" || f.getPath() == Environment.getExternalStorageDirectory() + "/lolcode/"){
    		this.setTitle("/sdcard/lolcode/");
    	}
    	else{
    		this.setTitle(f.getPath());
    	}
    	List<Item> dir = new ArrayList<Item>();
    	List<Item> fls = new ArrayList<Item>();
    	try{
    		for(File ff: dirs){
    			Date lastModDate = new Date(ff.lastModified()); 
				DateFormat formater = DateFormat.getDateInstance();
				String date_modify = formater.format(lastModDate);
				if(ff.isDirectory()){
					File[] fbuf = ff.listFiles(); 
					int buf = 0;
					if(fbuf != null){ 
						buf = fbuf.length;
					} 
					else buf = 0; 
					String num_item = String.valueOf(buf);
					if(buf == 0) num_item = num_item + " item";
					else num_item = num_item + " items";
					
					if(checkIsHidden(ff)){
						dir.add(new Item(ff.getName(), num_item,date_modify, ff.getCanonicalPath(), "directory_icon", true));
					}
					else if(checkIsHidden(ff) && checkIsSymlink(ff)){
						dir.add(new Item(ff.getName(), num_item,date_modify, ff.getCanonicalPath(), "directory_icon", true));
					}
					else{
						dir.add(new Item(ff.getName(), num_item,date_modify, ff.getAbsolutePath(), "directory_icon", false));
					}
				}
				else if(ff.getName().toLowerCase().endsWith(".lol")){
					fls.add(new Item(ff.getName(), convertByte(ff.length()), date_modify, ff.getAbsolutePath(),"lol", false));
				}
				else{
					if(checkIsHidden(ff)){
						fls.add(new Item(ff.getName(), convertByte(ff.length()), date_modify, ff.getAbsolutePath(),"unknown", true));
					}else{
						fls.add(new Item(ff.getName(), convertByte(ff.length()), date_modify, ff.getAbsolutePath(),"unknown", false));
					}
				}
			 }
		 }catch(Exception e){    
			 
		 }
		 Collections.sort(dir);
		 Collections.sort(fls);
		 dir.addAll(fls);
		 if(!f.getName().equalsIgnoreCase(""))
			 dir.add(0,new Item("..", "Parent Directory", "",f.getParent(), "clear", false));
		 adapter = new FileArrayAdapter(FileManager.this, R.layout.file_view, dir);
		 fileList.setAdapter(adapter);
		 fileList.setOnItemClickListener(new OnItemClickListener() {
			 public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				 Item o = adapter.getItem(position);
				 if(o.getImage().equalsIgnoreCase("directory_icon") || o.getImage().equalsIgnoreCase("clear") ||
						o.getImage().equalsIgnoreCase("sym")){
					 currentDir = new File(o.getPath());
					 fill(currentDir);
					 mDirs.clear();
				 }
				 else if(o.getName().toLowerCase().endsWith(".lol")){
					 Intent i = new Intent(FileManager.this, MainAndroid.class);
					 i.putExtra("File", o.getPath());
					 i.putExtra("Name", o.getName());
					 startActivity(i);
				 }else{
					 Toast.makeText(getBaseContext(), "Please choose a .lol file.", Toast.LENGTH_LONG).show();
				 }
			 }
		 });
    }
    
    public static boolean checkIsSymlink(File file) throws IOException{
    	File canon;
    	if (file.getParent() == null) {
    		canon = file;
    	} else {
    		File canonDir = file.getParentFile().getCanonicalFile();
    		canon = new File(canonDir, file.getName());
    	}
    	return !canon.getCanonicalFile().equals(canon.getAbsoluteFile());
    }
    
    public static boolean checkIsHidden(File file){
    	return file.getName().startsWith(".");
    }
    
    public static String convertByte(Long size){
    	String result = "Unknown size";
    	if (size.doubleValue() < 1024D){
    		//bytes
    		result = Math.round(size.doubleValue() * 1e1) / 1e1 + " bytes";
    	}else if (size.doubleValue() < 1048576D){
    		//kilobytes
    		result = Math.round(size.doubleValue()/1024D * 1e1) / 1e1 + " kB";
    	}else if (size.doubleValue() < 1073741824D){
    		//megabytes
    		result = Math.round(size.doubleValue()/1048576D * 1e1) / 1e1 + " MB";
    	}else if (size.doubleValue() < 1099511627776D){
    		//gigabytes
    		result = Math.round(size.doubleValue()/1073741824D * 1e1) / 1e1 + " GB";
    	}
    	return result;
    }
}
