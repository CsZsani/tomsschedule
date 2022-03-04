package hu.janny.tomsschedule.ui.main.statistics;

import androidx.lifecycle.Observer;
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

import java.util.List;

import hu.janny.tomsschedule.R;
import hu.janny.tomsschedule.databinding.FragmentTipsBinding;
import hu.janny.tomsschedule.model.entities.Tip;
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
        viewModel.getTipsList().observe(getViewLifecycleOwner(), new Observer<List<Tip>>() {
            @Override
            public void onChanged(List<Tip> tips) {
                adapter.setActivityList(tips);
            }
        });
        //adapter.setActivityList(viewModel.getTips());

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