package hu.janny.tomsschedule.ui.main.account;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import hu.janny.tomsschedule.databinding.FragmentAccountBinding;
import hu.janny.tomsschedule.model.DateConverter;
import hu.janny.tomsschedule.model.User;
import hu.janny.tomsschedule.ui.main.MainViewModel;

public class AccountFragment extends Fragment {

    private FragmentAccountBinding binding;
    private MainViewModel mainViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        binding = FragmentAccountBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        mainViewModel.getUser().observe(getViewLifecycleOwner(), new Observer<User>() {
            @Override
            public void onChanged(@Nullable User u) {
                if(u != null) {
                    binding.accountEmail.setText(u.getEmail());
                    binding.accountBirthDate.setText(u.getBirthDate());
                    binding.accountAgeGroup.setText(u.ageGroup());
                    binding.accountName.setText(u.getName());
                    binding.accountGender.setText(u.getGenderForAccount());
                } else {
                    binding.accountEmail.setText("ERROR");
                    binding.accountBirthDate.setText("ERROR");
                    binding.accountAgeGroup.setText("ERROR");
                    binding.accountName.setText("ERROR");
                    binding.accountGender.setText("ERROR");
                }
            }
        });
        return root;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}