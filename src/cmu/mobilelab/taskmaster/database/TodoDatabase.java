package cmu.mobilelab.taskmaster.database;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;

public class TodoDatabase extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 4;
	private static final String DATABASE_NAME = "taskmaster";
	private static final String DICTIONARY_TABLE_NAME = "toDoList";
	private static final String DICTIONARY_TABLE_CREATE = "CREATE TABLE "
			+ DICTIONARY_TABLE_NAME + " ( id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, estimatedTime INT, completedTime INT, category TEXT, alertMethod TEXT, completed BOOLEAN);";

	private static final int ID_INDEX = 0;
	private static final int TITLE_INDEX = 1;
	private static final int ESTIMATED_INDEX = 2;
	private static final int COMPLETED_TIME_INDEX = 3;
	private static final int CATEGORY_INDEX = 4;
	private static final int ALERT_INDEX = 5;
	private static final int COMPLETED_INDEX = 6;
	
	Context context;
	private static TodoDatabase singleton;

	public TodoDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		
		this.context = context;
		singleton = this;
	}

	public static TodoDatabase singleton() {
		return singleton;
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DICTIONARY_TABLE_CREATE);
		db.setVersion(DATABASE_VERSION);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion < 3) {
			// just delete the database and recreate it
			db.execSQL("DROP TABLE "+DICTIONARY_TABLE_NAME);
			onCreate(db);
			return;
		}
		
		if (oldVersion < 4) {
			// 3 --> 4
			db.execSQL("ALTER TABLE "+DICTIONARY_TABLE_NAME+" ADD completed BOOLEAN");
			ContentValues values = new ContentValues();
			values.put("completed",0);
			db.update(DICTIONARY_TABLE_NAME, values, null, null);
		}
	}
	
	public void clearDB(){
		SQLiteDatabase db = getReadableDatabase();
		db.execSQL("DROP TABLE "+DICTIONARY_TABLE_NAME);
		onCreate(db);
	}
	// TODO: if we want other apps to be able to access the list, we need a ContentProvider
	// Aand the following methods would probably belong in it?

	/**
	 * Create a new list item retrieved from the database
	 * 
	 * @param id
	 *            ID of item to retrieve
	 */
	public ToDoListTask getToDoTask(int id) {
		// Get the row
		Cursor result = getReadableDatabase().query(DICTIONARY_TABLE_NAME, null, "id = '" + id + "'", null, null, null, null, "1");

		//TODO: should we close the db?
		
		// Construct the result, return it
		return createToDoTask(result);
	}
	public ArrayList<ToDoListTask> getToDoTasks() {
		return getToDoTasks(null,null);
	}
	

	public ArrayList<ToDoListTask> getUncompletedToDoTasks() {
		return getToDoTasks("completed=0",null);
	}
	
	/**
	 * Get all the list items
	 */
	public ArrayList<ToDoListTask> getToDoTasks(String whereClause, String orderBy) {
		// Get the rows
//		Cursor result = getReadableDatabase().query(DICTIONARY_TABLE_NAME, null, whereClause, null, null, null, orderBy, null);
		if (orderBy == null)
			orderBy = "";
		if (!orderBy.equals(""))
			orderBy = " ORDER BY " + orderBy;
		
		if (whereClause == null)
			whereClause = "";
		if (!whereClause.equals(""))
			whereClause = " WHERE " + whereClause;
		
	
		Cursor result = getReadableDatabase().rawQuery("SELECT *,(completedTime/estimatedTime) AS completedness FROM "+DICTIONARY_TABLE_NAME+whereClause+orderBy, null);
		//TODO: should we close the db?
		
		// Construct the results, return it
		ArrayList<ToDoListTask> items = new ArrayList<ToDoListTask>();
		while (result.moveToNext()) {
			items.add(createToDoTask(result));
		}
		return items;
	}
	
	public ToDoListTask createToDoTask(Cursor result) {
		ToDoListTask item = new ToDoListTask(result.getString(TITLE_INDEX),
				result.getString(CATEGORY_INDEX), result.getInt(ESTIMATED_INDEX),
				result.getInt(COMPLETED_TIME_INDEX),result.getString(ALERT_INDEX));
		item.setId(result.getInt(ID_INDEX));
		item.setCompleted(result.getInt(COMPLETED_INDEX)==1);
		return item;
	}
	
	private ContentValues getValuesForItem(ToDoListTask item) {
		ContentValues values = new ContentValues();
		values.put("title",item.getTitle());
		values.put("estimatedTime", item.getEstimatedTime());
		values.put("completedTime", item.getCompletedTime());
		values.put("category", item.getCategory());
		values.put("alertMethod", item.getAlertMethod());
		values.put("completed", item.isCompleted() ? 1 : 0);
		return values;
	}

	/**
	 * Commit changes to the database
	 */
	public void putToDoTask(ToDoListTask item) {
		if (item.getId() == Long.MIN_VALUE) {
			// Insert new item
			long newID = getWritableDatabase().insert(DICTIONARY_TABLE_NAME, null, getValuesForItem(item));		
			// This should work, after reading the sqlite docs it seems ROWID == our ID row (woohoo, magic!)
			item.setId(newID);
			
		} else {
			// Update the existing entry
			int num = getWritableDatabase().update(DICTIONARY_TABLE_NAME, getValuesForItem(item), "id="+String.valueOf(item.getId()), null);
			System.out.println(num);
		}
		//TODO: should we close the db?
	}

	/**
	 * Get all the categories
	 */
	public ArrayList<String> getCategories() {
		// TODO: probably won't want ALL of them in the future? some subset instead
		// Get the rows
		String[] cols = {"category"};
		Cursor result = getReadableDatabase().query(true,DICTIONARY_TABLE_NAME, cols, null, null, null, null, null, null);

		//TODO: should we close the db?
		
		// Construct the results, return it
		ArrayList<String> items = new ArrayList<String>();
		while (result.moveToNext()) {
			items.add(result.getString(0));
		}
		return items;
	}
}
