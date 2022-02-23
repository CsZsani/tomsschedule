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
import hu.janny.tomsschedule.databinding.FragmentGlobalFilterBinding;
import hu.janny.tomsschedule.databinding.FragmentGlobalStatisticsBinding;
import hu.janny.tomsschedule.viewmodel.GlobalStatisticsViewModel;

public class GlobalStatisticsFragment extends Fragment {

    private FragmentGlobalStatisticsBinding binding;
    private GlobalStatisticsViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireActivity()).get(GlobalStatisticsViewModel.class);
        binding = FragmentGlobalStatisticsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        initFilterButton(root);

        return root;
    }

    private void initFilterButton(View fragView) {
        binding.gFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(fragView).navigate(R.id.action_nav_statistics_to_globalFilterFragment);
            }
        });
    }


}