package hu.janny.tomsschedule.ui.main.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hu.janny.tomsschedule.MainActivity;
import hu.janny.tomsschedule.R;
import hu.janny.tomsschedule.databinding.FragmentHomeBinding;
import hu.janny.tomsschedule.model.CustomActivity;
import hu.janny.tomsschedule.model.CustomActivityRecyclerAdapter;
import hu.janny.tomsschedule.model.firebase.FirebaseManager;
import hu.janny.tomsschedule.ui.main.MainViewModel;

public class HomeFragment extends Fragment {

//    private HomeViewModel homeViewModel;
    private MainViewModel mainViewModel;
    private FragmentHomeBinding binding;
    private CustomActivityRecyclerAdapter adapter;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        /*homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);*/
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.addCustomActivity.setVisibility(View.VISIBLE);

        recyclerSetup();

        /*LiveData<List<CustomActivity>> customActivities = homeViewModel.getCustomActivitiesLiveData();
        customActivities.observe(getViewLifecycleOwner(), new Observer<List<CustomActivity>>() {
            @Override
            public void onChanged(List<CustomActivity> customActivities) {
                // TODO: update custom activities list on UI
                binding.activitiesListRecyclerView.setAdapter(new CustomActivityRecyclerAdapter(customActivities));
            }
        });*/
        //LiveData<List<CustomActivity>> customActivities = mainViewModel.getAllActivitiesInList();
        mainViewModel.getActivitiesList().observe(getViewLifecycleOwner(), new Observer<List<CustomActivity>>() {
            @Override
            public void onChanged(List<CustomActivity> customActivities) {
                // TODO: update custom activities list on UI
                if(customActivities.isEmpty()) {
                    Toast.makeText(getActivity(), "Empty list!!! WHHY", Toast.LENGTH_LONG).show();
                }
                adapter.setActivityList(customActivities);
            }
        });
        return root;
    }

    private void recyclerSetup() {
        adapter = new CustomActivityRecyclerAdapter(R.layout.custom_activity_list_item);
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