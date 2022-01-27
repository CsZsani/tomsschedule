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

public class AccountFragment extends Fragment {

    private AccountViewModel accountViewModel;
    private FragmentAccountBinding binding;

    public static AccountFragment newInstance() {
        return new AccountFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        //return inflater.inflate(R.layout.account_fragment, container, false);
        accountViewModel =
                new ViewModelProvider(this).get(AccountViewModel.class);

        binding = FragmentAccountBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        accountViewModel.getUser().observe(getViewLifecycleOwner(), new Observer<User>() {
            @Override
            public void onChanged(@Nullable User u) {
                if(u != null) {
                    binding.accountEmail.setText(u.email);
                    binding.accountBirthDate.setText(DateConverter.longMillisToStringForSimpleDateDialog(DateConverter.stringMillisToLong(u.birthDate)));
                    binding.accountAgeGroup.setText(u.ageGroup());
                    binding.accountName.setText(u.name);
                    binding.accountGender.setText(u.getGender());
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



    /*@Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);
        // TODO: Use the ViewModel
    }*/


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}