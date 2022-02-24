package hu.janny.tomsschedule.ui.main.statistics;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import hu.janny.tomsschedule.MainActivity;
import hu.janny.tomsschedule.R;
import hu.janny.tomsschedule.databinding.FragmentHomeBinding;
import hu.janny.tomsschedule.databinding.FragmentTipsBinding;
import hu.janny.tomsschedule.model.ActivityWithTimes;
import hu.janny.tomsschedule.model.CustomActivityHelper;
import hu.janny.tomsschedule.model.CustomActivityRecyclerAdapter;
import hu.janny.tomsschedule.ui.main.details.DetailFragment;
import hu.janny.tomsschedule.viewmodel.adapter.TipsRecyclerAdapter;

public class TipsFragment extends Fragment {

    private TipsViewModel mViewModel;
    private FragmentTipsBinding binding;
    private TipsRecyclerAdapter adapter;

    public static TipsFragment newInstance() {
        return new TipsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentTipsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;
    }

    private void recyclerSetup() {
        View.OnClickListener onClickListener = itemView -> {
            ActivityWithTimes item = (ActivityWithTimes) itemView.getTag();
            //System.out.println(item);
            Bundle arguments = new Bundle();
            arguments.putLong(DetailFragment.ARG_ITEM_ID, item.customActivity.getId());
            long timeSpentToday = CustomActivityHelper.getHowManyTimeWasSpentTodayOnAct(item.activityTimes);
            arguments.putLong(DetailFragment.TODAY_SO_FAR, timeSpentToday);
            //System.out.println(timeSpentToday);
            Navigation.findNavController(itemView).navigate(R.id.action_nav_home_to_detailFragment, arguments);
        };

        adapter = new TipsRecyclerAdapter(R.layout.custom_activity_list_item, onClickListener);
        binding.tipsRecyclerView.setLayoutManager(
                new LinearLayoutManager(getContext()));
        binding.tipsRecyclerView.setAdapter(adapter);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(TipsViewModel.class);
        // TODO: Use the ViewModel
    }

}