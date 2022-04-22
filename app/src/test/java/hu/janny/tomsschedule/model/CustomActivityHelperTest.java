package hu.janny.tomsschedule.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

import hu.janny.tomsschedule.R;
import hu.janny.tomsschedule.model.entities.ActivityTime;
import hu.janny.tomsschedule.model.entities.CustomActivity;
import hu.janny.tomsschedule.model.entities.CustomWeekTime;
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

    private CustomActivity intervalNoDur = new CustomActivity(
      1, "user", "intervalNoDur", -123456, "note", 7,
            0, 0L, 0, false,
            apr3, apr7,
            0L, 0L, 0L, 0L,
            6,
            false,
            new CustomWeekTime(1, -1L, -1L, -1L, -1L, -1L, -1L, -1L)
    );

    private CustomActivity intervalSumT = new CustomActivity(
            2, "user", "intervalSumT", -123456, "note", 7,
            1, 7200000L, 0, false,
            apr3, apr7,
            0L, 7200000L, 0L, 0L,
            7,
            false,
            new CustomWeekTime(2, -1L, -1L, -1L, -1L, -1L, -1L, -1L)
    );

    private CustomActivity intervalDailyT = new CustomActivity(
            3, "user", "intervalDailyT", -123456, "note", 7,
            2, 7200000L, 0, false,
            apr3, apr7,
            0L, 7200000L, 0L, 0L,
            2,
            false,
            new CustomWeekTime(3, -1L, -1L, -1L, -1L, -1L, -1L, -1L)
    );

    private CustomActivity neitherNoDur = new CustomActivity(
            4, "user", "neitherNoDur", -123456, "note", 7,
            0, 0L, 0, false,
            0L, 0L,
            0L, 0L, 0L, 0L,
            1,
            false,
            new CustomWeekTime(4, -1L, -1L, -1L, -1L, -1L, -1L, -1L)
    );

    private CustomActivity neitherSumT = new CustomActivity(
            5, "user", "neitherSumT", -123456, "note", 7,
            1, 7200000L, 0, false,
            0L, 0L,
            0L, 7200000L, 0L, 0L,
            5,
            false,
            new CustomWeekTime(5, -1L, -1L, -1L, -1L, -1L, -1L, -1L)
    );

    private CustomActivity regularDailyNoDur = new CustomActivity(
            6, "user", "regularDailyNoDur", -123456, "note", 7,
            0, 0L, 1, false,
            0L, 0L,
            0L, 0L, 0L, 0L,
            1,
            false,
            new CustomWeekTime(6, -1L, -1L, -1L, -1L, -1L, -1L, -1L)
    );

    private CustomActivity regularDailyDailyT = new CustomActivity(
            7, "user", "regularDailyDailyT", -123456, "note", 7,
            2, 7200000L, 1, false,
            0L, 0L,
            0L, 7200000L, 0L, 0L,
            2,
            false,
            new CustomWeekTime(7, -1L, -1L, -1L, -1L, -1L, -1L, -1L)
    );

    private CustomActivity regularMonthlyNoEndDate = new CustomActivity(
            8, "user", "regularMonthlyNoEndDate", -123456, "note", 7,
            4, 7200000L, 3, false,
            0L, 0L,
            0L, 7200000L, 0L, 0L,
            3,
            false,
            new CustomWeekTime(8, -1L, -1L, -1L, -1L, -1L, -1L, -1L)
    );

    private CustomActivity regularMonthlyWithEndDate = new CustomActivity(
            9, "user", "regularMonthlyWithEndDate", -123456, "note", 7,
            4, 7200000L, 3, false,
            0L, apr7,
            0L, 7200000L, 0L, 0L,
            3,
            false,
            new CustomWeekTime(9, -1L, -1L, -1L, -1L, -1L, -1L, -1L)
    );

    private CustomActivity regularWeeklyNoFDNoED = new CustomActivity(
            10, "user", "regularWeeklyNoFDNoED", -123456, "note", 7,
            3, 7200000L, 2, false,
            0L, 0L,
            0L, 7200000L, 0L, 0L,
            4,
            false,
            new CustomWeekTime(10, -1L, -1L, -1L, -1L, -1L, -1L, -1L)
    );

    private CustomActivity regularWeeklyNoFDWithED = new CustomActivity(
            11, "user", "regularWeeklyWithFDNoED", -123456, "note", 7,
            3, 7200000L, 2, false,
            0L, apr7,
            0L, 7200000L, 0L, 0L,
            4,
            false,
            new CustomWeekTime(11, -1L, -1L, -1L, -1L, -1L, -1L, -1L)
    );

    private CustomActivity regularWeeklyWithFDNoEDNoDur = new CustomActivity(
            12, "user", "regularWeeklyWithFDNoEDNoDur", -123456, "note", 7,
            0, 0L, 2, true,
            0L, 0L,
            0L, 0L, 0L, 0L,
            1,
            false,
            new CustomWeekTime(12, 0L, -1L, -1L, -1L, 0L, 0L, -1L)
    );

    private CustomActivity regularWeeklyWithFDNoEDDailyT = new CustomActivity(
            13, "user", "regularWeeklyWithFDNoEDDailyT", -123456, "note", 7,
            2, 7200000L, 2, true,
            0L, 0L,
            0L, 7200000L, 0L, 0L,
            2,
            false,
            new CustomWeekTime(13, 0L, -1L, -1L, -1L, 0L, 0L, -1L)
    );

    private CustomActivity regularWeeklyWithFDNoEDWeeklyT = new CustomActivity(
            14, "user", "regularWeeklyWithFDNoEDWeeklyT", -123456, "note", 7,
            3, 7200000L, 2, true,
            0L, 0L,
            0L, 7200000L, 0L, 0L,
            4,
            false,
            new CustomWeekTime(14, 0L, -1L, -1L, -1L, 0L, 0L, -1L)
    );

    private CustomActivity regularWeeklyWithFDNoEDCustomT = new CustomActivity(
            15, "user", "regularWeeklyWithFDNoEDCustomT", -123456, "note", 7,
            5, 0L, 2, true,
            0L, 0L,
            0L, 0L, 0L, 0L,
            8,
            false,
            new CustomWeekTime(15, 7200000L, -1L, -1L, -1L, 7200000L, 7200000L, -1L)
    );

    private CustomActivity regularWeeklyWithFDWithEDNoDur = new CustomActivity(
            16, "user", "regularWeeklyWithFDWithEDNoDur", -123456, "note", 7,
            0, 0L, 2, true,
            0L, apr7,
            0L, 0L, 0L, 0L,
            1,
            false,
            new CustomWeekTime(16, 0L, -1L, -1L, -1L, 0L, 0L, -1L)
    );

    private CustomActivity regularWeeklyWithFDWithEDSumT = new CustomActivity(
            17, "user", "regularWeeklyWithFDWithEDDailyT", -123456, "note", 7,
            1, 7200000L, 2, true,
            0L, apr7,
            0L, 7200000L, 0L, 0L,
            5,
            false,
            new CustomWeekTime(17, 0L, -1L, -1L, -1L, 0L, 0L, -1L)
    );

    private CustomActivity regularWeeklyWithFDWithEDDailyT = new CustomActivity(
            18, "user", "regularWeeklyWithFDWithEDDailyT", -123456, "note", 7,
            2, 7200000L, 2, true,
            0L, apr7,
            0L, 7200000L, 0L, 0L,
            2,
            false,
            new CustomWeekTime(18, 0L, -1L, -1L, -1L, 0L, 0L, -1L)
    );

    private CustomActivity regularWeeklyWithFDWithEDWeeklyT = new CustomActivity(
            19, "user", "regularWeeklyWithFDWithEDWeeklyT", -123456, "note", 7,
            3, 7200000L, 2, true,
            0L, apr7,
            0L, 7200000L, 0L, 0L,
            4,
            false,
            new CustomWeekTime(19, 0L, -1L, -1L, -1L, 0L, 0L, -1L)
    );

    private CustomActivity regularWeeklyWithFDWithEDCustomT = new CustomActivity(
            20, "user", "regularWeeklyWithFDWithEDCustomT", -123456, "note", 7,
            5, 0L, 2, true,
            0L, apr7,
            0L, 0L, 0L, 0L,
            8,
            false,
            new CustomWeekTime(20, 7200000L, -1L, -1L, -1L, 7200000L, 7200000L, -1L)
    );

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

    @Test
    public void detailsOnCardsDeadline() {
        String result = CustomActivityHelper.detailsOnCardsDeadline(intervalDailyT);
        assertEquals("APR 3 2022-APR 7 2022", result);
        result = CustomActivityHelper.detailsOnCardsDeadline(regularMonthlyWithEndDate);
        assertEquals("APR 7 2022", result);
        result = CustomActivityHelper.detailsOnCardsDeadline(regularWeeklyWithFDNoEDCustomT);
        assertEquals("", result);
    }

    @Test
    public void detailsOnCardRegularity() {
        int result = CustomActivityHelper.detailsOnCardRegularity(regularDailyDailyT);
        assertEquals(R.string.details_daily, result);
        result = CustomActivityHelper.detailsOnCardRegularity(regularWeeklyWithFDWithEDSumT);
        assertEquals(R.string.details_weekly, result);
        result = CustomActivityHelper.detailsOnCardRegularity(regularMonthlyNoEndDate);
        assertEquals(R.string.details_monthly, result);
        result = CustomActivityHelper.detailsOnCardRegularity(neitherNoDur);
        assertEquals(0, result);
    }

    @Test
    public void detailsOnCardDuration() {
        String result = CustomActivityHelper.detailsOnCardDuration(intervalSumT);
        assertEquals("2h 0m", result);
        result = CustomActivityHelper.detailsOnCardDuration(neitherSumT);
        assertEquals("2h 0m", result);
        result = CustomActivityHelper.detailsOnCardDuration(regularDailyDailyT);
        assertEquals("2h 0m", result);
        result = CustomActivityHelper.detailsOnCardDuration(regularMonthlyNoEndDate);
        assertEquals("2h 0m", result);
        result = CustomActivityHelper.detailsOnCardDuration(regularWeeklyNoFDNoED);
        assertEquals("2h 0m", result);
        result = CustomActivityHelper.detailsOnCardDuration(regularWeeklyWithFDWithEDCustomT);
        assertEquals("", result);
    }

    @Test
    public void goalDurationForFixedDay() {
        String result = CustomActivityHelper.goalDurationForFixedDay(regularWeeklyWithFDNoEDCustomT.getCustomWeekTime(), DayOfWeek.FRIDAY);
        assertEquals("2h 0m", result);
        result = CustomActivityHelper.goalDurationForFixedDay(regularWeeklyWithFDNoEDCustomT.getCustomWeekTime(), DayOfWeek.TUESDAY);
        assertEquals("0h 0m", result);
        result = CustomActivityHelper.goalDurationForFixedDay(regularWeeklyNoFDNoED.getCustomWeekTime(), DayOfWeek.TUESDAY);
        assertEquals("0h 0m", result);
    }

    @Test
    public void goalDurationForFixedDayLong() {
        long result = CustomActivityHelper.goalDurationForFixedDayLong(regularWeeklyWithFDNoEDCustomT.getCustomWeekTime(), DayOfWeek.FRIDAY);
        assertEquals(7200000L, result);
        result = CustomActivityHelper.goalDurationForFixedDayLong(regularWeeklyWithFDWithEDDailyT.getCustomWeekTime(), DayOfWeek.FRIDAY);
        assertEquals(0L, result);
        result = CustomActivityHelper.goalDurationForFixedDayLong(regularWeeklyWithFDNoEDCustomT.getCustomWeekTime(), DayOfWeek.TUESDAY);
        assertEquals(-1L, result);
        result = CustomActivityHelper.goalDurationForFixedDayLong(regularWeeklyNoFDNoED.getCustomWeekTime(), DayOfWeek.TUESDAY);
        assertEquals(-1L, result);
    }

    @Test
    public void getSoFar() {
        String result = CustomActivityHelper.getSoFar(neitherNoDur);
        assertEquals("-", result);
        result = CustomActivityHelper.getSoFar(intervalDailyT);
        assertEquals("-", result);
        intervalDailyT.setsF(3600000L);
        intervalDailyT.setlD(apr3);
        result = CustomActivityHelper.getSoFar(intervalDailyT);
        assertEquals("-", result);
        intervalDailyT.setsF(0L);
            intervalDailyT.setlD(0L);
        result = CustomActivityHelper.getSoFar(regularMonthlyWithEndDate);
        assertEquals("-", result);
        result = CustomActivityHelper.getSoFar(regularWeeklyNoFDWithED);
        assertEquals("-", result);
        result = CustomActivityHelper.getSoFar(regularWeeklyWithFDWithEDSumT);
        assertEquals("-", result);
        result = CustomActivityHelper.getSoFar(intervalNoDur);
        assertEquals("-", result);
        result = CustomActivityHelper.getSoFar(intervalSumT);
        assertEquals("-", result);
        result = CustomActivityHelper.getSoFar(regularWeeklyWithFDNoEDCustomT);
        assertEquals("0h 0m", result);
    }

    @Test
    public void getSoFarLong() {
        long result = CustomActivityHelper.getSoFarLong(neitherNoDur);
        assertEquals(-1L, result);
        result = CustomActivityHelper.getSoFarLong(intervalDailyT);
        assertEquals(-1L, result);
        intervalDailyT.setsF(3600000L);
        intervalDailyT.setlD(0L);
        result = CustomActivityHelper.getSoFarLong(intervalDailyT);
        assertEquals(-1L, result);
        intervalDailyT.setsF(0L);
        intervalDailyT.setlD(0L);
        result = CustomActivityHelper.getSoFarLong(regularMonthlyWithEndDate);
        assertEquals(-1L, result);
        result = CustomActivityHelper.getSoFarLong(regularWeeklyNoFDWithED);
        assertEquals(-1L, result);
        result = CustomActivityHelper.getSoFarLong(regularWeeklyWithFDWithEDSumT);
        assertEquals(-1L, result);
        result = CustomActivityHelper.getSoFarLong(intervalNoDur);
        assertEquals(-1L, result);
        result = CustomActivityHelper.getSoFarLong(intervalSumT);
        assertEquals(-1L, result);
        result = CustomActivityHelper.getSoFarLong(regularWeeklyWithFDNoEDCustomT);
        assertEquals(-1L, result);
    }
}
