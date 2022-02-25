package hu.janny.tomsschedule.ui.main.statistics;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import hu.janny.tomsschedule.MainActivity;
import hu.janny.tomsschedule.R;
import hu.janny.tomsschedule.databinding.FragmentHomeBinding;
import hu.janny.tomsschedule.databinding.FragmentTipsBinding;
import hu.janny.tomsschedule.model.ActivityWithTimes;
import hu.janny.tomsschedule.model.CustomActivityHelper;
import hu.janny.tomsschedule.model.CustomActivityRecyclerAdapter;
import hu.janny.tomsschedule.model.Tip;
import hu.janny.tomsschedule.ui.main.details.DetailFragment;
import hu.janny.tomsschedule.viewmodel.StatisticsViewModel;
import hu.janny.tomsschedule.viewmodel.TipsViewModel;
import hu.janny.tomsschedule.viewmodel.adapter.TipsRecyclerAdapter;

public class TipsFragment extends Fragment {


    private FragmentTipsBinding binding;
    private TipsRecyclerAdapter adapter;
    private TipsViewModel viewModel;

    public static TipsFragment newInstance() {
        return new TipsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentTipsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        viewModel = new ViewModelProvider(requireActivity()).get(TipsViewModel.class);

        recyclerSetup(root);
        adapter.setActivityList(viewModel.getTips());

        return root;
    }

    private void recyclerSetup(View fragView) {
        View.OnClickListener onClickListener = itemView -> {
            Tip item = (Tip) itemView.getTag();
            Bundle arguments = new Bundle();
            arguments.putLong(TipDetailFragment.TIP_ITEM_ID, item.getId());
            viewModel.findTip(item);
            Navigation.findNavController(itemView).navigate(R.id.action_nav_statistics_to_tipDetailFragment, arguments);
        };

        adapter = new TipsRecyclerAdapter(R.layout.tip_list_item, onClickListener, fragView.getContext());
        binding.tipsRecyclerView.setLayoutManager(
                new LinearLayoutManager(getContext()));
        binding.tipsRecyclerView.setAdapter(adapter);
    }


}