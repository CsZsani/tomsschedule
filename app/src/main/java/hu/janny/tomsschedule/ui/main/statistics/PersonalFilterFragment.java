package hu.janny.tomsschedule.ui.main.statistics;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import hu.janny.tomsschedule.R;
import hu.janny.tomsschedule.databinding.FragmentPersonalFilterBinding;
import hu.janny.tomsschedule.databinding.FragmentStatisticsBinding;

public class PersonalFilterFragment extends Fragment {

    private FragmentPersonalFilterBinding binding;
    private StatisticsViewModel statisticsViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        statisticsViewModel =
                new ViewModelProvider(this).get(StatisticsViewModel.class);
        binding = FragmentPersonalFilterBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;
    }
}