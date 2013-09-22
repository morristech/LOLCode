/*
 *  Copyright 2011 Seto Chi Lap (setosoft@gmail.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package net.linxdroid.lolcode.console.gui;

import net.linxdroid.lolcode.console.CommandDispatcher;
import net.linxdroid.lolcode.R;
import net.linxdroid.lolcode.R.id;
import net.linxdroid.lolcode.R.layout;
import net.linxdroid.lolcode.R.string;
import net.linxdroid.lolcode.console.builtincmd.CmdInfo;
import net.linxdroid.lolcode.console.builtincmd.FileSys;
import net.linxdroid.lolcode.console.builtincmd.UiCmd;
import net.linxdroid.lolcode.console.common.*;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.util.Log;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.view.KeyEvent;
import android.widget.ImageButton;
import android.content.pm.ApplicationInfo;
import android.graphics.Typeface;
import android.content.res.Configuration;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Display;
import android.text.Layout;
import android.util.DisplayMetrics;

public class ConsoleActivity extends Activity implements IStdOut
{
	private ConsoleScrollView scrollView;
	private ConsoleOutputTextView outputTextView;
	private CommandDispatcher cmdDispatcher;
	String startCommand;
	boolean hasCommand = false;
	
    /** Called when the activity is first created. */
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.console);
        Bundle extras = getIntent().getExtras(); 
		if(extras !=null)
		{
			startCommand = "lolcode test file " + extras.getString("File");
			hasCommand = true;
		}
        CFunc.setAppInst(this.getApplication());
        init();
    }
    
    /**
     * To prevent system from calling onCreate once screen orientation/locale is changed.
     */
	@Override
	public void onConfigurationChanged(Configuration newConfig) 
	{
		super.onConfigurationChanged(newConfig);
	}
	
	@Override
	protected void onDestroy() 
	{
		super.onDestroy();
		cmdDispatcher.executeCommand(CmdInfo.getCmdInfo(CmdInfo.BuiltInCmd._ui_exit_.name()));
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (cmdDispatcher.isApkRunning()) {
				Toast.makeText(this, CFunc.getString(R.string.external_program_running), Toast.LENGTH_SHORT).show();
				return true; // prevent console from exiting if a program is still running
			}
		}

		return super.onKeyDown(keyCode, event);
	}
	
    
    private void issueCommand()
    {
       EditText inputView = (EditText)findViewById(R.id.inputBox);
	   String s = inputView.getText().toString().trim();
		
		   
	   if (cmdDispatcher.isApkRunning()) {
		   cmdDispatcher.write2ConStdIn(s);
	   }
	   else {

		   CmdInfo cmdInfo = CmdInfo.getCmdInfo(s);
		   if (cmdInfo != null) {
			  cmdDispatcher.executeCommand(cmdInfo);
		   }
		   else {
			  boolean isOK = false;
			  if (s.startsWith(CmdInfo.HISTORYCMD_PREFIX)) {
				 isOK = cmdDispatcher.executeCommand(CFunc.parseInt(s.substring(1)));
			  }
			  
			  if (!isOK) {
				 cmdDispatcher.executeCommand(new CmdInfo(CmdInfo.BuiltInCmd._unknown_, s, null));
			  }
		   }
	   }
	   
	   inputView.setText(""); // clear
		
    }
    
    private View.OnClickListener butClickListener = new View.OnClickListener() 
    {
    	public void onClick(View v) {
    		issueCommand();
		}
    	
    };
    
    private EditText.OnKeyListener inputTextViewKeyListener = new EditText.OnKeyListener() 
    {
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if (keyCode == KeyEvent.KEYCODE_ENTER
				&& event.getAction() == KeyEvent.ACTION_UP) {
    		
    			issueCommand();
    			return true;
    		}
    		return false;
    	}
    	
    };
    
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
    	menu.clear();
    	if (cmdDispatcher.isApkRunning()) {
    	   menu.add(0, UiConst.MENU_KILL_CONSOLE, 0, CFunc.getString(R.string.menu_kill_console))
               .setIcon(android.R.drawable.ic_delete);
    	   menu.add(0, UiConst.MENU_KILL_APP, 0, CFunc.getString(R.string.menu_kill_running_app))
               .setIcon(android.R.drawable.ic_menu_delete);
    	}
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected (MenuItem item)
    {
       switch(item.getItemId()) {
           case UiConst.MENU_KILL_APP:
        	 cmdDispatcher.killRunningApk();
        	 break;
           case UiConst.MENU_KILL_CONSOLE:
          	 cmdDispatcher.killRunningApk();
          	 CFunc.sleep(1000);
          	 Process.killProcess(Process.myPid());
          	 break;
       }
       
       return true;
    }
    
    private void init()
    {
    	// init private data dir
    	ApplicationInfo appInfo = this.getApplicationInfo();
    	FileSys.setPrivateDataRoot(appInfo.dataDir);
    	
    	// init display text views 
    	Display display = this.getWindowManager().getDefaultDisplay();
    	DisplayMetrics metrics = new DisplayMetrics();
    	display.getMetrics(metrics);
    	
    	LinearLayout ll = (LinearLayout)findViewById(R.id.verticalLayout);
    	outputTextView = new ConsoleOutputTextView(this, metrics.heightPixels);
    	ll.addView(outputTextView);
    	
    	scrollView = (ConsoleScrollView)findViewById(R.id.consoleScrollView);
    	scrollView.setScrollViewListener(outputTextView);
    	
    	// init input button
    	Button inputOK = (Button) findViewById(R.id.inputOKButton);
    	inputOK.setOnClickListener(butClickListener);
    	
    	// init input text view
    	EditText inputView = (EditText)findViewById(R.id.inputBox);
    	inputView.setOnKeyListener(inputTextViewKeyListener);
    	
    	cmdDispatcher = new CommandDispatcher(uiHandler);
    	
    	if(hasCommand){
			CmdInfo cmd = CmdInfo.getCmdInfo(startCommand);
			cmdDispatcher.executeCommand(cmd);
    	}
    }

    public void write(String msg)
    {
    	outputTextView.appendText(msg);
    	scrollView.post(scrollToBottomAction); // add scroll action to message queue
    }
    
    public void writeln(String msg)
    {
    	outputTextView.appendText(msg);
    	outputTextView.appendText(ConstantData.NEWLINE);
    	scrollView.post(scrollToBottomAction); // add scroll action to message queue
    }
    
    public void printError(String s)
	{
		writeln(CFunc.getString(R.string.error_prefix) + s);
	}
    
    private void clearScreen()
    {
    	outputTextView.clearAllText();
    	scrollView.post(scrollToTopAction); // add scroll action to message queue
    }
    
    private void setConsoleFontSize(int fontSize)
    {
    	clearScreen();
    	outputTextView.setFontSize(fontSize);
    }
    
    private Runnable scrollToBottomAction = new Runnable()
    {
    	public void run() 
    	{ 
    		if (outputTextView.getViewableHeight() > scrollView.getHeight()) {
    			scrollView.scrollTo(0, outputTextView.getHeight());
    		} 
        } 
    };
    
    private Runnable scrollToTopAction = new Runnable()
    {
    	public void run() 
    	{ 
    		scrollView.scrollTo(0, 0);
        } 
    };
    
    private Handler uiHandler = new Handler() {
		@Override
		public void handleMessage(Message m) {
			if (m.what == ConstantData.MsgType.STRING.ordinal()) {
			   write((String)m.obj);
			}
			else if (m.what == ConstantData.MsgType.CLEAR.ordinal()) {
			   clearScreen();
			}
			else if (m.what == ConstantData.MsgType.SHOWSRES.ordinal()) {
			   UiCmd.showScreenResolution(ConsoleActivity.this.getWindowManager().getDefaultDisplay(),
						                  ConsoleActivity.this);
			}
			else if (m.what == ConstantData.MsgType.SHOWFONTSIZE.ordinal()) {
			   int fontSize = outputTextView.getFontSize();
			   writeln(CFunc.getString(R.string.current_fontsize) + ": " + 
					                   ConstantData.ConsoleFontSize.toFontSize(fontSize).name() + 
					                   " (" + fontSize + ")");
			}
			else if (m.what == ConstantData.MsgType.SETFONTSIZE.ordinal()) {
			   setConsoleFontSize((Integer)m.obj);
			}
			else if (m.what == ConstantData.MsgType.EXIT.ordinal()) {
			   finish();
			}
		}
    };
}
