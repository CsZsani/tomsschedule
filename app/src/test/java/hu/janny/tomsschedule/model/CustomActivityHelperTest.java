package hu.janny.tomsschedule.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import hu.janny.tomsschedule.model.helper.CustomActivityHelper;

public class CustomActivityHelperTest {

    @Test
    public void isFixActivity() {
        boolean result = CustomActivityHelper.isFixActivity("LEARNING");
        assertTrue(result);
        result = CustomActivityHelper.isFixActivity("ENGLISH");
        assertFalse(result);
    }
}
