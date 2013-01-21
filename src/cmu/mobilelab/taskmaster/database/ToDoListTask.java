package cmu.mobilelab.taskmaster.database;

import android.os.Parcel;
import android.os.Parcelable;

public class ToDoListTask extends ToDoListItem implements Parcelable{
		//private String title = "";
		private long id = Long.MIN_VALUE; // Mark that this item is not in the database
		private String category = "";
		private int estimatedTime = 0;
		private long completedTime = 0;
		private String alertMethod = "";
		private boolean completed = false;

		


		public ToDoListTask(String title, String category, int estimatedTime, long completedTime, String alertMethod) {
			this.title = title;
			this.category = category;
			
			this.estimatedTime = estimatedTime;
			this.completedTime = completedTime;
			this.alertMethod = alertMethod;
		}


		public ToDoListTask() {
			this("","",0,0,"");
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
			out.writeLong(id);
			out.writeString(category);
			out.writeInt(estimatedTime);
			out.writeLong(completedTime);
			out.writeString(alertMethod);
			out.writeInt(completed ? 1 : 0); //tertiary operator, woohoo!
		}

		public static final Parcelable.Creator<ToDoListTask> CREATOR = new Parcelable.Creator<ToDoListTask>() {
			public ToDoListTask createFromParcel(Parcel in) {
				return new ToDoListTask(in);
			}

			public ToDoListTask[] newArray(int size) {
				return new ToDoListTask[size];
			}
		};

		private ToDoListTask(Parcel in) {
			title = in.readString();
			id = in.readLong();		
			category = in.readString();
			estimatedTime = in.readInt();
			completedTime = in.readLong();
			alertMethod = in.readString();
			completed = (in.readInt() == 1);
		}


//		public String getTitle() {
//			return title;
//		}
//
//
//		public void setTitle(String title) {
//			this.title = title;
//		}


		public long getId() {
			return id;
		}
		
		public void setId(long id) {
			this.id = id;
		}
		
		public Double getCompletionPercentage() {
			return (double) ((double)completedTime/estimatedTime);
		}
		
		public String getCategory() {
			return category;
		}


		public void setCategory(String category) {
			this.category = category;
		}


		public int getEstimatedTime() {
			return estimatedTime;
		}


		public void setEstimatedTime(int estimatedTime) {
			this.estimatedTime = estimatedTime;
		}


		public long getCompletedTime() {
			return completedTime;
		}


		public void setCompletedTime(long completedTime) {
			this.completedTime = completedTime;
		}

		public void addCompletedTime(long timeElapsed) {
			this.completedTime += timeElapsed;
		}

		public String getAlertMethod() {
			return alertMethod;
		}


		public void setAlertMethod(String alertMethod) {
			this.alertMethod = alertMethod;
		}


		public boolean isCompleted() {
			return completed;
		}


		public void setCompleted(boolean completed) {
			this.completed = completed;
		}

}
