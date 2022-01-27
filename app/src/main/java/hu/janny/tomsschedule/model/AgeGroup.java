package hu.janny.tomsschedule.model;

public enum AgeGroup {
    UNDER_TWENTY (0),
    TWENTY_TO_THIRTY (1),
    THIRTY_TO_FORTY (2),
    FORTY_TO_FIFTY (3),
    FIFTY_TO_SIXTY (4),
    ABOVE_SIXTY (5);

    private final int ageGroupNumber;
    AgeGroup(int ageGroupNumber) {
        this.ageGroupNumber = ageGroupNumber;
    }
    public int getAgeGroupNumber() {return ageGroupNumber;}

}
