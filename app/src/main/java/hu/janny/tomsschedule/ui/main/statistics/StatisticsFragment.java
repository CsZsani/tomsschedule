package hu.janny.tomsschedule.ui.main.statistics;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import hu.janny.tomsschedule.R;
import hu.janny.tomsschedule.databinding.FragmentStatisticsBinding;

public class StatisticsFragment extends Fragment {

    private final int[] tabsName = {R.string.personal_statistics,R.string.global_statistics, R.string.tips };
    private final int[] tabsIcons = {R.drawable.ic_person, R.drawable.ic_people, R.drawable.ic_star };

    private StatisticsViewModel statisticsViewModel;
    private FragmentStatisticsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        statisticsViewModel =
                new ViewModelProvider(this).get(StatisticsViewModel.class);

        binding = FragmentStatisticsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.statisticsTabLayout.addTab(binding.statisticsTabLayout.newTab().setText(R.string.personal_statistics).setIcon(R.drawable.ic_person));
        binding.statisticsTabLayout.addTab(binding.statisticsTabLayout.newTab().setText(R.string.global_statistics).setIcon(R.drawable.ic_people));
        binding.statisticsTabLayout.addTab(binding.statisticsTabLayout.newTab().setText(R.string.tips).setIcon(R.drawable.ic_star));

//        final StatisticsAdapter adapter = new StatisticsAdapter(getContext(),getParentFragmentManager(), binding.statisticsTabLayout.getTabCount());
//        binding.statisticsPager.setAdapter(adapter);
//        binding.statisticsPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
//        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
//            @Override
//            public void onTabSelected(TabLayout.Tab tab) {
//                viewPager.setCurrentItem(tab.getPosition());
//            }
//            @Override
//            public void onTabUnselected(TabLayout.Tab tab) {
//            }
//            @Override
//            public void onTabReselected(TabLayout.Tab tab) {
//            }
//        });
        StatisticsAdapter adapter = new StatisticsAdapter(this);
        binding.statisticsPager.setAdapter(adapter);

        /*StatisticsAdapter adapter = new StatisticsAdapter(getActivity().getSupportFragmentManager(), getActivity().getLifecycle());
        binding.statisticsPager.setAdapter(adapter);

        binding.statisticsTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                binding.statisticsPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        binding.statisticsPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                binding.statisticsTabLayout.selectTab(binding.statisticsTabLayout.getTabAt(position));
            }
        });*/

        new TabLayoutMediator(binding.statisticsTabLayout, binding.statisticsPager,
                (tab, position) -> tab.setText(tabsName[position]).setIcon(tabsIcons[position])
        ).attach();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}