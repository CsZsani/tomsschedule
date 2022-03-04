package hu.janny.tomsschedule.ui.main.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.List;

import hu.janny.tomsschedule.MainActivity;
import hu.janny.tomsschedule.R;
import hu.janny.tomsschedule.databinding.FragmentHomeBinding;
import hu.janny.tomsschedule.model.entities.ActivityWithTimes;
import hu.janny.tomsschedule.model.helper.CustomActivityHelper;
import hu.janny.tomsschedule.viewmodel.adapter.CustomActivityRecyclerAdapter;
import hu.janny.tomsschedule.viewmodel.MainViewModel;
import hu.janny.tomsschedule.ui.main.details.DetailFragment;

public class HomeFragment extends Fragment {

//    private HomeViewModel homeViewModel;
    private MainViewModel mainViewModel;
    private FragmentHomeBinding binding;
    private CustomActivityRecyclerAdapter adapter;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.addCustomActivity.setVisibility(View.VISIBLE);

        recyclerSetup();

        //mainViewModel.getAllActivitiesInList()
        /*mainViewModel.getActivitiesListEntities().observe(getViewLifecycleOwner(), new Observer<List<CustomActivity>>() {
            @Override
            public void onChanged(List<CustomActivity> customActivities) {
                adapter.setActivityList(customActivities);
            }
        });*/
        mainViewModel.getActivitiesWithTimesList().observe(getViewLifecycleOwner(), new Observer<List<ActivityWithTimes>>() {
            @Override
            public void onChanged(List<ActivityWithTimes> customActivities) {
                adapter.setActivityList(customActivities);
            }
        });
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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

        adapter = new CustomActivityRecyclerAdapter(R.layout.custom_activity_list_item, onClickListener, (MainActivity)getActivity());
        binding.activitiesListRecyclerView.setLayoutManager(
                new LinearLayoutManager(getContext()));
        binding.activitiesListRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding.addCustomActivity.setVisibility(View.GONE);
        binding = null;
    }
}