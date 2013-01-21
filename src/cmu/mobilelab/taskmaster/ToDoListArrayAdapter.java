package cmu.mobilelab.taskmaster;

import java.util.ArrayList;

import cmu.mobilelab.taskmaster.database.ToDoListItem;
import cmu.mobilelab.taskmaster.database.ToDoListTask;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * This adapter is an extension to the ArrayAdapter, to be able to display more interesting
 * list items, and for more flexibility in implementation
 *
 */
public class ToDoListArrayAdapter extends ArrayAdapter<ToDoListItem> {
	Home home;
	
	public ToDoListArrayAdapter(Home home, int listItem,
			ArrayList<ToDoListItem> listAdapterBack) {
		super(home, listItem, listAdapterBack);
		this.home = home;
	}

	/**
	 * Create view for a given list item
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ToDoListItem item = getItem(position);
		
		LayoutInflater inflater = home.getLayoutInflater(); 
        View retVal=inflater.inflate(R.layout.list_item, null); 
		TextView text = (TextView)retVal.findViewById(R.id.listItemTitle);
		if(item instanceof ToDoListTask){

			text.setText("    " + item.getTitle());
		}
		else{
			text.setText(item.getTitle());
			text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
		}
		return retVal;
	}

}
