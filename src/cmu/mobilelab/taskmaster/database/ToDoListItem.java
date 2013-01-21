package cmu.mobilelab.taskmaster.database;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Encapsulates the data we need to display a ToDo List item. In particular, we
 * probably need at least a display name (title) and an id to tie the item back
 * to the corresponding database item.
 */

public abstract class ToDoListItem implements Parcelable
{
	protected String title = "";


	public String getTitle() {
		return title;
	}


	public void setTitle(String title) {
		this.title = title;
	}
}

