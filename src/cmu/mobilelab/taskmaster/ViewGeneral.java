package cmu.mobilelab.taskmaster;

import java.util.HashMap;
import java.util.Map.Entry;

import cmu.mobilelab.taskmaster.database.ToDoListTask;
import cmu.mobilelab.taskmaster.database.TodoDatabase;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

/**
 * ViewGeneral is the activity which displays general application statistics.
 * 
 * @author mlen, trevorsa
 *
 */
public class ViewGeneral extends Activity {

	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.viewgeneral);

	    TextView taskTotalTime = (TextView)findViewById(R.id.viewGeneralTotalTimeText);
	    TextView tasksCompleted = (TextView)findViewById(R.id.viewGeneralTasksCompletedText);
	    TextView longestTaskText = (TextView)findViewById(R.id.viewGeneralTimeConsumingText);
	    TextView longestCategoryText = (TextView)findViewById(R.id.viewGeneralTimeConsumingCategoryText);
	    long totalTime = 0;
	
	    TodoDatabase database = new TodoDatabase(this);
	    int completedTasksCount = 0;
	    ToDoListTask longestTask = null;
        HashMap<String, Long> categories = new HashMap<String, Long>();
        
	    for(ToDoListTask task : database.getToDoTasks())
	    {
	    	if(longestTask == null) longestTask = task;
	    	else if(longestTask.getCompletedTime() < task.getCompletedTime()) longestTask = task;
	    	
	    	totalTime += task.getCompletedTime();
	    	if(task.isCompleted())
	    		completedTasksCount++;
	    	
        	
        	if(!categories.containsKey(task.getCategory())){
        		categories.put(task.getCategory(),task.getCompletedTime());
        	}else
        	{
        		categories.put(task.getCategory(),categories.get(task.getCategory()) + task.getCompletedTime());
        	}
	    }// end task loop
	    
	    taskTotalTime.setText(getString(R.string.viewGeneralTotalTimeText) + " " + Home.stringFromSeconds(totalTime));
	    tasksCompleted.setText(getString(R.string.viewGeneralTasksCompletedText) + " " + completedTasksCount);
	    
	    //As long as there is at least one task
	    if(longestTask != null){
		    longestTaskText.setText(getString(R.string.viewGeneralTimeConsumingText) + " " + longestTask.getTitle());
		    
		    Entry<String,Long> largestCategory = null;
		    for(Entry<String,Long> cat : categories.entrySet())
		    {
		    	if(largestCategory == null || largestCategory.getValue() < cat.getValue())
		    		largestCategory = cat;
		    }
		    if(largestCategory != null)
		    	longestCategoryText.setText(getString(R.string.viewGeneralTimeConsumingCategoryText) + " " + largestCategory.getKey() + "\n   and took " + Home.stringFromSeconds(largestCategory.getValue()));
		    		
	    }
	}//end onCreate
	
    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
    	menu.add("Clear Database");
    	return true;
    }
    
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {

		//Show a comfirmation box
    	if (item.getTitle().equals("Clear Database")){
    		
			final AlertDialog.Builder categoryDialog = new AlertDialog.Builder(this);
			final TextView msg = new TextView(this);
			msg.setPadding(5,5,5,5);
			msg.setText("Are you sure?");
			
			categoryDialog.setTitle("Clear your Database").setView(msg);
			
			//If save, take the input text and add it to the category list
			categoryDialog.setPositiveButton("Yes",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

				        
				        TodoDatabase database = new TodoDatabase(ViewGeneral.this);
				        database.clearDB();
				        dialog.cancel();
					}
			});

			//Cancel: do nothing
			categoryDialog.setNegativeButton("No",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							dialog.cancel();
						}
					});
			
			categoryDialog.show();	
    	}
    	return true;
    }

}
