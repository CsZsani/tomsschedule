package hu.janny.tomsschedule.model;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
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
        ViewHolder(View itemView) {
            super(itemView);
            activityName = itemView.findViewById(R.id.activityNameInList);
            detailsText = itemView.findViewById(R.id.activityBasicInfosInList);
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
    }

    @Override
    public int getItemCount() {
        if(activityList == null) {
            return 0;
        }
        return activityList.size();
    }
}
