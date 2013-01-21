package cmu.mobilelab.taskmaster.database;

import android.os.Parcel;
import android.os.Parcelable;

public class ToDoListActivity extends ToDoListItem implements Parcelable {

	private long completedTime = 0;
	public ToDoListActivity(String title)
	{
		this.title = title;
	}
	public long getCompletedTime() {
		return completedTime;
	}
	public void setCompletedTime(long completedTime) {
		this.completedTime = completedTime;
	}
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(title);
		dest.writeLong(completedTime);
	}

	public ToDoListActivity(Parcel in){
		this.title = in.readString();
		this.completedTime = in.readLong();
	}
}
