package cmu.mobilelab.taskmaster;

import java.util.ArrayList;
import java.util.Random;

import cmu.mobilelab.taskmaster.database.ToDoListCategory;
import cmu.mobilelab.taskmaster.database.ToDoListItem;
import cmu.mobilelab.taskmaster.database.ToDoListTask;
import cmu.mobilelab.taskmaster.database.TodoDatabase;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class Home extends Activity {
	
	static final int ADD_ITEM_REQUEST = 0;
	static final int VIEW_ITEM_REQUEST = 1;
	static final int VIEW_CATEGORY_REQUEST = 2;
	static final int VIEW_GENERAL_REQUEST = 3;
	
	private static final int SORT_URGENCY = 1;
	private static final int SORT_LCOMPLETED = 2;
	private static final int SORT_QUICKEST = 3;
	private static final int SORT_CATEGORY = 4;
	private static final int SORT_MCOMPLETED  =5;
	
	//ArrayList which is used in the Adapter 
   	ArrayList<ToDoListItem> listAdapterBack = new ArrayList<ToDoListItem>();

	private ToDoListArrayAdapter adapter;

	private TodoDatabase database = null;
	
	private int sortMode = SORT_CATEGORY;
	

	private AlertDialog recomendationAlert;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        database = new TodoDatabase(this);
        
        initializeList();
        
        //listens for a short click on items in the list
        OnItemClickListener listClickListener = new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				
				if(adapter.getItem(position) instanceof ToDoListTask){
					
					//When a task is clicked, we pass the task and and start viewItem
					Intent viewItemIntent = new Intent(Home.this, ViewItem.class);
					viewItemIntent.putExtra("ToDoItem", (ToDoListTask)adapter.getItem(position));
					startActivityForResult(viewItemIntent, VIEW_ITEM_REQUEST);
				} else {
					
					//When a category is clicked, we pass the category and and start viewCategory				
					Intent viewCategoryIntent = new Intent(Home.this, ViewCategory.class);
					viewCategoryIntent.putExtra("CategoryItem", (ToDoListCategory)adapter.getItem(position));
					startActivityForResult(viewCategoryIntent, VIEW_CATEGORY_REQUEST);
				}//end if ToDoListTask
			}//end onItemClick
		};//end onItemClickListener
		
		//Set the short click listener
		ListView list = (ListView) findViewById(R.id.listToDo);
		list.setOnItemClickListener(listClickListener);
		
		initShake();
    }//end onCreate
    
    

    /**
     * Initialize the list to contain all categories and their tasks
     */
    private void initializeList() {
    	// Chose between sorting methods;
    	// Should the sort persist between opening/closing the app?
    	
    	ListView list = (ListView) findViewById(R.id.listToDo);
        
        //Get Categories, and then add each category of tasks separately.
    	listAdapterBack.clear();
    	

		TextView header = (TextView) findViewById(R.id.homeHeader);
    	
    	switch (sortMode) {
    	case SORT_QUICKEST:
    		 // Get category's active tasks from the database
    		listAdapterBack.addAll(database.getToDoTasks("completed=0 AND category!='Activities'","estimatedTime"));
    		// update label
    		header.setText("Unfinished Tasks (Quickest)");
    		break;
    	case SORT_LCOMPLETED:
	   		 // Get category's active tasks from the database
    		listAdapterBack.addAll(database.getToDoTasks("completed=0 AND category!='Activities'","completedness ASC"));
    		// update label
    		header.setText("Unfinished Tasks (Least Complete)");

	   		break;
    	case SORT_MCOMPLETED:
	   		 // Get category's active tasks from the database
    		listAdapterBack.addAll(database.getToDoTasks("completed=0 AND category!='Activities'","completedness DESC"));
    		// update label
    		header.setText("Unfinished Tasks (Most Complete)");

	   		break;
    	case SORT_URGENCY:
    		// TODO: Uh, right now there's no due date. good/bad?
	   		 // Get category's active tasks from the database
    		listAdapterBack.addAll(database.getToDoTasks("completed=0 AND category!='Activities'","estimatedTime"));
    		// update label
    		header.setText("Unfinished Tasks (Urgent)");

	   		break;
    	case SORT_CATEGORY:
    	default:
    		// Default sort
    		initializeBackByCategory();
    		// update label
    		header.setText("Unfinished Tasks (Categories)");

    		break;
    	}
    	

        //Create an adapter that organizes the list, and sets the adapter on the list
        adapter = new ToDoListArrayAdapter(this, R.layout.list_item,listAdapterBack);
        list.setAdapter(adapter);
    }//end InitializeList
    
    private void initializeBackByCategory() {
		for(String category : database.getCategories()){
    		
    		 // Get category's active tasks from the database
    		 ArrayList<ToDoListTask> tasks = database.getToDoTasks("completed=0 AND category='" + category + "'",null);
    		 
    		
    		 if (tasks.size() > 0) {
    			 if(!category.trim().equals("") && category != null)
    				 listAdapterBack.add(new ToDoListCategory(category));
    			 
	        	 for(ToDoListTask task : tasks)
	        	 {
	        		 listAdapterBack.add(task);
	        	 }
    		 }   
    	}    	
	}//end initializeBackByCategory

	/**
     * We got a result back; probably a new or updated ToDo item.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	//if (requestCode == ADD_ITEM_REQUEST && resultCode == RESULT_OK) {
    		initializeList();
    	//} else if (requestCode == VIEW_ITEM_REQUEST) {
    	//	initializeList();
        //}
    }
    
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
    	if (item.getTitle().equals("Add new task"))
    		startActivityForResult(new Intent(this,AddItem.class),ADD_ITEM_REQUEST);
    	
    	if (item.getTitle().equals("What should I do?")) {
    		recommendATask();
    		recommendATask();
    	}
    	if (item.getGroupId() == 5) {
    		sortMode = item.getItemId();
    		initializeList();
    	}
    	
    	if (item.getTitle().equals("Show stats"))
    		startActivityForResult(new Intent(this,ViewGeneral.class),VIEW_GENERAL_REQUEST);
    	return true;
    }
    
    public void recommendATask() {
    	// SHUFFLE TIME!
    	if (recomendationAlert != null && recomendationAlert.isShowing())
    		return;
    	
    	recomendationAlert = new AlertDialog.Builder(this).create();
    	recomendationAlert.setTitle("Recomendation");
    	recomendationAlert.setButton(recomendationAlert.BUTTON_POSITIVE,"Ok!", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				recomendationAlert.dismiss();
			}
		});
    	
		Random r = new Random();
		
		String rationale = "";
		ToDoListTask recommended;

		if (database.getUncompletedToDoTasks().size() < 0) {
			rationale = "Go add some tasks!";
			recomendationAlert.setMessage(rationale);
			recomendationAlert.show();
			return;
		} else if (r.nextBoolean()) {
			recommended = database.getToDoTasks("completed=0 AND category!='Activities'","completedness ASC").get(0);
			rationale = "It's "+recommended.getCompletionPercentage()+"% done, you're almost there!";
		} else {
			recommended = database.getToDoTasks("completed=0 AND category!='Activities'","completedness DESC").get(0);
			rationale = "It's only "+recommended.getCompletionPercentage()+"% done; get cracking!";
		}
		

		recomendationAlert.setMessage("You should work on "+recommended.getTitle()+". "+rationale);
		recomendationAlert.show();
    }
    
    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
    	menu.add("Add new task");
    	menu.add("What should I do?");
    	menu.addSubMenu("Resort list");
    	//menu.getItem(2).getSubMenu().add(5,SORT_URGENCY, 0, "by Urgency: Due Date");
    	menu.getItem(2).getSubMenu().add(5,SORT_LCOMPLETED, 0, "by Least Completed");
    	menu.getItem(2).getSubMenu().add(5,SORT_MCOMPLETED, 0, "by Most Completed");
    	menu.getItem(2).getSubMenu().add(5,SORT_QUICKEST, 0, "by Quickest Estimated Time");
    	menu.getItem(2).getSubMenu().add(5,SORT_CATEGORY, 0, "by Category");
    	menu.add("Show stats");
    	
    	return true;
    }

    
    public static String stringFromSeconds(long totalSeconds){
    	 long hours = totalSeconds/3600;
	        long minutes = (totalSeconds / 60) - (hours * 60);
	        long seconds = (totalSeconds) - (hours * 3600) - (minutes * 60);
	        String minuteZero = "", secondZero = "";
	        if(minutes < 10) {
	        	minuteZero = "0";
	        }
	        if(seconds < 10) {
	        	secondZero = "0";
	        }
	        String totalTimeString = Long.toString(hours) + ":" + minuteZero + Long.toString(minutes) + ":" + secondZero + Long.toString(seconds);
    	return totalTimeString;
    }
    
    
    
    // Shake! (from http://stackoverflow.com/questions/2317428/android-i-want-to-shake-it)
    private SensorManager mSensorManager;
    private float mAccel; // acceleration apart from gravity
    private float mAccelCurrent; // current acceleration including gravity
    private float mAccelLast; // last acceleration including gravity

    public void initShake() {
    	/* do this in onCreate */
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        mAccel = 0.00f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;
    }
    
    private final SensorEventListener mSensorListener = new SensorEventListener() {

      public void onSensorChanged(SensorEvent se) {
        float x = se.values[0];
        float y = se.values[1];
        float z = se.values[2];
        mAccelLast = mAccelCurrent;
        mAccelCurrent = (float) Math.sqrt((double) (x*x + y*y + z*z));
        float delta = mAccelCurrent - mAccelLast;
        mAccel = mAccel * 0.9f + delta; // perform low-cut filter
        
        if (mAccel > 4) // we're shaking!
        	recommendATask();
      }

      public void onAccuracyChanged(Sensor sensor, int accuracy) {
      }
    };
    
}//end Home