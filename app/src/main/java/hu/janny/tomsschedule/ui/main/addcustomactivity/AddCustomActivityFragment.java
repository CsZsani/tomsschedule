package hu.janny.tomsschedule.ui.main.addcustomactivity;

import androidx.lifecycle.ViewModelProvider;

import androidx.appcompat.app.AlertDialog;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.DatePicker;

import com.skydoves.colorpickerview.ColorEnvelope;
import com.skydoves.colorpickerview.ColorPickerDialog;
import com.skydoves.colorpickerview.ColorPickerView;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import hu.janny.tomsschedule.R;
import hu.janny.tomsschedule.databinding.FragmentAddCustomActivityBinding;
import hu.janny.tomsschedule.databinding.FragmentHomeBinding;
import hu.janny.tomsschedule.model.DateConverter;

public class AddCustomActivityFragment extends Fragment implements AdapterView.OnItemSelectedListener{

    private AddCustomActivityViewModel mViewModel;
    private FragmentAddCustomActivityBinding binding;
    private AlertDialog colorPickerDialog;
    final Calendar myCalendar= Calendar.getInstance();

    public static AddCustomActivityFragment newInstance() {
        return new AddCustomActivityFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAddCustomActivityBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        //return inflater.inflate(R.layout.fragment_add_custom_activity, container, false);
        intiColorPicker();
        binding.activityPriority.setOnItemSelectedListener(this);

        binding.activityColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                colorPickerDialog.show();
            }
        });

        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH,month + 1);
                myCalendar.set(Calendar.DAY_OF_MONTH,day);
                updateLabel();
            }
        };

        binding.activityDeadline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(getActivity(),date,myCalendar.get(Calendar.YEAR),myCalendar.get(Calendar.MONTH),myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        return root;
    }

    private void updateLabel(){
        //String myFormat="MM/dd/yy";
        //SimpleDateFormat dateFormat = new SimpleDateFormat(myFormat, Locale.US);
        //binding.activityDeadline.setText(dateFormat.format(myCalendar.getTime()));
        binding.activityDeadline.setText(DateConverter.makeDateStringForSimpleDateDialog(
                myCalendar.get(Calendar.DATE), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.YEAR)));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(AddCustomActivityViewModel.class);
        // TODO: Use the ViewModel
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private void intiColorPicker() {
        colorPickerDialog = new ColorPickerDialog.Builder(getActivity())
                .setTitle(R.string.nav_header_title)
                .setPreferenceName("MyColorPickerDialog")
                .setPositiveButton(getString(R.string.confirm),
                        new ColorEnvelopeListener() {
                            @Override
                            public void onColorSelected(ColorEnvelope envelope, boolean fromUser) {
                                binding.activityColor.setBackgroundColor(envelope.getColor());
                            }
                        })
                .setNegativeButton(getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                .attachAlphaSlideBar(false)
                .attachBrightnessSlideBar(true)
                .setBottomSpace(12) // set a bottom space between the last slidebar and buttons.
                .create();
    }



}