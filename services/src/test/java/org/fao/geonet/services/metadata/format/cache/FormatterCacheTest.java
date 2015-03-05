package org.fao.geonet.services.metadata.format.cache;

import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.services.metadata.format.FormatType;
import org.junit.Test;

import java.util.Date;
import java.util.concurrent.Callable;

public class FormatterCacheTest extends AbstractCoreIntegrationTest {

    @Test
    public void testGet() throws Exception {
        final FormatterCache formatterCache = new FormatterCache(new MemoryPersistentStore());


        final boolean hideWithheld = true;
        final long changeDate = new Date().getTime();
        formatterCache.get(new Key(1, "eng", FormatType.html, "full_view", hideWithheld), new ChangeDateValidator(changeDate), new Callable<StoreInfo>() {
            @Override
            public StoreInfo call() throws Exception {
                return new StoreInfo("result", changeDate, false);
            }
        }, true);


    }

    @Test
    public void testGetPublic() throws Exception {

    }
}