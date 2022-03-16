package hu.janny.tomsschedule.ui.main.account;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import hu.janny.tomsschedule.R;
import hu.janny.tomsschedule.databinding.FragmentAccountBinding;
import hu.janny.tomsschedule.model.entities.User;
import hu.janny.tomsschedule.viewmodel.MainViewModel;

public class AccountFragment extends Fragment {

    private FragmentAccountBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Binds layout
        binding = FragmentAccountBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Gets a MainViewModel instance
        MainViewModel mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // Displays the logged in user's data
        mainViewModel.getUser().observe(getViewLifecycleOwner(), new Observer<User>() {
            @Override
            public void onChanged(@Nullable User u) {
                if(u != null) {
                    binding.accountEmail.setText(u.getEmail());
                    binding.accountBirthDate.setText(u.getBirthDate());
                    binding.accountAgeGroup.setText(u.birthDateToAgeGroupInt());
                    binding.accountName.setText(u.getName());
                    binding.accountGender.setText(u.getGenderForAccount());
                } else {
                    binding.accountEmail.setText(getString(R.string.error));
                    binding.accountBirthDate.setText(getString(R.string.error));
                    binding.accountAgeGroup.setText(getString(R.string.error));
                    binding.accountName.setText(getString(R.string.error));
                    binding.accountGender.setText(getString(R.string.error));
                }
            }
        });

        // Navigates to edit user data fragment
        binding.editAccoutFragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(view).navigate(R.id.action_nav_account_to_editAccountFragment);
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}