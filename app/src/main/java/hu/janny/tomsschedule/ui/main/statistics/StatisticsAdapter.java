package hu.janny.tomsschedule.ui.main.statistics;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class StatisticsAdapter extends FragmentStateAdapter {

    private final int TABS_NUM = 3;

    public StatisticsAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    public StatisticsAdapter(FragmentActivity fa) {
        super(fa);
    }

    public StatisticsAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    /*public StatisticsAdapter(Context context, FragmentManager fm, int totalTabs) {
        super(fm);
        myContext = context;
        this.totalTabs = totalTabs;
    }
    // this is for fragment tabs
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                PersonalStatisticsFragment personalStatisticsFragment = new PersonalStatisticsFragment();
                return personalStatisticsFragment;
            case 1:
                GlobalStatisticsFragment globalStatisticsFragment = new GlobalStatisticsFragment();
                return globalStatisticsFragment;
            case 2:
                TipsFragment tipsFragment = new TipsFragment();
                return tipsFragment;
            default:
                return null;
        }
    }
    // this counts total number of tabs
    @Override
    public int getCount() {
        return totalTabs;
    }*/


    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                PersonalStatisticsFragment personalStatisticsFragment = new PersonalStatisticsFragment();
                return personalStatisticsFragment;
            case 1:
                GlobalStatisticsFragment globalStatisticsFragment = new GlobalStatisticsFragment();
                return globalStatisticsFragment;
        }
        TipsFragment tipsFragment = new TipsFragment();
        return tipsFragment;
    }

    @Override
    public int getItemCount() {
        return TABS_NUM;
    }
}
