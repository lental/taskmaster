package cmu.mobilelab.taskmaster;

import java.util.ArrayList;

import cmu.mobilelab.taskmaster.database.ToDoListTask;
import cmu.mobilelab.taskmaster.database.TodoDatabase;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

/**
 * AddItem is the activity that handles the creation of a new ToDoListItem.
 * 
 * @author mlen, trevorsa
 * 
 */
public class AddItem extends Activity {

	ToDoListTask item;
	private ArrayList<String> categories;
	Spinner categorySpinner;
	EditText estimatedTimeEdit;
	Spinner alertSpinner;
	int lastPosition;  //stores the last selected category, in case someone cancels out of a NewCategory...
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.additem);
		
		//Find EstimatedTimeEdit for use in OnItemSelectedListener
		estimatedTimeEdit = (EditText) findViewById(R.id.AddItemEstimatedTimeEditText);

		// Set alert spinner value
		ArrayList<String> alerts = new ArrayList<String>();
		alerts.add("No Alert");
		alertSpinner = (Spinner) findViewById(R.id.AddItemWarningModeSpinner);
		final ArrayAdapter<String> alertAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item,alerts);
		alertSpinner.setAdapter(alertAdapter);
		
		
		// Get previously used categories
		categories = TodoDatabase.singleton().getCategories();
		
		// Add defaults if they don't exist already
		categories.remove(null);
		categories.remove("None");
		if(!categories.contains("No Category"))
			categories.add(0, "No Category");
		if(!categories.contains("Activities"))
			categories.add("Activities");
		categories.add("New Category...");
		//TODO: Put Special Categories into String Table (Particularly New Category)
		
		categorySpinner = (Spinner) findViewById(R.id.AddItemCategorySpinner);
		final ArrayAdapter<String> categoryAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item,categories);

		categorySpinner.setAdapter(categoryAdapter);
		
		//Listen for New Category, and if New Category, show the New Category Dialog
		categorySpinner.setOnItemSelectedListener(
			new OnItemSelectedListener() {

		    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				//Create a dialog box to add a new category
				if (((TextView) view).getText() == "New Category...") {
					final AlertDialog.Builder categoryDialog = new AlertDialog.Builder(parent.getContext());
					final EditText input = new EditText(parent.getContext());
					
					categoryDialog.setTitle(getString(R.string.categoryDialogText)).setView(input);
					
					//If save, take the input text and add it to the category list
					categoryDialog.setPositiveButton(getString(R.string.categoryDialogSaveButton), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							categoryAdapter.add(input.getText().toString().trim());
							lastPosition = categoryAdapter.getCount() - 1;
							categorySpinner.setSelection(lastPosition);							
						}
					});

					//Cancel: do nothing
					categoryDialog.setNegativeButton(getString(R.string.categoryDialogCancelButton),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int whichButton) {
									categorySpinner.setSelection(lastPosition);
									dialog.cancel();
								}
							});
					
					categoryDialog.show();					
				}//End new Category
				else{
					lastPosition = pos;
				}
				//If activity, disable estimated time
				if (((TextView) view).getText() == "Activities") {
					estimatedTimeEdit.setText("");
					estimatedTimeEdit.setEnabled(false);
					alertSpinner.setEnabled(false);
				}
				else{
					estimatedTimeEdit.setEnabled(true);
					//TODO: alertSpinner.setEnabled(true);
				}
		    }//End onItemSelect

		    public void onNothingSelected(AdapterView parent) {
		      // Do nothing.
		    }
	});//End setOnItemSelectedListener
		
		// listens for click on the save button.
		OnClickListener buttonListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Set values
				item.setTitle(((EditText)findViewById(R.id.AddItemTitleEditText)).getText().toString());
				item.setCategory(((Spinner)findViewById(R.id.AddItemCategorySpinner)).getSelectedItem().toString());
				//item.setAlertMethod(((Spinner)findViewById(R.id.WarningModeSpinner)).getSelectedItem().toString());
				String estimatedTimeString = ((EditText)findViewById(R.id.AddItemEstimatedTimeEditText)).getText().toString();
				int estimatedTime = 0;
				try{
					  new java.math.BigInteger(estimatedTimeString);
						estimatedTime = Integer.valueOf(estimatedTimeString);
					}catch(NumberFormatException ex) {
						if (estimatedTimeString.equals(""))
							estimatedTime = 0;
						else{
								final AlertDialog.Builder categoryDialog = new AlertDialog.Builder(v.getContext());
								final TextView msg = new TextView(v.getContext());
								msg.setPadding(5,5,5,5);
								msg.setText("Invalid Input for Estimated Time.  Please enter a numeric value");
								
								categoryDialog.setTitle(getString(R.string.alarmActivityTimeUp)).setView(msg);
								
								//If save, take the input text and add it to the category list
								categoryDialog.setPositiveButton("Okay",
									new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int whichButton) {
										}
								});
								categoryDialog.show();	
								return;
						}
					}
				item.setEstimatedTime(estimatedTime);

	    		// And commit it to the database!
	    		TodoDatabase.singleton().putToDoTask(item);
	    		
				// Return new item as the activity exits
				setResult(RESULT_OK, new Intent().putExtra("ToDoItem", item));

				// Close activity
				finish();
			}//end onClick
		};//End new onClickListener

		// Cancel button.  do nothing
		OnClickListener cancelListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Close activity with Cancel result
				setResult(RESULT_CANCELED);
				finish();
			}
		};

		// set the button's listener
		findViewById(R.id.AddItemSaveButton).setOnClickListener(buttonListener);
		findViewById(R.id.AddItemCancelAddButton).setOnClickListener(cancelListener);
		
		initializeField();
	}//End onCreate

	
	private void initializeField() {
		if (getIntent().hasExtra("ToDoItem")) {
			// We're in edit mode
			item = getIntent().getParcelableExtra("ToDoItem");
			
			// Initialize fields with proper values
			((EditText)findViewById(R.id.AddItemTitleEditText)).setText(item.getTitle());
			
			((Spinner)findViewById(R.id.AddItemCategorySpinner)).setSelection(categories.indexOf(item.getCategory()));
			//TODO: set Alert Method
			//item.setAlertMethod(((Spinner)findViewById(R.id.WarningModeSpinner)).getSelectedItem().toString());
			((EditText)findViewById(R.id.AddItemEstimatedTimeEditText)).setText(String.valueOf(item.getEstimatedTime()));

			if (item.getCategory().equals("Activities")) {
				estimatedTimeEdit.setText("");
				estimatedTimeEdit.setEnabled(false);
				alertSpinner.setEnabled(false);
			}
		} else {
			// Add mode
			item = new ToDoListTask("","",0,0,"");
			((Spinner)findViewById(R.id.AddItemCategorySpinner)).setSelection(categories.indexOf("No Category"));
		}
	}	
}//End Class