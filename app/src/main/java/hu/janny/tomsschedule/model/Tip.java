package hu.janny.tomsschedule.model;

import java.util.ArrayList;
import java.util.List;

public class Tip {

    private int id;
    private long time;
    private String title;
    private String text;
    private String author;
    private String source;
    private String url = "";
    private String hexColor;
    private List<String> tags = new ArrayList<>();

    public Tip(int id, long time, String title, String text, String author, String source, String hexColor) {
        this.id = id;
        this.time = time;
        this.title = title;
        this.text = text;
        this.author = author;
        this.source = source;
        this.hexColor = hexColor;
    }

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
}
