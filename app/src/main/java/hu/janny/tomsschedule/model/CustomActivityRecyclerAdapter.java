package hu.janny.tomsschedule.model;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import hu.janny.tomsschedule.MainActivity;
import hu.janny.tomsschedule.R;

public class CustomActivityRecyclerAdapter
        extends RecyclerView.Adapter<CustomActivityRecyclerAdapter.ViewHolder>
{
    private final int listItemLayout;
    private View.OnClickListener onClickListener;
    private MainActivity mainActivity;
    List<CustomActivity> activityList;

    public CustomActivityRecyclerAdapter(int layoutId, View.OnClickListener onClickListener, MainActivity mainActivity) {
        this.listItemLayout = layoutId;
        this.onClickListener = onClickListener;
        this.mainActivity = mainActivity;
    }

    public void setActivityList(List<CustomActivity> activityList) {
        this.activityList = activityList;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView activityName;
        TextView detailsText;
        View divider;
        Button beginActivity;
        ViewHolder(View itemView) {
            super(itemView);
            activityName = itemView.findViewById(R.id.activityNameInList);
            detailsText = itemView.findViewById(R.id.activityBasicInfosInList);
            divider = itemView.findViewById(R.id.divider);
            beginActivity = itemView.findViewById(R.id.beginActivityInList);
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
        if(CustomActivityHelper.isFixActivity(activityList.get(i).getName())) {
            viewHolder.activityName.setText(CustomActivityHelper.getStringResourceOfFixActivity(activityList.get(i).getName()));
        } else {
            viewHolder.activityName.setText(activityList.get(i).getName());
        }
        viewHolder.detailsText.setText("Ide jon majd a fancy reszlet");
        viewHolder.divider.setBackgroundColor(activityList.get(i).getCol());
        viewHolder.beginActivity.setBackgroundColor(darkenColor(activityList.get(i).getCol()));
        viewHolder.beginActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainActivity.startTimerActivity(activityList.get(viewHolder.getBindingAdapterPosition()).getId(),
                        activityList.get(viewHolder.getBindingAdapterPosition()).getName());
            }
        });
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
