package hu.janny.tomsschedule.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import hu.janny.tomsschedule.model.entities.Tip;
import hu.janny.tomsschedule.model.repository.TipsRepository;

/**
 * The view model of tips.
 */
public class TipsViewModel extends AndroidViewModel {

    private final TipsRepository repository;
    private final MutableLiveData<Tip> tip;
    private final MutableLiveData<List<Tip>> tips;

    public TipsViewModel(@NonNull Application application) {
        super(application);
        repository = new TipsRepository(application);

        tip = repository.getTip();
        tips = repository.getTipsList();
    }

    /**
     * Searches for the given tip.
     *
     * @param tip the tip we are searching for
     */
    public void findTip(Tip tip) {
        repository.findTip(tip);
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
