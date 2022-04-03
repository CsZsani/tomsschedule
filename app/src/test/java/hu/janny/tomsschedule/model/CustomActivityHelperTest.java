package hu.janny.tomsschedule.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import hu.janny.tomsschedule.R;
import hu.janny.tomsschedule.model.entities.ActivityTime;
import hu.janny.tomsschedule.model.helper.CustomActivityHelper;

public class CustomActivityHelperTest {

    private final long mar29 = 1648504800000L;
    private final long mar30 = 1648591200000L;
    private final long mar31 = 1648677600000L;

    private final long apr1 = 1648764000000L;
    private final long apr2 = 1648850400000L;
    private final long apr3 = 1648936800000L;
    private final long apr4 = 1649023200000L;
    private final long apr5 = 1649109600000L;
    private final long apr6 = 1649196000000L;
    private final long apr7 = 1649282400000L;
    private final long apr8 = 1649368800000L;
    private final long apr9 = 1649455200000L;
    private final long apr10 = 1649541600000L;

    private final long apr30 = 1651269600000L;

    private final long may1 = 1651356000000L;
    private final long may2 = 1651442400000L;
    private final long may3 = 1651528800000L;

    @Test
    public void isFixActivity() {
        boolean result = CustomActivityHelper.isFixActivity("LEARNING");
        assertTrue(result);
        result = CustomActivityHelper.isFixActivity("ENGLISH");
        assertFalse(result);
    }

    @Test
    public void getSelectedFixActivityName() {
        String result = CustomActivityHelper.getSelectedFixActivityName("Housework");
        assertEquals("HOUSEWORK", result);
        result = CustomActivityHelper.getSelectedFixActivityName("Házimunka");
        assertEquals("HOUSEWORK", result);
        result = CustomActivityHelper.getSelectedFixActivityName("Hobby");
        assertEquals("HOBBY", result);
        result = CustomActivityHelper.getSelectedFixActivityName("Hobbi");
        assertEquals("HOBBY", result);
        result = CustomActivityHelper.getSelectedFixActivityName("Cooking");
        assertEquals("COOKING", result);
        result = CustomActivityHelper.getSelectedFixActivityName("Főzés");
        assertEquals("COOKING", result);
        result = CustomActivityHelper.getSelectedFixActivityName("valami");
        assertEquals("ERROR", result);
    }

    @Test
    public void getStringResourceOfFixActivity() {
        int result = CustomActivityHelper.getStringResourceOfFixActivity("WORKOUT");
        assertEquals(R.string.fa_workout, result);
        result = CustomActivityHelper.getStringResourceOfFixActivity("HOBBY");
        assertEquals(R.string.fa_hobby, result);
        result = CustomActivityHelper.getStringResourceOfFixActivity("LEARNING");
        assertEquals(R.string.fa_learning, result);
        result = CustomActivityHelper.getStringResourceOfFixActivity("asdas");
        assertEquals(R.string.error, result);
    }

    @Test
    public void getHowManyTimeWasSpentTodayOnAct_there_is_today() {
        List<ActivityTime> list = new ArrayList<>();
        list.add(new ActivityTime(1,1648944000000L, 3600000));
        list.add(new ActivityTime(2,1648857600000L, 3600000));
        list.add(new ActivityTime(3,1649030400000L, 600000));
        long result = CustomActivityHelper.getHowManyTimeWasSpentTodayOnAct(list, 1648944000000L);
        assertEquals(3600000L, result);
    }

    @Test
    public void getHowManyTimeWasSpentTodayOnAct_no_today() {
        List<ActivityTime> list = new ArrayList<>();
        list.add(new ActivityTime(1,1648133000000L, 3600000));
        list.add(new ActivityTime(2,1648857600000L, 3600000));
        list.add(new ActivityTime(3,1649030400000L, 600000));
        long result = CustomActivityHelper.getHowManyTimeWasSpentTodayOnAct(list, 1648944000000L);
        assertEquals(0L, result);
    }

    @Test
    public void getHowManyTimeWasSpentOnActInInterval_there_are() {
        List<ActivityTime> list = new ArrayList<>();
        list.add(new ActivityTime(1, apr3, 1000000));
        list.add(new ActivityTime(1, apr4, 1000000));
        list.add(new ActivityTime(1, apr5, 1000000));
        list.add(new ActivityTime(1, apr30, 1000000));
        list.add(new ActivityTime(1, may3, 1000000));
        long result = CustomActivityHelper.getHowManyTimeWasSpentOnActInInterval(list, apr3, apr5);
        assertEquals(3000000L, result);
    }

    @Test
    public void getHowManyTimeWasSpentOnActInInterval_no_time_in_interval() {
        List<ActivityTime> list = new ArrayList<>();
        list.add(new ActivityTime(1, apr3, 1000000));
        list.add(new ActivityTime(1, apr4, 1000000));
        list.add(new ActivityTime(1, apr5, 1000000));
        list.add(new ActivityTime(1, apr30, 1000000));
        list.add(new ActivityTime(1, may3, 1000000));
        long result = CustomActivityHelper.getHowManyTimeWasSpentOnActInInterval(list, apr6, apr8);
        assertEquals(0L, result);
    }

    @Test
    public void getHowManyTimeWasSpentFrom_there_are() {
        List<ActivityTime> list = new ArrayList<>();
        list.add(new ActivityTime(1, apr3, 1000000));
        list.add(new ActivityTime(1, apr4, 1000000));
        list.add(new ActivityTime(1, apr5, 1000000));
        list.add(new ActivityTime(1, apr30, 1000000));
        list.add(new ActivityTime(1, may3, 1000000));
        list.add(new ActivityTime(1, mar30, 1000000));
        long result = CustomActivityHelper.getHowManyTimeWasSpentFrom(list, apr3);
        assertEquals(5000000L, result);
    }

    @Test
    public void getHowManyTimeWasSpentFrom_no_time_from_date() {
        List<ActivityTime> list = new ArrayList<>();
        list.add(new ActivityTime(1, apr3, 1000000));
        list.add(new ActivityTime(1, apr4, 1000000));
        list.add(new ActivityTime(1, apr5, 1000000));
        list.add(new ActivityTime(1, apr30, 1000000));
        list.add(new ActivityTime(1, mar30, 1000000));
        long result = CustomActivityHelper.getHowManyTimeWasSpentFrom(list, may1);
        assertEquals(0L, result);
    }
}
