package hu.janny.tomsschedule.model.helper;

/**
 * The class if for timer assets to set background, sound, colours.
 */
public class TimerAsset {

    // Resource id of the asset name in R.string
    private final int nameResId;
    // Resource id of the asset background picture in R.drawable
    private final int bgResId;
    // Theme color it of the asset, it can be resource R.color
    private final int color;
    // Text color it of the asset, it can be resource R.color
    private final int colorOfText;
    // Music resource of the asset in R.raw
    private final int musicResId;

    public TimerAsset(int nameResId, int bgResId, int color, int colorOfText, int musicResId) {
        this.nameResId = nameResId;
        this.bgResId = bgResId;
        this.color = color;
        this.colorOfText = colorOfText;
        this.musicResId = musicResId;
    }

    // Getters

    public int getNameResId() {
        return nameResId;
    }

    public int getBgResId() {
        return bgResId;
    }

    public int getColor() {
        return color;
    }

    public int getMusicResId() {
        return musicResId;
    }

    public int getColorOfText() {
        return colorOfText;
    }
}
