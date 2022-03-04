package hu.janny.tomsschedule.model.entities;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class ActivityTimeFirebase {

    // Date in long millis
    public long d;
    // Time in long millis
    public long t;
    // Count of users who added time to this activity the given day
    public int c;
    // Gender of the users
    public int g;
    // Age group of the users
    public int a;

    /// Constructors

    public ActivityTimeFirebase() {
    }

    public ActivityTimeFirebase(long d, long t, int c, int g, int a) {
        this.d = d;
        this.t = t;
        this.c = c;
        this.g = g;
        this.a = a;
    }

    // Getters and setters

    public long getD() {
        return d;
    }

    public void setD(long d) {
        this.d = d;
    }

    public long getT() {
        return t;
    }

    public void setT(long t) {
        this.t = t;
    }

    public int getC() {
        return c;
    }

    public void setC(int c) {
        this.c = c;
    }

    public int getG() {
        return g;
    }

    public void setG(int g) {
        this.g = g;
    }

    public int getA() {
        return a;
    }

    public void setA(int a) {
        this.a = a;
    }

    @Exclude
    @Override
    public String toString() {
        return "ActivityTimeFirebase{" +
                "d=" + d +
                ", t=" + t +
                ", c=" + c +
                ", g=" + g +
                ", a=" + a +
                '}';
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("d", d);
        result.put("t", t);
        result.put("c", c);
        result.put("g", g);
        result.put("a", a);

        return result;
    }

}
