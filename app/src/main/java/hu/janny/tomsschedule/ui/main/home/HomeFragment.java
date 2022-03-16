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

/**
 * This fragment displays the added activities list in a recycler view
 * and includes a fab icon for adding new activity.
 */
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private CustomActivityRecyclerAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // Binds layout
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Gets a MainViewModel instance
        MainViewModel mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // Displays add activity fab icon
        binding.addCustomActivity.setVisibility(View.VISIBLE);

        recyclerSetup();

        // Observes the activity list with the corresponding times
        mainViewModel.getActivitiesWithTimesList().observe(getViewLifecycleOwner(), new Observer<List<ActivityWithTimes>>() {
            @Override
            public void onChanged(List<ActivityWithTimes> customActivities) {
                adapter.setActivityList(customActivities);
            }
        });
    }

    /**
     * Sets up the recycler adapter fot he activity list.
     * Includes an onClickListener for navigating to the detail fragment of the clicked activity.
     */
    private void recyclerSetup() {
        View.OnClickListener onClickListener = itemView -> {
            ActivityWithTimes item = (ActivityWithTimes) itemView.getTag();

            Bundle arguments = new Bundle();
            arguments.putLong(DetailFragment.ARG_ITEM_ID, item.customActivity.getId());
            //long timeSpentToday = CustomActivityHelper.getHowManyTimeWasSpentTodayOnAct(item.activityTimes);

            Navigation.findNavController(itemView).navigate(R.id.action_nav_home_to_detailFragment, arguments);
        };
        // Creates a new adapter with layout of the activity list, item onClickListener and MainActivity context
        adapter = new CustomActivityRecyclerAdapter(R.layout.custom_activity_list_item,
                onClickListener, (MainActivity) getActivity());
        // Sets the layout recycler view of the activity
        binding.activitiesListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.activitiesListRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Sets add activity fab icon to gone
        binding.addCustomActivity.setVisibility(View.GONE);
        binding = null;
    }
}