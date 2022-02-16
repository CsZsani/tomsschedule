package hu.janny.tomsschedule.model;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;
import java.util.List;

import hu.janny.tomsschedule.MainActivity;
import hu.janny.tomsschedule.R;

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
        ViewHolder(View itemView) {
            super(itemView);
            activityName = itemView.findViewById(R.id.activityNameInList);
            detailsText = itemView.findViewById(R.id.activityBasicInfosInList);
            divider = itemView.findViewById(R.id.divider);
            beginActivity = itemView.findViewById(R.id.beginActivityInList);
            todayTime = itemView.findViewById(R.id.todayTime);
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
        viewHolder.beginActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainActivity.startTimerActivity(activityList.get(viewHolder.getBindingAdapterPosition()).customActivity.getId(),
                        activityList.get(viewHolder.getBindingAdapterPosition()).customActivity.getName());
            }
        });
        viewHolder.todayTime.setText(DateConverter.durationConverterFromLongToStringForADay(CustomActivityHelper.getHowManyTimeWasSpentTodayOnAct(
                activityList.get(i).activityTimes, todayMillis
        )));
        viewHolder.detailsText.setText(detailsText(activityList.get(i).customActivity));
        CustomActivityHelper.remainingTime(activityList.get(i).customActivity, activityList.get(i).activityTimes);
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
