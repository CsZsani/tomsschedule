package hu.janny.tomsschedule.viewmodel.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import hu.janny.tomsschedule.MainActivity;
import hu.janny.tomsschedule.R;
import hu.janny.tomsschedule.model.ActivityWithTimes;
import hu.janny.tomsschedule.model.CustomActivityRecyclerAdapter;
import hu.janny.tomsschedule.model.Tip;

public class TipsRecyclerAdapter  extends RecyclerView.Adapter<TipsRecyclerAdapter.ViewHolder>{

    private final int listItemLayout;
    private final View.OnClickListener onClickListener;
    private List<Tip> tips;

    public TipsRecyclerAdapter(int layoutId, View.OnClickListener onClickListener) {
        this.listItemLayout = layoutId;
        this.onClickListener = onClickListener;
    }

    public void setActivityList(List<Tip> tips) {
        this.tips = tips;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TipsRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(listItemLayout, viewGroup, false);
        return new TipsRecyclerAdapter.ViewHolder(v);
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
    public void onBindViewHolder(@NonNull TipsRecyclerAdapter.ViewHolder viewHolder, int i) {
        viewHolder.itemView.setOnClickListener(onClickListener);
        viewHolder.itemView.setTag(tips.get(i));
    }

    @Override
    public int getItemCount() {
        if(tips == null) {
            return 0;
        }
        return tips.size();
    }
}
