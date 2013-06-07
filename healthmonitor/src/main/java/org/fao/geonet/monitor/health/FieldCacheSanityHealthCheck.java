package org.fao.geonet.monitor.health;

import jeeves.monitor.HealthCheckFactory;
import jeeves.server.context.ServiceContext;

import org.apache.lucene.search.FieldCache;
import org.apache.lucene.util.FieldCacheSanityChecker;
import org.apache.lucene.util.FieldCacheSanityChecker.Insanity;

import com.yammer.metrics.core.HealthCheck;

public class FieldCacheSanityHealthCheck implements HealthCheckFactory {

    @Override
    public HealthCheck create(ServiceContext context) {
        return new HealthCheck("Default Field Cache Sanity") {
            @Override
            protected Result check() throws Exception {
                StringBuilder b = new StringBuilder();
                check(b, FieldCache.DEFAULT);

                if (b.length() == 0) {
                    return Result.healthy();
                } else {
                    return Result.unhealthy(b.toString());
                }
            }

            private void check(StringBuilder b, FieldCache cache) {
                Insanity[] insanities = FieldCacheSanityChecker.checkSanity(cache);
                for (Insanity insanity : insanities) {
                    b.append(insanity.getMsg());
                }
            }
            
        };
    }
}
