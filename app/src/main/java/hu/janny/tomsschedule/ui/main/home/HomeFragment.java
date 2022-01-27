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
import hu.janny.tomsschedule.databinding.FragmentHomeBinding;
import hu.janny.tomsschedule.model.CustomActivity;
import hu.janny.tomsschedule.model.CustomActivityRecyclerAdapter;
import hu.janny.tomsschedule.model.firebase.FirebaseManager;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView.Adapter adapter;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        System.out.println("haho tortenik itt valami?111111111");
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.addCustomActivity.setVisibility(View.VISIBLE);

        layoutManager = new LinearLayoutManager(getActivity());
        binding.activitiesListRecyclerView.setLayoutManager(layoutManager);
        binding.activitiesListRecyclerView.setAdapter(new CustomActivityRecyclerAdapter(new ArrayList<>()));

        LiveData<List<CustomActivity>> customActivities = homeViewModel.getCustomActivitiesLiveData();
        customActivities.observe(getViewLifecycleOwner(), new Observer<List<CustomActivity>>() {
            @Override
            public void onChanged(List<CustomActivity> customActivities) {
                // TODO: update custom activities list on UI
                binding.activitiesListRecyclerView.setAdapter(new CustomActivityRecyclerAdapter(customActivities));
            }
        });

        /*binding.addCustomActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String key = FirebaseManager.database.getReference("customactivities")
                        .child(FirebaseManager.user.getUid()).push().getKey();
                CustomActivity activity = new CustomActivity("name", "#FF00FF", "note", 5, false, false, false);
                System.out.println("haho tortenik itt valami?");

                Map<String, Object> activityValues = activity.toMap();
                FirebaseManager.database.getReference().child("customactivities").child(FirebaseManager.auth.getUid()).child(key).setValue(activityValues)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getActivity(),"Added to db!",Toast.LENGTH_LONG).show();
                        }
                    })
                            .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            e.printStackTrace();
                            Toast.makeText(getActivity(),"Failed adding to db!",Toast.LENGTH_LONG).show();
                        }
                    });

            }
        });*/
        System.out.println("haho tortenik itt valami?2222222222");
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding.addCustomActivity.setVisibility(View.GONE);
        binding = null;
    }
}