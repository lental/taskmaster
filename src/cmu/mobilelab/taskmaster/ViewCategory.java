package cmu.mobilelab.taskmaster;

import cmu.mobilelab.taskmaster.database.ToDoListCategory;
import cmu.mobilelab.taskmaster.database.ToDoListTask;
import cmu.mobilelab.taskmaster.database.TodoDatabase;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

/**
 * ViewCategory is the activity which displays category statistics.
 * 
 * @author mlen, trevorsa
 *
 */

public class ViewCategory extends Activity{
	 public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.viewcategory);

	        //Get intent bundle for task name
	        ToDoListCategory categoryItem = this.getIntent().getParcelableExtra("CategoryItem");


	        TextView taskCategoryText = (TextView)findViewById(R.id.viewCategoryNameText);
	        if(categoryItem.getTitle().compareTo("Activities") != 0){
	        	taskCategoryText.setText("\"" + categoryItem.getTitle()+ "\" Statistics" );
	        }
	        else{
	        	taskCategoryText.setText(getString(R.string.viewCategoryActivityNameText));
	        }
	        TextView taskTotalTime = (TextView)findViewById(R.id.viewCategoryTotalTimeText);
	        TextView tasksCompleted = (TextView)findViewById(R.id.viewCategoryTasksCompletedText);
	        TextView longestTaskText = (TextView)findViewById(R.id.viewCategoryTimeConsumingText);
	        long totalTime = 0;

	        TodoDatabase database = new TodoDatabase(this);
	        int completedTasksCount = 0;
	        ToDoListTask longestTask = null;
	        
	        for(ToDoListTask task : database.getToDoTasks("category='" + categoryItem.getTitle() + "'", null))
	        {
	        	if(longestTask == null) longestTask = task;
	        	else if(longestTask.getCompletedTime() < task.getCompletedTime()) longestTask = task;
	        	
	        	totalTime += task.getCompletedTime();
	        	Log.v("viewCategory: is completed",Boolean.toString(task.isCompleted()));
	        	if(categoryItem.getTitle().compareTo("Activities") != 0 && task.isCompleted())
	        		completedTasksCount++;

	        }
	        //Calculate time
	       
	        if(categoryItem.getTitle().compareTo("Activities") == 0){
	        	taskTotalTime.setText(getString(R.string.viewCategoryTotalTimeText) + " " + Home.stringFromSeconds(totalTime));
	        	tasksCompleted.setVisibility(View.INVISIBLE);
	        	longestTaskText.setText(getString(R.string.viewCategoryActivityTimeConsumingText) +  " " +
		        		longestTask.getTitle() + "\n   and took " + Home.stringFromSeconds(longestTask.getCompletedTime()));
	        }
	        else{
		        taskTotalTime.setText(getString(R.string.viewCategoryTotalTimeText) + " " + Home.stringFromSeconds(totalTime));
		        tasksCompleted.setText(getString(R.string.viewCategoryTasksCompletedText) + " " + completedTasksCount);

		        longestTaskText.setText(getString(R.string.viewCategoryTimeConsumingText) + " " +
		        		longestTask.getTitle() + "\n   and took " + Home.stringFromSeconds(longestTask.getCompletedTime()));

	        }
	 }//end onCreate
}//end class
