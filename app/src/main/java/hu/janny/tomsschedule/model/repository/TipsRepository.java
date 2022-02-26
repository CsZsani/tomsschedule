package hu.janny.tomsschedule.model.repository;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.common.util.IOUtils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import hu.janny.tomsschedule.model.CustomActivityHelper;
import hu.janny.tomsschedule.model.Tip;
import hu.janny.tomsschedule.model.firebase.FirebaseManager;

public class TipsRepository {

    private final MutableLiveData<Tip> tip = new MutableLiveData<>();
    private Tip tipFilter;
    private List<Tip> fixTips = new ArrayList<>();
    private List<Tip> cache = new ArrayList<>();
    private Context context;

    private final FirebaseStorage db;

    public TipsRepository(Application application) {
        db = FirebaseManager.storage;
        context = application.getApplicationContext();
        loadTips();
    }

    private void loadTips() {
        try {
            JSONObject obj = new JSONObject(loadFromAsset());
            JSONArray tips = obj.getJSONArray("tips");

            for (int i = 0; i < tips.length(); i++) {
                JSONObject tip = tips.getJSONObject(i);
                int id = tip.getInt("id");
                long time = tip.getLong("time");
                JSONObject titleObj = tip.getJSONObject("title");
                String title = titleObj.getString(Locale.getDefault().getLanguage());
                JSONObject textObj = tip.getJSONObject("text");
                String text = textObj.getString(Locale.getDefault().getLanguage());
                String author = tip.getString("author");
                String source = tip.getString("source");
                String color = tip.getString("hexColor");
                Tip newTip = new Tip(id, time, title, text, author, source, color);
                JSONArray tags = tip.getJSONArray("tags");
                for (int j = 0; j<tags.length(); j++) {
                    String tag = tags.getString(j);
                    newTip.addTag(tag);
                }
                fixTips.add(newTip);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String loadFromAsset() {
        String json = null;
        try {
            InputStream is = context.getAssets().open("base_tips.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    Handler handlerFilter = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            tip.setValue(tipFilter);
        }
    };

    public void findTipId(int id) {
        Tip tip = null;
        for(Tip t : fixTips) {
            if(t.getId() == id) {
                tip = t;
            }
        }
        if(tip != null) {
            tipFilter = tip;
        }
        for(Tip t : cache) {
            if(t.getId() == id) {
                tip = t;
            }
        }
        if(tip != null) {
            tipFilter = tip;
        } else {
            tipFilter = fixTips.get(0);
        }
        handlerFilter.sendEmptyMessage(0);
    }

    public void findTip(Tip tip) {
        if(fixTips.contains(tip)) {
            tipFilter = fixTips.get(fixTips.indexOf(tip));
        } else if(cache.contains(tip)) {
            tipFilter = cache.get(cache.indexOf(tip));
        } else {
            tipFilter = fixTips.get(0);
        }
        handlerFilter.sendEmptyMessage(0);
    }

    public List<Tip> getTips() {
        if(!cache.isEmpty()) {
            fixTips.addAll(cache);
        }
        return fixTips;
    }

    public MutableLiveData<Tip> getTip() {
        return tip;
    }
}
