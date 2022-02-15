package hu.janny.tomsschedule.model;

import java.util.ArrayList;
import java.util.List;

import hu.janny.tomsschedule.R;

public final class TimerAssets {

    private final static List<TimerAsset> timerAssets = new ArrayList<TimerAsset>(){
        {
            add(new TimerAsset(R.string.colourful, R.drawable.colorful_timer_bg, R.color.colourful, R.color.white, 0));
            add(new TimerAsset(R.string.blue, R.drawable.blue_wood, R.color.blue, R.color.black,0));
            add(new TimerAsset(R.string.green, R.drawable.leaves, R.color.green, R.color.white,0));
            add(new TimerAsset(R.string.morning, R.drawable.coffee_shop, R.color.coffee, R.color.white, R.raw.morning));
            add(new TimerAsset(R.string.water, R.drawable.water, R.color.water, R.color.black,R.raw.ocean_waves));
            add(new TimerAsset(R.string.forest, R.drawable.forest, R.color.forest, R.color.white,R.raw.forest));
            add(new TimerAsset(R.string.fire, R.drawable.fire, R.color.fire, R.color.white,R.raw.fireplace));
            add(new TimerAsset(R.string.milky_way, R.drawable.milky_way, R.color.milky_way, R.color.white,R.raw.milky_way));
        }
    };

    public static TimerAsset getAsset(int i) {
        if(i > timerAssets.size() - 1) {
            return timerAssets.get(timerAssets.size() - 1);
        }
        return timerAssets.get(i);
    }

    public static int maxAssetNum() {
        return timerAssets.size();
    }
}
