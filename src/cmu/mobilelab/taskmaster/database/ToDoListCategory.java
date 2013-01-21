package cmu.mobilelab.taskmaster.database;

import android.os.Parcel;
import android.os.Parcelable;

public class ToDoListCategory extends ToDoListItem implements Parcelable {

	public ToDoListCategory(String title) {
		this.title = title;
	}


	public ToDoListCategory() {
		this("");
	}
	
	/*
	 *  Methods to implement Parcelable so we can pass these between activities
	 */
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeString(title);
	}

	public static final Parcelable.Creator<ToDoListCategory> CREATOR = new Parcelable.Creator<ToDoListCategory>() {
		public ToDoListCategory createFromParcel(Parcel in) {
			return new ToDoListCategory(in);
		}

		public ToDoListCategory[] newArray(int size) {
			return new ToDoListCategory[size];
		}
	};

	private ToDoListCategory(Parcel in) {
		title = in.readString();
	}


}
