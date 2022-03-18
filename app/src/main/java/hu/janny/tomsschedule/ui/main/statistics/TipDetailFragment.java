package hu.janny.tomsschedule.ui.main.statistics;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import hu.janny.tomsschedule.databinding.FragmentTipDetailBinding;
import hu.janny.tomsschedule.model.entities.Tip;
import hu.janny.tomsschedule.viewmodel.TipsViewModel;

/**
 * This fragment shows the details of a selected tip.
 */
public class TipDetailFragment extends Fragment {

    private FragmentTipDetailBinding binding;
    private TipsViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Binds layout
        binding = FragmentTipDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Gets an TipsViewModel instance
        viewModel = new ViewModelProvider(requireActivity()).get(TipsViewModel.class);

        // Observer of the tip which shows its details
        viewModel.getTip().observe(getViewLifecycleOwner(), new Observer<Tip>() {
            @Override
            public void onChanged(Tip tip) {
                binding.tipDetailTitle.setText(tip.getTitle());
                binding.tipDetailText.setText(tip.getText());
                binding.tipDetailSource.setText(tip.getSource());
                binding.tipDetailAuthor.setText(tip.getAuthor());
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}