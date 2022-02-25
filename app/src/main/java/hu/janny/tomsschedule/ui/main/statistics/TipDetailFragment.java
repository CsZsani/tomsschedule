package hu.janny.tomsschedule.ui.main.statistics;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import hu.janny.tomsschedule.R;
import hu.janny.tomsschedule.databinding.FragmentTipDetailBinding;
import hu.janny.tomsschedule.model.Tip;
import hu.janny.tomsschedule.viewmodel.TipsViewModel;

public class TipDetailFragment extends Fragment {

    private FragmentTipDetailBinding binding;
    private TipsViewModel viewModel;
    public final static String TIP_ITEM_ID = "tip_id";
    private int tipId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTipDetailBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        viewModel = new ViewModelProvider(requireActivity()).get(TipsViewModel.class);

        /*if (getArguments() != null && getArguments().containsKey(TIP_ITEM_ID)) {
            tipId = getArguments().getInt(TIP_ITEM_ID);
            viewModel.findTip(tipId);
        }*/

        viewModel.getTip().observe(getViewLifecycleOwner(), new Observer<Tip>() {
            @Override
            public void onChanged(Tip tip) {
                binding.tipDetailTitle.setText(tip.getTitle());
                binding.tipDetailText.setText(tip.getText());
                binding.tipDetailSource.setText(tip.getSource());
                binding.tipDetailAuthor.setText(tip.getAuthor());
            }
        });

        return root;
    }
}