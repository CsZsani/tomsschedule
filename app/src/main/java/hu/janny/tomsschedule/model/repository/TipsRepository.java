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
import hu.janny.tomsschedule.model.helper.InternetConnectionHelper;

/**
 * The repository of tips. It gets them from assets\base_tips.json and from Firebase Storage (tips\tips.json).
 */
public class TipsRepository {
    // Tip we searched for, clicked on to view its details
    private final MutableLiveData<Tip> tip = new MutableLiveData<>();
    // Tip we searched for, clicked on to view its details
    private Tip tipFilter;
    // The list of tips
    private final MutableLiveData<List<Tip>> tips = new MutableLiveData<>();
    // The list of tips
    private List<Tip> allTips = new ArrayList<>();

    // Comes from assets\base_tips.json
    private List<Tip> fixTips = new ArrayList<>();
    // Comes from Firebase Storage tips.json
    private List<Tip> cache = new ArrayList<>();

    private final Context context;

    private final FirebaseStorage db;

    public TipsRepository(Application application) {
        db = FirebaseManager.storage;
        context = application.getApplicationContext();

        loadFixTips();
        loadFirebase();
    }

    /**
     * Sets the tips list to allTips when we loaded the tips.
     */
    Handler handlerTips = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            tips.setValue(allTips);
        }
    };

    /**
     * Sets the tip to tipFilter when we get the we have searched for.
     */
    Handler handlerFilter = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            tip.setValue(tipFilter);
        }
    };

    /**
     * Loads tips from Firebase Storage tips\tips.json.
     */
    private void loadFirebase() {
        if (cache.isEmpty()) {
            StorageReference storageReference = db.getReference().child("tips/tips.json");
            loadFromFirebaseReference(storageReference);
        }
    }

    /**
     * Loads tips from Firebase Storage with the given reference.
     *
     * @param storageReference the reference to load tips from Firebase Storage
     */
    private void loadFromFirebaseReference(StorageReference storageReference) {
        if (!InternetConnectionHelper.hasInternetConnection()) {
            setAllTips();
        } else {
            try {
                File localFile = File.createTempFile("tips", ".json");

                storageReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        // Local temp file has been created
                        String json = readFromTempFile(localFile);
                        if (json != null) {
                            try {
                                JSONObject obj = new JSONObject(json);
                                loadTips(obj, cache);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        setAllTips();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                        setAllTips();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                setAllTips();
            }
        }
    }

    /**
     * Sets the tip list to the loaded tips and sends an empty message to the handler of tips to set mutable live data.
     */
    private void setAllTips() {
        List<Tip> list = new ArrayList<>(fixTips);
        if (!cache.isEmpty()) {
            list.addAll(cache);
        }
        allTips = list;
        handlerTips.sendEmptyMessage(0);
    }

    /**
     * Load the given file and returns a string.
     *
     * @param localFile the file that contains tips json
     * @return string of tips json
     */
    private String readFromTempFile(File localFile) {
        try {
            InputStream is = new FileInputStream(localFile);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            return new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Loads the fix tips from assets\base_tips.json
     */
    private void loadFixTips() {
        if (fixTips.isEmpty()) {
            try {
                String loaded = loadFromAsset();
                if (loaded != null) {
                    JSONObject obj = new JSONObject(loaded);
                    loadTips(obj, fixTips);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Loads tips from the given JSONObject and adds to the list.
     *
     * @param jsonObject string of json object
     * @param list       list of tips to which we want to add tips
     */
    private void loadTips(JSONObject jsonObject, List<Tip> list) {
        try {
            JSONArray tips = jsonObject.getJSONArray("tips");

            for (int i = 0; i < tips.length(); i++) {
                JSONObject tip = tips.getJSONObject(i);
                int id = tip.getInt("id");
                long time = tip.getLong("time");

                JSONObject titleObj = tip.getJSONObject("title");
                String title;
                if (!titleObj.isNull(Locale.getDefault().getLanguage())) {
                    title = titleObj.getString(Locale.getDefault().getLanguage());
                } else {
                    title = titleObj.getString("en");
                }

                JSONObject textObj = tip.getJSONObject("text");
                String text;
                if (!textObj.isNull(Locale.getDefault().getLanguage())) {
                    text = textObj.getString(Locale.getDefault().getLanguage());
                } else {
                    text = textObj.getString("en");
                }

                String author = tip.getString("author");
                String source = tip.getString("source");
                String color = tip.getString("hexColor");
                Tip newTip = new Tip(id, time, title, text, author, source, color);

                JSONObject tagObj = tip.getJSONObject("tags");
                JSONArray tags;
                if (!tagObj.isNull(Locale.getDefault().getLanguage())) {
                    tags = tagObj.getJSONArray(Locale.getDefault().getLanguage());
                } else {
                    tags = tagObj.getJSONArray("en");
                }
                for (int j = 0; j < tags.length(); j++) {
                    String tag = tags.getString(j);
                    newTip.addTag(tag);
                }
                list.add(newTip);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads assets\base_tips.json as a string.
     *
     * @return the string of the json
     */
    private String loadFromAsset() {
        try {
            InputStream is = context.getAssets().open("base_tips.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            return new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Searches for the given tip.
     *
     * @param tip the tip we are searching for
     */
    public void findTip(Tip tip) {
        if (fixTips.contains(tip)) {
            tipFilter = fixTips.get(fixTips.indexOf(tip));
        } else if (cache.contains(tip)) {
            tipFilter = cache.get(cache.indexOf(tip));
        } else {
            tipFilter = fixTips.get(0);
        }
        handlerFilter.sendEmptyMessage(0);
    }

    /**
     * Returns the tip we clicked on to view its details.
     *
     * @return the tip we have searched for
     */
    public MutableLiveData<Tip> getTip() {
        return tip;
    }

    /**
     * Returns the list of tips.
     *
     * @return the list of tips
     */
    public MutableLiveData<List<Tip>> getTipsList() {
        return tips;
    }
}
