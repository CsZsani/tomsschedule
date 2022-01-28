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

import hu.janny.tomsschedule.R;

public class CustomActivityRecyclerAdapter
        extends RecyclerView.Adapter<CustomActivityRecyclerAdapter.ViewHolder>
{
    List<CustomActivity> activityList;

    public CustomActivityRecyclerAdapter(List<CustomActivity> activityList) {
        this.activityList = activityList;
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
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.custom_activity_list_item, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        viewHolder.activityName.setText(activityList.get(i).name);
        viewHolder.detailsText.setText("Ide jon majd a fancy reszlet");
        viewHolder.divider.setBackgroundColor(Color.parseColor(activityList.get(i).color));
        viewHolder.beginActivity.setBackgroundColor(darkenColor(Color.parseColor(activityList.get(i).color)));
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
