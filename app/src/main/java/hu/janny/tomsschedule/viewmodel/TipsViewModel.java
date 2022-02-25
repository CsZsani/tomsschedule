package hu.janny.tomsschedule.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import hu.janny.tomsschedule.model.Tip;
import hu.janny.tomsschedule.model.repository.Repository;
import hu.janny.tomsschedule.model.repository.TipsRepository;

public class TipsViewModel extends AndroidViewModel {

    private final TipsRepository repository;
    private List<Tip> tipsList;
    private final MutableLiveData<Tip> tip;

    public TipsViewModel(@NonNull Application application) {
        super(application);
        repository = new TipsRepository(application);
        tip = repository.getTip();
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

    public List<Tip> getTipsList() {
        return tipsList;
    }

    public MutableLiveData<Tip> getTip() {
        return tip;
    }
}
