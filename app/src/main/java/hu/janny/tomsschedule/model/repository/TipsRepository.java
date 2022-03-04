package hu.janny.tomsschedule.model.repository;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import hu.janny.tomsschedule.model.entities.Tip;
import hu.janny.tomsschedule.model.firebase.FirebaseManager;

public class TipsRepository {

    private final MutableLiveData<Tip> tip = new MutableLiveData<>();
    private final MutableLiveData<List<Tip>> tips = new MutableLiveData<>();
    private List<Tip> allTips = new ArrayList<>();
    private Tip tipFilter;
    private List<Tip> fixTips = new ArrayList<>();
    private List<Tip> cache = new ArrayList<>();
    private Context context;

    private final FirebaseStorage db;

    public TipsRepository(Application application) {
        db = FirebaseManager.storage;
        context = application.getApplicationContext();
        loadFixTips();
        loadFirebase();
    }

    Handler handlerTips = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            tips.setValue(allTips);
        }
    };

    Handler handlerFilter = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            tip.setValue(tipFilter);
        }
    };

    private void loadFirebase() {
        System.out.println(cache + " cahce");
        if(cache.isEmpty()) {
            StorageReference storageReference = db.getReference().child("tips/tips.json");
            loadFromFirebaseReference(storageReference);
        }
    }

    private void loadFromFirebaseReference(StorageReference storageReference) {
        try {
            File localFile = File.createTempFile("tips", ".json");

            storageReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    // Local temp file has been created
                    String json = readFromTempFile(localFile);
                    if(json != null) {
                        try {
                            JSONObject obj = new JSONObject(json);
                            loadTips(obj, cache);
                            setAllTips();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setAllTips() {
        List<Tip> list = new ArrayList<>(fixTips);
        if(!cache.isEmpty()) {
            list.addAll(cache);
        }
        allTips = list;
        handlerTips.sendEmptyMessage(0);
    }

    private String readFromTempFile(File localFile) {
        String json = null;
        try {
            InputStream is = new FileInputStream(localFile);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return json;
    }

    private void loadFixTips() {
        if(fixTips.isEmpty()) {
            try {
                JSONObject obj = new JSONObject(loadFromAsset());
                loadTips(obj, fixTips);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadTips(JSONObject jsonObject, List<Tip> list) {
        try {
            JSONArray tips = jsonObject.getJSONArray("tips");

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
                list.add(newTip);
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
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

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
        List<Tip> list = new ArrayList<>(fixTips);
        if(!cache.isEmpty()) {
            list.addAll(cache);
        }
        return list;
    }

    public MutableLiveData<Tip> getTip() {
        return tip;
    }

    public MutableLiveData<List<Tip>> getTipsList() {
        return tips;
    }
}
