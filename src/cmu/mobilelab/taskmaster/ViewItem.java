package cmu.mobilelab.taskmaster;

import cmu.mobilelab.taskmaster.database.ToDoListTask;
import cmu.mobilelab.taskmaster.database.TodoDatabase;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Chronometer.OnChronometerTickListener;

//TODO:  If we had time, it would've been nice create a background service for timing.
/**
 * ViewCategory is the activity which displays Task/Activity information as well as timing functionality.
 * 
 * @author mlen, trevorsa
 *
 */
public class ViewItem extends Activity implements OnChronometerTickListener, OnClickListener{
	
	static final int EDIT_ITEM_REQUEST = 0;
	
	private long timeElapsed = 0;
	private ToDoListTask taskItem;
    Chronometer currentTime;
    Button startTimerBtn;
    Button stopTimerBtn;
    Button resumeTimerBtn;		        
    Button pauseBtn;
    public void onCreate(Bundle savedInstanceState){
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.viewitem);
        
        //Get intent bundle for task name
         taskItem = (ToDoListTask)this.getIntent().getParcelableExtra("ToDoItem");
         

         currentTime = (Chronometer) findViewById(R.id.viewItemTimer);
         startTimerBtn = (Button) findViewById(R.id.viewItemStartButton);
         stopTimerBtn = (Button) findViewById(R.id.viewItemStopButton);
         resumeTimerBtn = (Button) findViewById(R.id.viewItemResumeButton);		        
         pauseBtn = (Button) findViewById(R.id.viewItemPauseButton);
        //Initialize gui elements values
        initializeGUIValues();
        
        //Listen for all timer button events
    	OnClickListener timerButtonListener = this;
        
		findViewById(R.id.viewItemStartButton).setOnClickListener(timerButtonListener);
        findViewById(R.id.viewItemStopButton).setOnClickListener(timerButtonListener);
        findViewById(R.id.viewItemResumeButton).setOnClickListener(timerButtonListener);
        findViewById(R.id.viewItemPauseButton).setOnClickListener(timerButtonListener);		
        findViewById(R.id.viewItemPauseButton).setEnabled(false);
        
        
    }//end onCreate
    
    
    private void initializeGUIValues() {
    	 //Set the name
        TextView taskNameText = (TextView)findViewById(R.id.viewItemTaskNameText);
        TextView estimatedTimeText = (TextView)findViewById(R.id.viewItemEstimatedTimeText);
        taskNameText.setText(taskItem.getTitle());
        
        //Set the category
        TextView taskCategoryText = (TextView)findViewById(R.id.viewItemTaskCategoryText);
        if(taskItem.getCategory().trim().equals("")){
            taskCategoryText.setText(getString(R.string.viewItemCategoryText) + " Uncategorized");
        }
        	
        taskCategoryText.setText(getString(R.string.viewItemCategoryText) + " " + taskItem.getCategory());
        

	   
	    	TextView taskTotalTimeText = (TextView)findViewById(R.id.viewItemTotalTimeText);
        long totalTime = taskItem.getCompletedTime();
        if(taskItem.getCategory().compareTo("Activities") == 0)
        	taskTotalTimeText.setText(getString(R.string.viewItemTotalTimeActivityText) + " " + Home.stringFromSeconds(totalTime));
	    else
	    {
        taskTotalTimeText.setText(getString(R.string.viewItemTotalTimeText) + " " + Home.stringFromSeconds(totalTime));

        estimatedTimeText.setText(getString(R.string.viewItemEstimatedTimeText) + " " + Home.stringFromSeconds(taskItem.getEstimatedTime() * 60));
	    }
        
	}


	/**
     * We got a result back; probably an edited ToDo item.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (requestCode == EDIT_ITEM_REQUEST && resultCode == RESULT_OK) {
    		// Get modified task, refresh the GUI elements
    		taskItem = data.getParcelableExtra("ToDoItem");
    		initializeGUIValues();
    	}
    }
    
    /**
     * Menu item was selected
     */
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
    	if (item.getTitle().equals("Edit task"))
    		startActivityForResult(new Intent(this,AddItem.class).putExtra("ToDoItem", taskItem),EDIT_ITEM_REQUEST);
    	else if (item.getTitle().equals("Mark as completed")) {
    		// Set completed, save to database, and return to Home page
    		taskItem.setCompleted(true);
    		TodoDatabase.singleton().putToDoTask(taskItem);
    		ViewItem.this.finish();
    	}
    	return true;
    }
    
    /**
     * Create menu items
     */
    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
    	menu.add("Edit task");
    	
    	//TODO: Allow activities to be deleted.
    	if(taskItem != null && taskItem.getCategory().compareTo("Activities") != 0)
    		menu.add("Mark as completed");
    	return true;
    }
    

	private long activityAlarmTime = 5;
    /**
     * given to Chronometer when the ToDoListItem is an Activity. 
     */
	public void onChronometerTick(Chronometer chronometer) {
	
		if((SystemClock.elapsedRealtime() -  chronometer.getBase())/ 1000 >= activityAlarmTime)
		{
			chronometer.stop();
			final AlertDialog.Builder categoryDialog = new AlertDialog.Builder(this);
			final TextView msg = new TextView(this);
			msg.setPadding(5,5,5,5);
			msg.setText("You've spent some time with this activity.  You should take a break and complete another task");
			
			categoryDialog.setTitle(getString(R.string.alarmActivityTimeUp)).setView(msg);
			
			//If save, take the input text and add it to the category list
			categoryDialog.setPositiveButton("Stop Activity",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						stopTime();
						dialog.cancel();
						finish();
					}
			});

			//Cancel: do nothing
			categoryDialog.setNegativeButton("Continue",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							//Resume time, adding to when time was paused
							activityAlarmTime += 10;
					        Chronometer currentTime = (Chronometer) findViewById(R.id.viewItemTimer);
					        currentTime.start();
							dialog.cancel();
						}
					});
			
			categoryDialog.show();	
		}
		
		
	}
	
	/**
	 * onClick for chronometer buttons
	 */
	public void onClick(View v) {
        TextView taskTotalTimeText = (TextView)findViewById(R.id.viewItemTotalTimeText);
		if(v.getId() == R.id.viewItemStartButton) {
	        //starting timer from nothing
			startTime();
			
		} else if(v.getId() == R.id.viewItemStopButton) {
			stopTime();
			
			//Print on screen
		    if(taskItem.getCategory().compareTo("Activities") == 0)
	        	taskTotalTimeText.setText(getString(R.string.viewItemTotalTimeActivityText) + " " + Home.stringFromSeconds(taskItem.getCompletedTime()));
	        else
	        	taskTotalTimeText.setText(getString(R.string.viewItemTotalTimeText) + " " + Home.stringFromSeconds(taskItem.getCompletedTime()));	
	        
		} else if(v.getId() == R.id.viewItemPauseButton) {
	    	pauseTime();
		} else { //viewItemResumeButton
			resumeTime();
		} //End If
	}//End onClick
	
	private boolean isTiming = false;
	
	/**
	 * starting timer from nothing
	 */
	private void startTime(){   		
        isTiming =  true;
		timeElapsed = 0;
        currentTime.setBase(SystemClock.elapsedRealtime());
		currentTime.start();
		startTimerBtn.setText(getString(R.string.viewItemStopButton));
		
	    if(taskItem.getCategory().compareTo("Activities") == 0)
	    	currentTime.setOnChronometerTickListener(this);
		
		pauseBtn.setClickable(true);
		startTimerBtn.setVisibility(View.GONE);
		stopTimerBtn.setVisibility(View.VISIBLE);
		pauseBtn.setEnabled(true);
	}
	/**
	 * Pause time, save current elapsed time
	 */
	private void pauseTime(){
		timeElapsed = SystemClock.elapsedRealtime() - currentTime.getBase();	
		
		resumeTimerBtn.setVisibility(View.VISIBLE);
		currentTime.stop();
	
		pauseBtn.setEnabled(false);
	
	}

	/**
	 * Resume time, adding to when time was paused
	 */
	private void resumeTime(){
		
        currentTime.setBase(SystemClock.elapsedRealtime()-timeElapsed);
		currentTime.start();
		
		resumeTimerBtn.setVisibility(View.GONE);
		stopTimerBtn.setVisibility(View.VISIBLE);
		pauseBtn.setEnabled(true);
	}
	/**
	 * stop and save time.
	 */
	private void stopTime(){

        isTiming = false;
        //If you are paused, use timeElapsed from PauseTime, not at stopTime.
        if(pauseBtn.isEnabled())
        	timeElapsed = SystemClock.elapsedRealtime() - currentTime.getBase();
		currentTime.stop();
		startTimerBtn.setText(getString(R.string.viewItemStartButton));
		
		pauseBtn.setClickable(false);
		startTimerBtn.setVisibility(View.VISIBLE);
		stopTimerBtn.setVisibility(View.GONE);
		resumeTimerBtn.setVisibility(View.GONE);
		pauseBtn.setEnabled(false);

		// Save the time elapsed to database
		taskItem.addCompletedTime(timeElapsed/1000); // Let's store seconds; milliseconds is too fine-grained for storage

		TodoDatabase.singleton().putToDoTask(taskItem);
		
	}
	
	/**
	 * Capture Back button if we're currently timing
	 */
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if ((keyCode == KeyEvent.KEYCODE_BACK) && isTiming) {
			pauseTime();
			final AlertDialog.Builder categoryDialog = new AlertDialog.Builder(this);
			final TextView msg = new TextView(this);
			msg.setPadding(5,5,5,5);
			msg.setText("Are you sure you want to exit?");
			
			categoryDialog.setTitle("You are currently timing this task.").setView(msg);
			
			//If save, take the input text and add it to the category list
			categoryDialog.setPositiveButton("Exit and save time",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						stopTime();
						dialog.cancel();
						finish();
					}
			});
			categoryDialog.setNeutralButton("Stay and continue timing",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							resumeTime();
						}
				});
			
			//Cancel: do nothing
			categoryDialog.setNegativeButton("Exit and forget",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							dialog.cancel();
							finish();
						}
					});
			
			categoryDialog.show();	
			return true;
		}
	    else
	    	return super.onKeyDown(keyCode, event);
	}
	
	public void onSaveInstanceState(Bundle outState) {
		if (isTiming) {
			Chronometer currentTime = (Chronometer) findViewById(R.id.viewItemTimer);
			Button pauseBtn = (Button) findViewById(R.id.viewItemPauseButton);
			if (pauseBtn.isEnabled())
				outState.putString("state", "start");
			else
				outState.putString("state", "paused");

			outState.putLong("base", currentTime.getBase());
		} else {
			outState.putString("state", "stop");
		}
		super.onSaveInstanceState(outState);
	}

	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		String state = savedInstanceState.getString("state");
		if (state.compareTo("start") == 0) {
			startTime();
			Chronometer currentTime = (Chronometer) findViewById(R.id.viewItemTimer);
			currentTime.setBase(savedInstanceState.getLong("base"));
		} else if (state.compareTo("pause") == 0) {
			startTime();
			pauseTime();
			Chronometer currentTime = (Chronometer) findViewById(R.id.viewItemTimer);
			currentTime.setBase(savedInstanceState.getLong("base"));
		} else {

		}
	}
}
