package hu.janny.tomsschedule.model.repository;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

import hu.janny.tomsschedule.model.CustomActivityHelper;
import hu.janny.tomsschedule.model.Tip;
import hu.janny.tomsschedule.model.firebase.FirebaseManager;

public class TipsRepository {

    private final MutableLiveData<Tip> tip = new MutableLiveData<>();
    private Tip tipFilter;
    private List<Tip> fixTips = new ArrayList<>();
    private List<Tip> cache = new ArrayList<>();

    private final FirebaseStorage db;

    public TipsRepository(Application application) {
        db = FirebaseManager.storage;
        fixTips.add(new Tip(1, CustomActivityHelper.todayMillis(), "Tip 1",
                "slhfs lsihf slihf sfsiheuslfhuisrhflseh lsijnfsiuef hsnfls efuhse ufhslieufh slsileufh se" +
                        "i iufh sieufhsi elfuhsiefh sieuhfs iluefh sieuf siuefh isuefhslue fhsliuh sieufh slieufh slie" +
                        "isef sieuf iseugfskeu gfsjehbuzske g", "author", "source", "#F6A433"));
        fixTips.add(new Tip(2, CustomActivityHelper.todayMillis(), "Tip 2",
                "slhfs lsihf slihf sfsiheuslfhuisrhflseh lsijnfsiuef hsnfls efuhse ufhslieufh slsileufh se" +
                        "i iufh sieufhsi elfuhsiefh sieuhfs iluefh sieuf siuefh isuefhslue fhsliuh sieufh slieufh slie" +
                        "isef sieuf iseugfskeu gfsjehbuzske g", "author", "source", "#F6A433"));
        fixTips.add(new Tip(3, CustomActivityHelper.todayMillis(), "Tip 3",
                "slhfs lsihf slihf sfsiheuslfhuisrhflseh lsijnfsiuef hsnfls efuhse ufhslieufh slsileufh se" +
                        "i iufh sieufhsi elfuhsiefh sieuhfs iluefh sieuf siuefh isuefhslue fhsliuh sieufh slieufh slie" +
                        "isef sieuf iseugfskeu gfsjehbuzske g", "author", "source", "#F6A433"));
        fixTips.add(new Tip(4, CustomActivityHelper.todayMillis(), "Tip 4",
                "slhfs lsihf slihf sfsiheuslfhuisrhflseh lsijnfsiuef hsnfls efuhse ufhslieufh slsileufh se" +
                        "i iufh sieufhsi elfuhsiefh sieuhfs iluefh sieuf siuefh isuefhslue fhsliuh sieufh slieufh slie" +
                        "isef sieuf iseugfskeu gfsjehbuzske g", "author", "source", "#F6A433"));
    }

    Handler handlerFilter = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            tip.setValue(tipFilter);
        }
    };

    public void findTipId(int id) {

    }

    public void findTip(Tip tip) {
        if(fixTips.contains(tip)) {
            tipFilter = fixTips.get(fixTips.indexOf(tip));
        } else if(cache.contains(tip)) {
            tipFilter = cache.get(cache.indexOf(tip));
        }
        tipFilter = fixTips.get(0);
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
