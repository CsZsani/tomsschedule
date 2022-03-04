package hu.janny.tomsschedule.ui.main.account;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.DatePicker;

import java.util.Calendar;

import hu.janny.tomsschedule.R;
import hu.janny.tomsschedule.databinding.FragmentEditAccountBinding;
import hu.janny.tomsschedule.model.helper.DateConverter;
import hu.janny.tomsschedule.model.entities.User;
import hu.janny.tomsschedule.viewmodel.LoginRegisterViewModel;

public class EditAccountFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private FragmentEditAccountBinding binding;
    private DatePickerDialog datePickerDialog;

    private LoginRegisterViewModel viewModel;
    private final Calendar calendar = Calendar.getInstance();
    private User editUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Binds layout
        binding = FragmentEditAccountBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Gets a LoginRegisterViewModel instance
        viewModel = new ViewModelProvider(this).get(LoginRegisterViewModel.class);

        //Sets item selected listener on gender spinner
        binding.editGender.setOnItemSelectedListener(this);

        // Listens for logged in user's data
        viewModel.getCurrentUser().observe(getViewLifecycleOwner(), new Observer<User>() {
            @Override
            public void onChanged(User user) {
                editUser = user;
                binding.editUserName.setText(user.getName());
                setGender(user.getGender());
                setBirthDate(user.getBirthDate());
            }
        });

        initDatePicker();
        binding.editBirthDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                datePickerDialog.show();
            }
        });

        // Save changes button
        binding.editUserDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser(root);
            }
        });

        return root;
    }

    /**
     * Sets gender based on database data.
     *
     * @param gender the gender string to be set, "female" or "male"
     */
    private void setGender(String gender) {
        binding.editGender.post(new Runnable() {
            @Override
            public void run() {
                if (gender.equals("female")) {
                    binding.editGender.setSelection(0);
                } else {
                    binding.editGender.setSelection(1);
                }
            }
        });
    }

    /**
     * Sets birth date based on database data.
     *
     * @param birthDate the birthdate string to be set
     */
    private void setBirthDate(String birthDate) {
        binding.editBirthDate.setText(birthDate);
    }

    /**
     * Checks if every necessary data is provided by the user in correct form for updating.
     */
    public void registerUser(View fragView) {
        /*if (!InternetConnectionHelper.hasInternetConnection()) {
            Toast.makeText(getActivity(), R.string.connect_to_stable_internet, Toast.LENGTH_SHORT).show();
            return;
        }*/

        String name = binding.editUserName.getText().toString().trim();
        String birthDate = binding.editBirthDate.getText().toString().trim();
        String gender;
        if (binding.editGender.getSelectedItem().toString().equals(getString(R.string.female))) {
            gender = "female";
        } else {
            gender = "male";
        }

        if (!name.isEmpty()) {
            editUser.setName(name);
        }

        if (!birthDate.isEmpty()) {
            editUser.setBirthDate(birthDate);
            editUser.setAgeGroup(DateConverter.birthDateFromSimpleDateDialogToAgeGroupInt(birthDate));
        }

        editUser.setGender(gender);

        updateUser(editUser);
        Navigation.findNavController(fragView).popBackStack();
    }

    /**
     * Updates the given user in database(s).
     *
     * @param user user to be updated
     */
    private void updateUser(User user) {
        viewModel.updateUser(user);
        viewModel.updateUserInFirebase(user, getActivity());
    }

    /**
     * They are necessary for AdapterView.OnItemSelectedListener for gender spinner.
     */
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    /**
     * Initializes calendar for birth day pick.
     */
    private void initDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                calendar.clear();
                calendar.set(year, month, day);
                // Months counting begins with 0
                month = month + 1;
                String date = DateConverter.makeDateStringForSimpleDateDialog(day, month, year);
                binding.editBirthDate.setText(date);
            }
        };

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        int style = AlertDialog.THEME_HOLO_LIGHT;

        datePickerDialog = new DatePickerDialog(getContext(), style, dateSetListener, year, month, day);
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}