package hu.janny.tomsschedule.viewmodel.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

import hu.janny.tomsschedule.R;
import hu.janny.tomsschedule.model.entities.Tip;

/**
 * This is recycler adapter of the card view of tips.
 */
public class TipsRecyclerAdapter  extends RecyclerView.Adapter<TipsRecyclerAdapter.ViewHolder>{

    private final int listItemLayout;
    private final View.OnClickListener onClickListener;
    private List<Tip> tips;
    private Context context;

    public TipsRecyclerAdapter(int layoutId, View.OnClickListener onClickListener, Context context) {
        this.listItemLayout = layoutId;
        this.onClickListener = onClickListener;
        this.context = context;
    }

    /**
     * Sets the list to the given one.
     *
     * @param tips the tip list to display
     */
    public void setTipsList(List<Tip> tips) {
        this.tips = tips;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TipsRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(listItemLayout, viewGroup, false);
        return new TipsRecyclerAdapter.ViewHolder(v);
    }

    /**
     * The view holder
     */
    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tipTitle;
        TextView tipText;
        View divider;
        TextView tipAuthor;
        TextView tipSource;
        ChipGroup tipTags;
        ViewHolder(View itemView) {
            super(itemView);
            tipTitle = itemView.findViewById(R.id.tipTitle);
            tipText = itemView.findViewById(R.id.tipText);
            divider = itemView.findViewById(R.id.tipCardDivider);
            tipAuthor = itemView.findViewById(R.id.tipAuthor);
            tipSource = itemView.findViewById(R.id.tipSource);
            tipTags = itemView.findViewById(R.id.tipTags);
        }
    }

    /**
     * Binds the view holder and sets the UI.
     *
     * @param viewHolder view holder
     * @param i          the index of tip in the list
     */
    @Override
    public void onBindViewHolder(@NonNull TipsRecyclerAdapter.ViewHolder viewHolder, int i) {
        viewHolder.itemView.setOnClickListener(onClickListener);
        viewHolder.itemView.setTag(tips.get(i));
        viewHolder.tipTitle.setText(tips.get(i).getTitle());
        String text = tips.get(i).getText().substring(0, Math.min(tips.get(i).getText().length(), 100)) + "...";
        viewHolder.tipText.setText(text);
        viewHolder.tipAuthor.setText(tips.get(i).getAuthor());
        viewHolder.tipSource.setText(tips.get(i).getSource());
        viewHolder.divider.setBackgroundColor(tips.get(i).getColorInt());
        if(!tips.get(i).getTags().isEmpty()) {
            for(String s : tips.get(i).getTags()) {
                Chip chip = new Chip(context);
                chip.setId(ViewCompat.generateViewId());
                chip.setText(s);
                chip.setCheckable(false);
                viewHolder.tipTags.addView(chip);
            }
        }
    }

    /**
     * Returns how many tip are in the list.
     *
     * @return the list size of tip list
     */
    @Override
    public int getItemCount() {
        if(tips == null) {
            return 0;
        }
        return tips.size();
    }
}
