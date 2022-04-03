package hu.janny.tomsschedule.model;

import org.junit.Test;

import hu.janny.tomsschedule.model.helper.DateConverter;

import static org.junit.Assert.assertEquals;

public class DateConverterTest {

    @Test
    public void makeDateStringForSimpleDateDialog() {
        String result = DateConverter.makeDateStringForSimpleDateDialog(3, 4, 2022);
        assertEquals(result, "APR 3 2022");
        result = DateConverter.makeDateStringForSimpleDateDialog(3, 0, 2022);
        assertEquals(result, "JAN 3 2022");
    }

    @Test
    public void longMillisToStringForSimpleDateDialog() {
        String result = DateConverter.longMillisToStringForSimpleDateDialog(1648986160000L);
        assertEquals(result, "APR 3 2022");
    }

    @Test
    public void birthDateFromSimpleDateDialogToAgeGroupInt() {
        int ageGroup = DateConverter.birthDateFromSimpleDateDialogToAgeGroupInt("SEP 23 2005");
        assertEquals(0, ageGroup);
        ageGroup = DateConverter.birthDateFromSimpleDateDialogToAgeGroupInt("DEC 28 2000");
        assertEquals(1, ageGroup);
        ageGroup = DateConverter.birthDateFromSimpleDateDialogToAgeGroupInt("APR 3 1992");
        assertEquals(2, ageGroup);
        ageGroup = DateConverter.birthDateFromSimpleDateDialogToAgeGroupInt("JAN 1 1975");
        assertEquals(3, ageGroup);
        ageGroup = DateConverter.birthDateFromSimpleDateDialogToAgeGroupInt("DEC 31 1963");
        assertEquals(4, ageGroup);
        ageGroup = DateConverter.birthDateFromSimpleDateDialogToAgeGroupInt("JUN 21 1954");
        assertEquals(5, ageGroup);
    }

    @Test
    public void durationTimeConverterFromIntToLong() {
        long result = DateConverter.durationTimeConverterFromIntToLong(0, 0, 1);
        assertEquals(60000, result);
        result = DateConverter.durationTimeConverterFromIntToLong(0, 1, 1);
        assertEquals(3660000, result);
        result = DateConverter.durationTimeConverterFromIntToLong(1, 1, 1);
        assertEquals(90060000, result);
        result = DateConverter.durationTimeConverterFromIntToLong(0, 0, 59);
        assertEquals(3540000, result);
        result = DateConverter.durationTimeConverterFromIntToLong(0, 23, 59);
        assertEquals(86340000, result);
        result = DateConverter.durationTimeConverterFromIntToLong(0, 0, 0);
        assertEquals(0, result);
    }

    @Test
    public void durationTimeConverterFromIntToLongForDays() {
        long result = DateConverter.durationTimeConverterFromIntToLongForDays(0, 1);
        assertEquals(60000, result);
        result = DateConverter.durationTimeConverterFromIntToLongForDays(1, 0);
        assertEquals(3600000, result);
        result = DateConverter.durationTimeConverterFromIntToLongForDays(0, 59);
        assertEquals(3540000, result);
        result = DateConverter.durationTimeConverterFromIntToLongForDays(23, 59);
        assertEquals(86340000, result);
        result = DateConverter.durationTimeConverterFromIntToLongForDays(0, 0);
        assertEquals(0, result);
    }

    @Test
    public void durationConverterFromLongToStringForADay() {
        String result = DateConverter.durationConverterFromLongToStringForADay(60000);
        assertEquals("0h 1m", result);
        result = DateConverter.durationConverterFromLongToStringForADay(3600000);
        assertEquals("1h 0m", result);
        result = DateConverter.durationConverterFromLongToStringForADay(3540000);
        assertEquals("0h 59m", result);
        result = DateConverter.durationConverterFromLongToStringForADay(86340000);
        assertEquals("23h 59m", result);
        result = DateConverter.durationConverterFromLongToStringForADay(86400000);
        assertEquals("0h 0m", result);
        result = DateConverter.durationConverterFromLongToStringForADay(90000000);
        assertEquals("1h 0m", result);
        result = DateConverter.durationConverterFromLongToStringForADay(0);
        assertEquals("0h 0m", result);
    }

    @Test
    public void durationConverterFromLongToString() {
        String result = DateConverter.durationConverterFromLongToString(60000);
        assertEquals("0h 1m", result);
        result = DateConverter.durationConverterFromLongToString(3600000);
        assertEquals("1h 0m", result);
        result = DateConverter.durationConverterFromLongToString(3540000);
        assertEquals("0h 59m", result);
        result = DateConverter.durationConverterFromLongToString(86340000);
        assertEquals("23h 59m", result);
        result = DateConverter.durationConverterFromLongToString(86400000);
        assertEquals("1d 0h 0m", result);
        result = DateConverter.durationConverterFromLongToString(90000000);
        assertEquals("1d 1h 0m", result);
        result = DateConverter.durationConverterFromLongToString(0);
        assertEquals("0h 0m", result);
    }

    @Test
    public void durationConverterFromLongToStringToTimer() {
        String result = DateConverter.durationConverterFromLongToStringToTimer(0);
        assertEquals("00:00", result);
        result = DateConverter.durationConverterFromLongToStringToTimer(1000);
        assertEquals("00:01", result);
        result = DateConverter.durationConverterFromLongToStringToTimer(59000);
        assertEquals("00:59", result);
        result = DateConverter.durationConverterFromLongToStringToTimer(59999);
        assertEquals("00:59", result);
        result = DateConverter.durationConverterFromLongToStringToTimer(60000);
        assertEquals("01:00", result);
        result = DateConverter.durationConverterFromLongToStringToTimer(3599000);
        assertEquals("59:59", result);
        result = DateConverter.durationConverterFromLongToStringToTimer(3600000);
        assertEquals("01:00:00", result);
        result = DateConverter.durationConverterFromLongToStringToTimer(86399000);
        assertEquals("23:59:59", result);
        result = DateConverter.durationConverterFromLongToStringToTimer(86400000);
        assertEquals("1:00:00:00", result);
    }

    @Test
    public void durationConverterFromLongToBarChart() {
        float result = DateConverter.durationConverterFromLongToBarChart(720000);
        assertEquals(0.2f, result, 0.0);
        result = DateConverter.durationConverterFromLongToBarChart(3600000);
        assertEquals(1.0f, result, 0.0);
        result = DateConverter.durationConverterFromLongToBarChart(1800000);
        assertEquals(0.5f, result, 0.0);
        result = DateConverter.durationConverterFromLongToBarChart(8640000);
        assertEquals(2.4f, result, 0.0);
        result = DateConverter.durationConverterFromLongToBarChart(0);
        assertEquals(0.0f, result, 0.0);
    }

    @Test
    public void durationConverterForPieChart() {
        int result = DateConverter.durationConverterForPieChart(0);
        assertEquals(0, result);
        result = DateConverter.durationConverterForPieChart(23034);
        assertEquals(0, result);
        result = DateConverter.durationConverterForPieChart(60000);
        assertEquals(1, result);
        result = DateConverter.durationConverterForPieChart(1800000);
        assertEquals(30, result);
        result = DateConverter.durationConverterForPieChart(3540000);
        assertEquals(59, result);
        result = DateConverter.durationConverterForPieChart(3570000);
        assertEquals(60, result);
        result = DateConverter.durationConverterForPieChart(3600000);
        assertEquals(60, result);
        result = DateConverter.durationConverterForPieChart(9000000);
        assertEquals(150, result);
    }

    @Test
    public void durationConverterFromLongToChartInt() {
        float result = DateConverter.durationConverterFromLongToChartInt(0);
        assertEquals(0.0, result, 0.0);
        result = DateConverter.durationConverterFromLongToChartInt(2320);
        assertEquals(0.0, result, 0.0);
        result = DateConverter.durationConverterFromLongToChartInt(60000);
        assertEquals(1.0, result, 0.0);
        result = DateConverter.durationConverterFromLongToChartInt(3540000);
        assertEquals(59.0, result, 0.0);
        result = DateConverter.durationConverterFromLongToChartInt(3600000);
        assertEquals(60.0, result, 0.0);
        result = DateConverter.durationConverterFromLongToChartInt(9000000);
        assertEquals(150.0, result, 0.0);
    }

    @Test
    public void durationConverterFromLongToDays() {
        long result = DateConverter.durationConverterFromLongToDays(0);
        assertEquals(0L, result);
        result = DateConverter.durationConverterFromLongToDays(86399999);
        assertEquals(0L, result);
        result = DateConverter.durationConverterFromLongToDays(86400000);
        assertEquals(1L, result);
        result = DateConverter.durationConverterFromLongToDays(90000000);
        assertEquals(1L, result);
    }

    @Test
    public void durationConverterFromLongToHours() {
        long result = DateConverter.durationConverterFromLongToHours(0);
        assertEquals(0L, result);
        result = DateConverter.durationConverterFromLongToHours(3599999);
        assertEquals(0L, result);
        result = DateConverter.durationConverterFromLongToHours(3600000);
        assertEquals(1L, result);
        result = DateConverter.durationConverterFromLongToHours(7201000);
        assertEquals(2L, result);
        result = DateConverter.durationConverterFromLongToHours(86399000);
        assertEquals(23L, result);
        result = DateConverter.durationConverterFromLongToHours(86400000);
        assertEquals(0L, result);
    }

    @Test
    public void durationConverterFromLongToMinutes() {
        long result = DateConverter.durationConverterFromLongToMinutes(0);
        assertEquals(0L, result);
        result = DateConverter.durationConverterFromLongToMinutes(59000);
        assertEquals(0L, result);
        result = DateConverter.durationConverterFromLongToMinutes(60000);
        assertEquals(1L, result);
        result = DateConverter.durationConverterFromLongToMinutes(3540000);
        assertEquals(59L, result);
        result = DateConverter.durationConverterFromLongToMinutes(3600000);
        assertEquals(0L, result);
    }

    @Test
    public void durationConverterFromLongToSeconds() {
        long result = DateConverter.durationConverterFromLongToSeconds(0);
        assertEquals(0L, result);
        result = DateConverter.durationConverterFromLongToSeconds(999);
        assertEquals(0L, result);
        result = DateConverter.durationConverterFromLongToSeconds(1000);
        assertEquals(1L, result);
        result = DateConverter.durationConverterFromLongToSeconds(59000);
        assertEquals(59L, result);
        result = DateConverter.durationConverterFromLongToSeconds(60000);
        assertEquals(0L, result);
    }

    @Test
    public void chartTimeConverter() {
        String result = DateConverter.chartTimeConverter(0.0f);
        assertEquals("0h 0m", result);
        result = DateConverter.chartTimeConverter(0.1f);
        assertEquals("0h 6m", result);
        result = DateConverter.chartTimeConverter(1.0f);
        assertEquals("1h 0m", result);
        result = DateConverter.chartTimeConverter(2.5f);
        assertEquals("2h 30m", result);
        result = DateConverter.chartTimeConverter(25.2f);
        assertEquals("25h 12m", result);
    }

    @Test
    public void chartTimeConverterFromInt() {
        String result = DateConverter.chartTimeConverterFromInt(0.0f);
        assertEquals("0h 0m", result);
        result = DateConverter.chartTimeConverterFromInt(1.0f);
        assertEquals("0h 1m", result);
        result = DateConverter.chartTimeConverterFromInt(59.3f);
        assertEquals("0h 59m", result);
        result = DateConverter.chartTimeConverterFromInt(60.0f);
        assertEquals("1h 0m", result);
        result = DateConverter.chartTimeConverterFromInt(1439.0f);
        assertEquals("23h 59m", result);
        result = DateConverter.chartTimeConverterFromInt(1441.0f);
        assertEquals("24h 1m", result);
    }

    @Test
    public void getMonthIntFromMonthFormat() {
        int result = DateConverter.getMonthIntFromMonthFormat("JAN");
        assertEquals(1, result);
        result = DateConverter.getMonthIntFromMonthFormat("APR");
        assertEquals(4, result);
        result = DateConverter.getMonthIntFromMonthFormat("valami");
        assertEquals(1, result);
    }

    @Test
    public void getMonthFormatFromInt() {
        String result = DateConverter.getMonthFormatFromInt(1);
        assertEquals("JAN", result);
        result = DateConverter.getMonthFormatFromInt(4);
        assertEquals("APR", result);
        result = DateConverter.getMonthFormatFromInt(0);
        assertEquals("JAN", result);
        result = DateConverter.getMonthFormatFromInt(13);
        assertEquals("JAN", result);
    }
}
