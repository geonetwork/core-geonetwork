/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

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
        return new HealthCheck(this.getClass().getSimpleName()) {
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
