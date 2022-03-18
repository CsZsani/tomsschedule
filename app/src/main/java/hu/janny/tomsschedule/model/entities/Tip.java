package hu.janny.tomsschedule.model.entities;

import android.graphics.Color;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.List;

/**
 * This entity is for tips for the fix activities.
 */
public class Tip {

    // Tip id
    private int id;
    // The time when it is added in long millis
    private long time;
    // Title
    private String title;
    // Text
    private String text;
    // Author - book, article on the Internet...
    private String author;
    // Source - name of the book...
    private String source;
    // URL of a site which is a source of the tip
    private String url = "";
    // Colour hex string for the tip card colour
    private String hexColor;
    // Tags - to which fix activity(es) belongs to the tip
    private List<String> tags = new ArrayList<>();

    // Constructors

    public Tip(int id, long time, String title, String text, String author, String source, String hexColor) {
        this.id = id;
        this.time = time;
        this.title = title;
        this.text = text;
        this.author = author;
        this.source = source;
        this.hexColor = hexColor;
    }

    // Setters and getters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getHexColor() {
        return hexColor;
    }

    public void setHexColor(String hexColor) {
        this.hexColor = hexColor;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    /**
     * Returns the colour of the tip in int.
     *
     * @return color int
     */
    @Exclude
    public int getColorInt() {
        return Color.parseColor(hexColor);
    }

    @Exclude
    @Override
    public String toString() {
        return "Tip{" +
                "id=" + id +
                ", time=" + time +
                ", title='" + title + '\'' +
                ", text='" + text + '\'' +
                ", author='" + author + '\'' +
                ", source='" + source + '\'' +
                ", url='" + url + '\'' +
                ", hexColor='" + hexColor + '\'' +
                ", tags=" + tags +
                '}';
    }

    /**
     * Adds a tag to the tip.
     *
     * @param tag the tag be added to the tip, usually it is name of a fix activity
     */
    @Exclude
    public void addTag(String tag) {
        tags.add(tag);
    }
}
