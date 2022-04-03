package hu.janny.tomsschedule.viewmodel.adapter;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import hu.janny.tomsschedule.MainActivity;
import hu.janny.tomsschedule.R;
import hu.janny.tomsschedule.model.helper.CustomActivityHelper;
import hu.janny.tomsschedule.model.helper.DateConverter;
import hu.janny.tomsschedule.model.entities.ActivityWithTimes;
import hu.janny.tomsschedule.model.entities.CustomActivity;

/**
 * This adapter is for displaying the custom activities in a card view list in HomeFragment.
 */
public class CustomActivityRecyclerAdapter
        extends RecyclerView.Adapter<CustomActivityRecyclerAdapter.ViewHolder> {

    private final int listItemLayout;
    private final View.OnClickListener onClickListener;
    private final MainActivity mainActivity;
    private List<ActivityWithTimes> activityList;

    public CustomActivityRecyclerAdapter(int layoutId, View.OnClickListener onClickListener, MainActivity mainActivity) {
        this.listItemLayout = layoutId;
        this.onClickListener = onClickListener;
        this.mainActivity = mainActivity;
    }

    /**
     * Sets the list to the given one.
     *
     * @param activityList the activity list to display
     */
    public void setActivityList(List<ActivityWithTimes> activityList) {
        this.activityList = activityList;
        notifyDataSetChanged();
    }

    /**
     * The view holder
     */
    class ViewHolder extends RecyclerView.ViewHolder {
        TextView activityName;
        TextView detailsText;
        View divider;
        Button beginActivity;
        TextView todayTime;
        Button statusIndicator;

        ViewHolder(View itemView) {
            super(itemView);
            activityName = itemView.findViewById(R.id.activityNameInList);
            detailsText = itemView.findViewById(R.id.activityBasicInfosInList);
            divider = itemView.findViewById(R.id.divider);
            beginActivity = itemView.findViewById(R.id.beginActivityInList);
            todayTime = itemView.findViewById(R.id.todayTime);
            statusIndicator = itemView.findViewById(R.id.statusIndicatorInList);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(listItemLayout, viewGroup, false);
        return new ViewHolder(v);
    }

    /**
     * Binds the view holder and sets the UI.
     *
     * @param viewHolder view holder
     * @param i          the index af activity in the list
     */
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        viewHolder.itemView.setOnClickListener(onClickListener);
        viewHolder.itemView.setTag(activityList.get(i));
        if (CustomActivityHelper.isFixActivity(activityList.get(i).customActivity.getName())) {
            viewHolder.activityName.setText(CustomActivityHelper.getStringResourceOfFixActivity(activityList.get(i).customActivity.getName()));
        } else {
            viewHolder.activityName.setText(activityList.get(i).customActivity.getName());
        }
        viewHolder.divider.setBackgroundColor(activityList.get(i).customActivity.getCol());
        //System.out.printf("#%06X%n", (0xFFFFFF & activityList.get(i).customActivity.getCol()));
        viewHolder.beginActivity.setBackgroundColor(darkenColor(activityList.get(i).customActivity.getCol()));
        long todayMillis = CustomActivityHelper.todayMillis();
        long timeSpentToday = CustomActivityHelper.getHowManyTimeWasSpentTodayOnAct(activityList.get(i).activityTimes, todayMillis);
        viewHolder.todayTime.setText(DateConverter.durationConverterFromLongToStringForADay(timeSpentToday));
        viewHolder.detailsText.setText(detailsText(activityList.get(i).customActivity));
        viewHolder.beginActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainActivity.startTimerActivity(activityList.get(viewHolder.getBindingAdapterPosition()).customActivity.getId(),
                        activityList.get(viewHolder.getBindingAdapterPosition()).customActivity.getName());
            }
        });

        long soFar = CustomActivityHelper.getSoFarLong(activityList.get(i).customActivity);
        long remaining = CustomActivityHelper.getRemainingLong(activityList.get(i).customActivity);
        if (notificationShown(soFar, remaining, activityList.get(i).customActivity)) {
            viewHolder.statusIndicator.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(mainActivity,
                    notificationColor(soFar, remaining, activityList.get(i).customActivity))));
            viewHolder.statusIndicator.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Returns which colour we have to display.
     *
     * @param soFar     the time spent so far to reach the goal duration
     * @param remaining the time remaining to reach the goal duration
     * @param activity  the activity
     * @return color resource
     */
    private int notificationColor(long soFar, long remaining, CustomActivity activity) {
        if (greenColor(soFar, remaining, activity)) {
            return R.color.green_notification;
        } else if (orangeColor(soFar, remaining, activity)) {
            return R.color.orange_notification;
        } else if (redColor(soFar, remaining, activity)) {
            return R.color.red_notification;
        }
        return R.color.white;
    }

    /**
     * Returns true if we have to show green colour - means that the goal is reached.
     *
     * @param soFar     the time spent so far to reach the goal duration
     * @param remaining the time remaining to reach the goal duration
     * @param activity  the activity
     * @return true if we have to display green colour
     */
    private boolean greenColor(long soFar, long remaining, CustomActivity activity) {
        return remaining == 0L && soFar >= activity.getDur();
    }

    /**
     * Returns true if we have to show orange colour - means that the goal is not reached but today
     * is not selected (not daily, custom duration).
     *
     * @param soFar     the time spent so far to reach the goal duration
     * @param remaining the time remaining to reach the goal duration
     * @param activity  the activity
     * @return true if we have to display orange colour
     */
    private boolean orangeColor(long soFar, long remaining, CustomActivity activity) {
        if (activity.gettT() == 1 && soFar < activity.getDur()) {
            return true;
        } else if (activity.ishFD() && activity.gettT() == 3 && soFar < activity.getDur()
                && CustomActivityHelper.todayIsAFixedDayAndWhat(activity.getCustomWeekTime()) != 0) {
            return true;
        } else if (activity.gettT() == 3 && soFar < activity.getDur()) {
            return true;
        } else if (activity.gettT() == 4 && soFar < activity.getDur()) {
            return true;
        }
        return false;
    }

    /**
     * Returns true if we have to show red colour - means that the goal is not reached but today
     * is selected, so we must do this activity today (daily, custom duration).
     *
     * @param soFar     the time spent so far to reach the goal duration
     * @param remaining the time remaining to reach the goal duration
     * @param activity  the activity
     * @return true if we have to display red colour
     */
    private boolean redColor(long soFar, long remaining, CustomActivity activity) {
        if (activity.gettT() == 2 && soFar < activity.getDur()) {
            return true;
        }
        if (activity.ishFD()) {
            if ((activity.gettT() == 3 || activity.gettT() == 5) && CustomActivityHelper.todayIsAFixedDayAndWhat(activity.getCustomWeekTime()) != 0
                    && soFar < CustomActivityHelper.todayIsAFixedDayAndDuration(activity.getCustomWeekTime())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if we have to show notification colour and false if we do not have to.
     *
     * @param soFar     the time spent so far to reach the goal duration
     * @param remaining the time remaining to reach the goal duration
     * @param activity  the activity
     * @return true if we have to show notification colour, false otherwise
     */
    private boolean notificationShown(long soFar, long remaining, CustomActivity activity) {
        if (activity.gettT() == 0 || activity.gettN() == 6 || activity.gettN() == 1 || soFar == -1L ||
                (activity.ishFD() && CustomActivityHelper.todayIsAFixedDayAndWhat(activity.getCustomWeekTime()) == 0) ||
                (activity.gettT() == 1 && activity.geteD() == 0L && soFar >= activity.getDur())) {
            return false;
        }
        return true;
    }

    /**
     * Returns a string for displaying the type of activity on the card.
     *
     * @param activity the activity
     * @return string of details
     */
    private String detailsText(CustomActivity activity) {
        StringBuilder s = new StringBuilder("");
        // Deadline
        String dl = CustomActivityHelper.detailsOnCardsDeadline(activity);
        boolean first = false;
        if (!dl.equals("")) {
            s.append(dl);
            first = true;
        }
        // Regularity
        int reg = CustomActivityHelper.detailsOnCardRegularity(activity);
        if (reg != 0) {
            if (first) {
                s.append(", ");
            }
            s.append(mainActivity.getString(reg));
            if (activity.ishFD()) {
                s.append(" ");
                s.append(selectedWeeklyDaysToString(activity));
            }
            first = true;
        }
        // Duration
        String dur = CustomActivityHelper.detailsOnCardDuration(activity);
        if (activity.gettT() == 5) {
            mainActivity.getString(R.string.custom);
        }
        if (!dur.equals("")) {
            if (first) {
                s.append(", ");
            }
            s.append(dur);
        }
        return s.toString();
    }

    /**
     * Returns the selected days of week for displaying on the card.
     *
     * @param activity the current activity
     * @return string to display
     */
    private String selectedWeeklyDaysToString(CustomActivity activity) {
        StringBuilder s = new StringBuilder("(");
        boolean notFirst = false;
        if (activity.getCustomWeekTime().getMon() != -1L) {
            s.append(mainActivity.getString(R.string.monday_short));
            notFirst = true;
        }
        if (activity.getCustomWeekTime().getTue() != -1L) {
            if (notFirst) {
                s.append(" ");
            }
            s.append(mainActivity.getString(R.string.tuesday_short));
            notFirst = true;
        }
        if (activity.getCustomWeekTime().getWed() != -1L) {
            if (notFirst) {
                s.append(" ");
            }
            s.append(mainActivity.getString(R.string.wednesday_short));
            notFirst = true;
        }
        if (activity.getCustomWeekTime().getThu() != -1L) {
            if (notFirst) {
                s.append(" ");
            }
            s.append(mainActivity.getString(R.string.thursday_short));
            notFirst = true;
        }
        if (activity.getCustomWeekTime().getFri() != -1L) {
            if (notFirst) {
                s.append(" ");
            }
            s.append(mainActivity.getString(R.string.friday_short));
            notFirst = true;
        }
        if (activity.getCustomWeekTime().getSat() != -1L) {
            if (notFirst) {
                s.append(" ");
            }
            s.append(mainActivity.getString(R.string.saturday_short));
            notFirst = true;
        }
        if (activity.getCustomWeekTime().getSun() != -1L) {
            if (notFirst) {
                s.append(" ");
            }
            s.append(mainActivity.getString(R.string.sunday_short));
        }
        s.append(")");
        return s.toString();
    }

    /**
     * Returns how many activity are in the list.
     *
     * @return the list size of activity list
     */
    @Override
    public int getItemCount() {
        if (activityList == null) {
            return 0;
        }
        return activityList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Darkens the given colour.
     *
     * @param color the colour to be darker
     * @return color int of the darker colour
     */
    @ColorInt
    int darkenColor(@ColorInt int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f;
        return Color.HSVToColor(hsv);
    }
}
