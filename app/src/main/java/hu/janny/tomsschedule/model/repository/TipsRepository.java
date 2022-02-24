package hu.janny.tomsschedule.model.repository;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

import hu.janny.tomsschedule.model.Tip;
import hu.janny.tomsschedule.model.firebase.FirebaseManager;

public class TipsRepository {

    private final MutableLiveData<Tip> tip = new MutableLiveData<>();
    private Tip tipFilter;
    private List<Tip> cache = new ArrayList<>();

    private final FirebaseStorage db;

    public TipsRepository(Application application) {
        db = FirebaseManager.storage;
    }

    Handler handlerFilter = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            tip.setValue(tipFilter);
        }
    };
}
