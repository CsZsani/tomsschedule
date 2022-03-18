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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Binds layout
        binding = FragmentTipsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Gets a TipViewModel instance
        viewModel = new ViewModelProvider(requireActivity()).get(TipsViewModel.class);

        recyclerSetup(view);

        // Observer of the tip list
        viewModel.getTipsList().observe(getViewLifecycleOwner(), new Observer<List<Tip>>() {
            @Override
            public void onChanged(List<Tip> tips) {
                adapter.setTipsList(tips);
            }
        });
    }

    /**
     * Sets up the recycler adapter for the tip list.
     * Includes an onClickListener for navigating to the detail fragment of the clicked tip.
     */
    private void recyclerSetup(View fragView) {
        View.OnClickListener onClickListener = itemView -> {
            Tip item = (Tip) itemView.getTag();
            viewModel.findTip(item);
            Navigation.findNavController(itemView).navigate(R.id.action_nav_statistics_to_tipDetailFragment);
        };

        // Creates a new adapter with layout of the tip list, item onClickListener and context
        adapter = new TipsRecyclerAdapter(R.layout.tip_list_item, onClickListener, fragView.getContext());
        // Sets the layout recycler view of the tip
        binding.tipsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.tipsRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}