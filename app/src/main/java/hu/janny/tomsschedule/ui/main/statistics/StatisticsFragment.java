package hu.janny.tomsschedule.ui.main.statistics;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayoutMediator;

import hu.janny.tomsschedule.R;
import hu.janny.tomsschedule.databinding.FragmentStatisticsBinding;

/**
 * This fragment is for displaying the statistics fragments and he tip fragment with a tab layout.
 */
public class StatisticsFragment extends Fragment {

    // The name and icons of the tabs: personal-, global statistics and tips
    private final int[] tabsName = {R.string.personal_statistics, R.string.global_statistics, R.string.tips};
    private final int[] tabsIcons = {R.drawable.ic_person, R.drawable.ic_people, R.drawable.ic_star};

    private FragmentStatisticsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Binds layout
        binding = FragmentStatisticsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Creates the tab layout
        binding.statisticsTabLayout.addTab(binding.statisticsTabLayout.newTab().setText(R.string.personal_statistics).setIcon(R.drawable.ic_person));
        binding.statisticsTabLayout.addTab(binding.statisticsTabLayout.newTab().setText(R.string.global_statistics).setIcon(R.drawable.ic_people));
        binding.statisticsTabLayout.addTab(binding.statisticsTabLayout.newTab().setText(R.string.tips).setIcon(R.drawable.ic_star));

        StatisticsAdapter adapter = new StatisticsAdapter(this);
        binding.statisticsPager.setAdapter(adapter);

        new TabLayoutMediator(binding.statisticsTabLayout, binding.statisticsPager,
                (tab, position) -> tab.setText(tabsName[position]).setIcon(tabsIcons[position])
        ).attach();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}