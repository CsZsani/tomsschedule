package hu.janny.tomsschedule.model.helper;

/**
 * This interface helps the backup creating and restoring as a callback.
 */
public interface SuccessCallback {
    void onCallback(boolean successful);
}
