package hu.janny.tomsschedule.model;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class ActivityTimeFirebase {

    public long d;
    public long t;
    public int c;

    public ActivityTimeFirebase() {}
    public ActivityTimeFirebase(long d, long t, int c) {
        this.d = d;
        this.t = t;
        this.c = c;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("d", d);
        result.put("t", t);
        result.put("c", c);

        return result;
    }
}
