package hu.janny.tomsschedule.ui.main.statistics;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/**
 * Adapter of the statistics tab layout.
 */
public class StatisticsAdapter extends FragmentStateAdapter {

    private final int TABS_NUM = 3;

    public StatisticsAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    /**
     * Returns the fragment on the position in the tab layout.
     *
     * @param position which fragment we should return
     * @return the fragment on the position in the tab layout
     */
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new PersonalStatisticsFragment();
            case 1:
                return new GlobalStatisticsFragment();
        }
        return new TipsFragment();
    }

    /**
     * Returns the number of tabs.
     *
     * @return the number of tabs
     */
    @Override
    public int getItemCount() {
        return TABS_NUM;
    }
}
