package hu.janny.tomsschedule.model.entities;

import androidx.annotation.NonNull;
import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

import hu.janny.tomsschedule.model.entities.ActivityTime;
import hu.janny.tomsschedule.model.entities.CustomActivity;

public class ActivityWithTimes {
    @Embedded
    public CustomActivity customActivity;
    @Relation(
            parentColumn = "activityId",
            entityColumn = "actId"
    )
    public List<ActivityTime> activityTimes;

    @NonNull
    @Override
    public String toString() {
        return customActivity.toString() + " " + activityTimes.size() + " " + activityTimes.toString();
    }
}
