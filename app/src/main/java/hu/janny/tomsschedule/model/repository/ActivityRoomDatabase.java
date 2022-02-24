package hu.janny.tomsschedule.model.repository;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import hu.janny.tomsschedule.model.ActivityTime;
import hu.janny.tomsschedule.model.CustomActivity;
import hu.janny.tomsschedule.model.CustomWeekTime;
import hu.janny.tomsschedule.model.User;

@Database(entities = {User.class, CustomActivity.class, CustomWeekTime.class, ActivityTime.class}, version = 1)
public abstract class ActivityRoomDatabase extends RoomDatabase {

    public abstract CustomActivityDao customActivityDao();
    public abstract UserDao userDao();
    public abstract ActivityTimeDao activityTimeDao();
    private static ActivityRoomDatabase INSTANCE;

    static ActivityRoomDatabase getDatabase(final Context context) {
        if(INSTANCE == null) {
            synchronized (ActivityRoomDatabase.class) {
                if(INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            ActivityRoomDatabase.class, "activitydatabase.db").build();
                }
            }
        }
        return INSTANCE;
    }
}
