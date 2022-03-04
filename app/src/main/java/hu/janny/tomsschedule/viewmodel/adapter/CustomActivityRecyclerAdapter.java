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

import java.util.Calendar;
import java.util.List;

import hu.janny.tomsschedule.MainActivity;
import hu.janny.tomsschedule.R;
import hu.janny.tomsschedule.model.helper.CustomActivityHelper;
import hu.janny.tomsschedule.model.helper.DateConverter;
import hu.janny.tomsschedule.model.entities.ActivityWithTimes;
import hu.janny.tomsschedule.model.entities.CustomActivity;

public class CustomActivityRecyclerAdapter
        extends RecyclerView.Adapter<CustomActivityRecyclerAdapter.ViewHolder>
{
    private final int listItemLayout;
    private final View.OnClickListener onClickListener;
    private final MainActivity mainActivity;
    private List<ActivityWithTimes> activityList;
    private final long todayMillis;

    public CustomActivityRecyclerAdapter(int layoutId, View.OnClickListener onClickListener, MainActivity mainActivity) {
        this.listItemLayout = layoutId;
        this.onClickListener = onClickListener;
        this.mainActivity = mainActivity;
        Calendar cal = Calendar.getInstance();
        int year  = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int date  = cal.get(Calendar.DATE);
        cal.clear();
        cal.set(year, month, date);
        todayMillis = cal.getTimeInMillis();
    }

    public void setActivityList(List<ActivityWithTimes> activityList) {
        this.activityList = activityList;
        notifyDataSetChanged();
    }

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

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        viewHolder.itemView.setOnClickListener(onClickListener);
        viewHolder.itemView.setTag(activityList.get(i));
        if(CustomActivityHelper.isFixActivity(activityList.get(i).customActivity.getName())) {
            viewHolder.activityName.setText(CustomActivityHelper.getStringResourceOfFixActivity(activityList.get(i).customActivity.getName()));
        } else {
            viewHolder.activityName.setText(activityList.get(i).customActivity.getName());
        }
        viewHolder.detailsText.setText("Ide jon majd a fancy reszlet");
        viewHolder.divider.setBackgroundColor(activityList.get(i).customActivity.getCol());
        viewHolder.beginActivity.setBackgroundColor(darkenColor(activityList.get(i).customActivity.getCol()));
        long timeSpentToday = CustomActivityHelper.getHowManyTimeWasSpentTodayOnAct(activityList.get(i).activityTimes);
        viewHolder.beginActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainActivity.startTimerActivity(activityList.get(viewHolder.getBindingAdapterPosition()).customActivity.getId(),
                        activityList.get(viewHolder.getBindingAdapterPosition()).customActivity.getName(),
                        timeSpentToday);
            }
        });
        viewHolder.todayTime.setText(DateConverter.durationConverterFromLongToStringForADay(timeSpentToday));
        viewHolder.detailsText.setText(detailsText(activityList.get(i).customActivity));
        //CustomActivityHelper.remainingTime(activityList.get(i).customActivity, activityList.get(i).activityTimes);
        long soFar = CustomActivityHelper.getSoFarLong(activityList.get(i).customActivity);
        long remaining = CustomActivityHelper.getRemainingLong(activityList.get(i).customActivity);
        if(notificationShown(soFar, remaining, activityList.get(i).customActivity)) {
            viewHolder.statusIndicator.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(mainActivity,
                    notificationColor(soFar, remaining, activityList.get(i).customActivity))));
            int intColor = ContextCompat.getColor(mainActivity,notificationColor(soFar, remaining, activityList.get(i).customActivity));
            //String hexColor = String.format("#%06X", (0xFFFFFF & intColor));
            //System.out.println(hexColor);
            viewHolder.statusIndicator.setVisibility(View.VISIBLE);
        }
    }

    private int notificationColor(long soFar, long remaining, CustomActivity activity) {
        if(greenColor(soFar, remaining, activity)) {
            return R.color.green_notification;
        } else if(redColor(soFar, remaining, activity)) {
            return R.color.red_notification;
        } else if(orangeColor(soFar, remaining, activity)) {
            return R.color.orange_notification;
        }
        return R.color.white;
    }

    private boolean greenColor(long soFar, long remaining, CustomActivity activity) {
        if(remaining == 0L && soFar >= activity.getDur()) {
            return true;
        }
        return false;
    }

    private boolean orangeColor(long soFar, long remaining, CustomActivity activity) {
        if(activity.gettT() == 1 && soFar < activity.getDur()) {
            return true;
        }else if(activity.ishFD() && activity.gettT() == 3 && soFar < activity.getDur()
                && CustomActivityHelper.todayIsAFixedDayAndWhat(activity.getCustomWeekTime()) != 0) {
            return true;
        } else if(activity.gettT() == 3 && soFar < activity.getDur()) {
            return true;
        } else if(activity.gettT() == 4 && soFar < activity.getDur()) {
            return true;
        }
        return false;
    }

    private boolean redColor(long soFar, long remaining, CustomActivity activity) {
        if(activity.gettT() == 2 && soFar < activity.getDur()) {
            return true;
        }
        if(activity.ishFD()) {
            if((activity.gettT() == 3 || activity.gettT() == 5) && CustomActivityHelper.todayIsAFixedDayAndWhat(activity.getCustomWeekTime()) != 0
                    && soFar < activity.getDur()) {
                return true;
            }
        }
        return false;
    }

    private boolean notificationShown(long soFar, long remaining, CustomActivity activity) {
        if(activity.gettT() == 0 || activity.gettN() == 1
                || (activity.geteD() != 0 && CustomActivityHelper.todayMillis() > activity.geteD()) ||
                (activity.ishFD() && CustomActivityHelper.todayIsAFixedDayAndWhat(activity.getCustomWeekTime()) != 0) ||
                (activity.gettT() == 1 && activity.geteD() == 0L && soFar > activity.getDur())) {
            return false;
        }
        return true;
    }

    private String detailsText(CustomActivity activity) {
        StringBuilder s = new StringBuilder("");
        String dl = CustomActivityHelper.detailsOnCardsDeadline(activity);
        boolean first = false;
        if(!dl.equals("")) {
            s.append(dl);
            first = true;
        }
        int reg = CustomActivityHelper.detailsOnCardRegularity(activity);
        if(reg != 0) {
            if(first) {
                s.append(", ");
            }
            s.append(mainActivity.getString(reg));
            if(activity.ishFD()) {
                s.append(" ");
                s.append(selectedWeeklyDaysToString(activity));
            }
            first = true;
        }
        String dur = CustomActivityHelper.detailsOnCardDuration(activity);
        if(!dur.equals("")){
            if(first) {
                s.append(", ");
            }
            s.append(dur);
        }
        return s.toString();
    }

    private String selectedWeeklyDaysToString(CustomActivity activity) {
        StringBuilder s = new StringBuilder("(");
        boolean notFirst = false;
        if(activity.getCustomWeekTime().getMon() != -1L) {
            s.append(mainActivity.getString(R.string.monday_short));
            notFirst = true;
        }
        if(activity.getCustomWeekTime().getTue() != -1L) {
            if(notFirst) {
                s.append(", ");
            }
            s.append(mainActivity.getString(R.string.tuesday_short));
            notFirst = true;
        }
        if(activity.getCustomWeekTime().getWed() != -1L) {
            if(notFirst) {
                s.append(", ");
            }
            s.append(mainActivity.getString(R.string.wednesday_short));
            notFirst = true;
        }
        if(activity.getCustomWeekTime().getThu() != -1L) {
            if(notFirst) {
                s.append(", ");
            }
            s.append(mainActivity.getString(R.string.thursday_short));
            notFirst = true;
        }
        if(activity.getCustomWeekTime().getFri() != -1L) {
            if(notFirst) {
                s.append(", ");
            }
            s.append(mainActivity.getString(R.string.friday_short));
            notFirst = true;
        }
        if(activity.getCustomWeekTime().getSat() != -1L) {
            if(notFirst) {
                s.append(", ");
            }
            s.append(mainActivity.getString(R.string.saturday_short));
            notFirst = true;
        }
        if(activity.getCustomWeekTime().getSun() != -1L) {
            if(notFirst) {
                s.append(", ");
            }
            s.append(mainActivity.getString(R.string.sunday_short));
        }
        s.append(")");
        return s.toString();
    }

    @Override
    public int getItemCount() {
        if(activityList == null) {
            return 0;
        }
        return activityList.size();
    }

    @ColorInt
    int darkenColor(@ColorInt int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f;
        return Color.HSVToColor(hsv);
    }
}
