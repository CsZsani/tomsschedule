package hu.janny.tomsschedule.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import hu.janny.tomsschedule.model.entities.Tip;
import hu.janny.tomsschedule.model.repository.TipsRepository;

public class TipsViewModel extends AndroidViewModel {

    private final TipsRepository repository;
    private List<Tip> tipsList;
    private final MutableLiveData<Tip> tip;
    private final MutableLiveData<List<Tip>> tips;

    public TipsViewModel(@NonNull Application application) {
        super(application);
        repository = new TipsRepository(application);
        tip = repository.getTip();
        tips = repository.getTipsList();
    }

    public void findTip(int id) {
        repository.findTipId(id);
    }

    public void findTip(Tip tip) {
        repository.findTip(tip);
    }

    public List<Tip> getTips() {
        return repository.getTips();
    }

    public MutableLiveData<Tip> getTip() {
        return tip;
    }

    public MutableLiveData<List<Tip>> getTipsList() {
        return tips;
    }
}
