package hu.janny.tomsschedule.model;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class ActivityTimeFirebase {

    public long d;
    public long t;
    public int c;
    public int g;
    public int a;

    public ActivityTimeFirebase() {}
    public ActivityTimeFirebase(long d, long t, int c, int g, int a) {
        this.d = d;
        this.t = t;
        this.c = c;
        this.g = g;
        this.a = a;
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
}
