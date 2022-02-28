package hu.janny.tomsschedule.ui.main.settings;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import hu.janny.tomsschedule.R;
import hu.janny.tomsschedule.databinding.FragmentSettingsBinding;
import hu.janny.tomsschedule.model.User;
import hu.janny.tomsschedule.viewmodel.BackUpViewModel;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private AlertDialog saveDialog;
    private AlertDialog restoreDialog;
    private BackUpViewModel viewModel;
    private String userId;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        viewModel = new ViewModelProvider(this).get(BackUpViewModel.class);

        setUpSaveDialog();
        setUpRestoreDialog();

        viewModel.getUser().observe(getViewLifecycleOwner(), new Observer<User>() {
            @Override
            public void onChanged(User user) {
                userId = user.getUid();
            }
        });

        viewModel.getReady().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean ready) {
                if(ready) {
                    Toast.makeText(getActivity(), "Save/restore is done!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.createBackupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveDialog.show();
            }
        });

        binding.restoreBackupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                restoreDialog.show();
            }
        });

        return root;
    }

    private void setUpSaveDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.save_data_warning);
        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(getActivity(), "Saving is in progress!", Toast.LENGTH_LONG).show();
                viewModel.saveData(userId);
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });
        saveDialog = builder.create();
    }

    private void setUpRestoreDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.download_data_warning);
        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(getActivity(), "Restoring is in progress!", Toast.LENGTH_LONG).show();
                viewModel.restoreBackup(userId);
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });
        restoreDialog = builder.create();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}