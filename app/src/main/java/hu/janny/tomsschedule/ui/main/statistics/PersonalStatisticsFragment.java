package hu.janny.tomsschedule.ui.main.statistics;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import hu.janny.tomsschedule.R;
import hu.janny.tomsschedule.databinding.FragmentPersonalStatisticsBinding;
import hu.janny.tomsschedule.databinding.FragmentStatisticsBinding;

public class PersonalStatisticsFragment extends Fragment {

    private PersonalStatisticsViewModel mViewModel;
    private FragmentPersonalStatisticsBinding binding;

    public static PersonalStatisticsFragment newInstance() {
        return new PersonalStatisticsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        //return inflater.inflate(R.layout.fragment_personal_statistics, container, false);
        binding = FragmentPersonalStatisticsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(root).navigate(R.id.action_nav_statistics_to_personalFilterFragment);
            }
        });

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(PersonalStatisticsViewModel.class);
        // TODO: Use the ViewModel
    }

}