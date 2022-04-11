package hu.janny.tomsschedule.ui.main.settings;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import hu.janny.tomsschedule.R;
import hu.janny.tomsschedule.databinding.FragmentSettingsBinding;
import hu.janny.tomsschedule.model.helper.InternetConnectionHelper;
import hu.janny.tomsschedule.model.helper.SuccessCallback;
import hu.janny.tomsschedule.viewmodel.BackUpViewModel;

/**
 * Settings of the application.
 * Includes creating and restoring backups.
 */
public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private BackUpViewModel viewModel;

    // Dialogs for creating or restoring backup confirmation
    private AlertDialog saveDialog;
    private AlertDialog restoreDialog;
    // Current user's id
    private String userId;

    private final int[] loaded = new int[1];
    private final int[] success = new int[1];

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // Binds layout
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Gets a BackUpViewModel instance
        viewModel = new ViewModelProvider(this).get(BackUpViewModel.class);

        loaded[0] = 0;
        success[0] = 0;

        setUpSaveDialog();
        setUpRestoreDialog();

        // Gets the current user's id
        viewModel.getUser().observe(getViewLifecycleOwner(), user -> userId = user.getUid());

        binding.createBackupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!InternetConnectionHelper.hasInternetConnection()) {
                    Toast.makeText(getContext(), R.string.connect_to_stable_internet, Toast.LENGTH_SHORT).show();
                    return;
                }
                saveDialog.show();
            }
        });

        binding.restoreBackupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!InternetConnectionHelper.hasInternetConnection()) {
                    Toast.makeText(getContext(), R.string.connect_to_stable_internet, Toast.LENGTH_SHORT).show();
                    return;
                }
                restoreDialog.show();
            }
        });

    }

    /**
     * Sets up the confirmation dialog for creating backup. Has two button: confirm and cancel
     */
    private void setUpSaveDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.save_data_warning);
        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                showProgress(R.string.create_backup_progress);
                loaded[0] = 0;
                success[0] = 0;
                if (!viewModel.saveData(userId, new CreateBackupSuccess())) {
                    // If the save failed
                    hideProgress();
                    Toast.makeText(getContext(), getString(R.string.create_backup_fail), Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });
        saveDialog = builder.create();
    }

    /**
     * Shows the progress bar and text and hides the scroll view
     *
     * @param resId the id of the resource to display as a text above progress bar
     */
    private void showProgress(int resId) {
        binding.settingsScrollView.setVisibility(View.GONE);
        binding.settingsProgressBar.setVisibility(View.VISIBLE);
        binding.settingsProgressText.setText(getString(resId));
        binding.settingsProgressText.setVisibility(View.VISIBLE);
    }

    /**
     * Hides the progress bar and text and shows the scroll view
     */
    private void hideProgress() {
        binding.settingsScrollView.setVisibility(View.VISIBLE);
        binding.settingsProgressBar.setVisibility(View.GONE);
        binding.settingsProgressText.setVisibility(View.GONE);
    }

    /**
     * This is used when we create a backup. If the activities and times are saved from local database into Firebase,
     * then it called and it modify the UI and informs the user about the successfulness of the save.
     */
    private class CreateBackupSuccess implements SuccessCallback {

        @Override
        public void onCallback(boolean successful) {
            if (loaded[0] == 1) {
                loaded[0] = 0;
                hideProgress();
                if (successful && success[0] == 1) {
                    Toast.makeText(getContext(), getString(R.string.create_backup_done), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getContext(), getString(R.string.create_backup_fail), Toast.LENGTH_LONG).show();
                }
                success[0] = 0;
            } else if (loaded[0] == 0) {
                loaded[0] = 1;
                if (successful) {
                    success[0] = 1;
                }
            }
        }
    }

    /**
     * This is used when we restore a backup. If the activities and times are saved from Firebase into local database,
     * then it called and it modify the UI and informs the user about the successfulness of the save.
     */
    private class RestoreBackupSuccess implements SuccessCallback {

        @Override
        public void onCallback(boolean successful) {
            hideProgress();
            if (successful) {
                Toast.makeText(getContext(), getString(R.string.restore_backup_done), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getContext(), getString(R.string.restore_backup_fail), Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Sets up the confirmation dialog for restoring backup. Has two button: confirm and cancel
     */
    private void setUpRestoreDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.download_data_warning);
        builder.setPositiveButton(R.string.confirm, (dialogInterface, i) -> {
            showProgress(R.string.restore_backup_progress);
            viewModel.restoreBackup(userId, new RestoreBackupSuccess());
        });
        builder.setNegativeButton(R.string.cancel, (dialog, id) -> {
            // User cancelled the dialog
        });
        restoreDialog = builder.create();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}