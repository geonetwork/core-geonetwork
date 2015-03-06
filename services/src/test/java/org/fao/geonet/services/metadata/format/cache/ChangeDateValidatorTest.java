package org.fao.geonet.services.metadata.format.cache;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ChangeDateValidatorTest {

    @Test
    public void testIsCacheVersionValid() throws Exception {
        final long date = 123456789;
        StoreInfoAndData info = new StoreInfoAndData(null, date, false);
        assertTrue(new ChangeDateValidator(date).isCacheVersionValid(info));
        info = new StoreInfoAndData(null, date + 100, false);
        assertFalse(new ChangeDateValidator(date).isCacheVersionValid(info));
        info = new StoreInfoAndData(null, date - 100, false);
        assertFalse(new ChangeDateValidator(date).isCacheVersionValid(info));
        info = new StoreInfoAndData(null, date - 9, false);
        assertTrue(new ChangeDateValidator(date).isCacheVersionValid(info));
    }
}