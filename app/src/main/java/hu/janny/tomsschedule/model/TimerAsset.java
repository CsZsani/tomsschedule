package hu.janny.tomsschedule.model;

public class TimerAsset {

    private int nameResId;
    private int bgResId;
    private int color;
    private int colorOfText;
    private int musicResId;

    public TimerAsset(int nameResId, int bgResId, int color, int colorOfText, int musicResId) {
        this.nameResId = nameResId;
        this.bgResId = bgResId;
        this.color = color;
        this.colorOfText = colorOfText;
        this.musicResId = musicResId;
    }

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
