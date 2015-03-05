package org.fao.geonet.services.metadata.format.cache;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ChangeDateValidatorTest {

    @Test
    public void testIsCacheVersionValid() throws Exception {
        final long date = 123456789;
        StoreInfo info = new StoreInfo(null, date, false);
        assertTrue(new ChangeDateValidator(date).isCacheVersionValid(info));
        info = new StoreInfo(null, date + 100, false);
        assertFalse(new ChangeDateValidator(date).isCacheVersionValid(info));
        info = new StoreInfo(null, date - 100, false);
        assertFalse(new ChangeDateValidator(date).isCacheVersionValid(info));
        info = new StoreInfo(null, date - 9, false);
        assertTrue(new ChangeDateValidator(date).isCacheVersionValid(info));
        info = new StoreInfo(null, date + 9, false);
        assertTrue(new ChangeDateValidator(date).isCacheVersionValid(info));
    }
}