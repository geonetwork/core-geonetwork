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

package org.fao.geonet.notifier;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataNotification;
import org.fao.geonet.domain.MetadataNotificationAction;
import org.fao.geonet.domain.MetadataNotificationId;
import org.fao.geonet.domain.MetadataNotifier;
import org.fao.geonet.repository.MetadataNotificationRepository;
import org.fao.geonet.repository.MetadataNotifierRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.MetadataRepositoryTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test that the {@link org.fao.geonet.notifier.MetadataNotifierTask} sends notifications to urls.
 * <p/>
 * Created by Jesse on 3/11/14.
 */
public class MetadataNotifierTaskIntegrationTest extends AbstractCoreIntegrationTest {

    @Autowired
    private MetadataNotifierRepository _notifierRepository;
    @Autowired
    private MetadataNotificationRepository _notificationRepository;
    @Autowired
    private MetadataRepository _metadataRepository;

    @Test
    public void testRun() throws Exception {
        final int port = 34256;
        InetSocketAddress address = new InetSocketAddress(port);
        HttpServer httpServer = HttpServer.create(address, 0);
        final String path = "/notify";
        final AtomicBoolean notified = new AtomicBoolean(false);


        httpServer.createContext(path, new HttpHandler() {
            @Override
            public void handle(HttpExchange httpExchange) throws IOException {
                notified.set(true);
                httpExchange.sendResponseHeaders(200, 0);
                httpExchange.close();
            }
        });
        try {
            httpServer.start();
            TransactionlessTesting.get().run(new TestTask() {
                @Override
                public void run() throws Exception {

                    final Metadata metadata = _metadataRepository.save(MetadataRepositoryTest.newMetadata(_inc));
                    metadata.setUuid(UUID.randomUUID().toString());

                    MetadataNotifier notifier = new MetadataNotifier();
                    notifier.setUrl("http://localhost:" + port + path);
                    notifier.setName("MyNotifier");
                    notifier.setEnabled(true);
                    notifier = _notifierRepository.saveAndFlush(notifier);

                    assertEquals(1, _notifierRepository.count());

                    MetadataNotification notification = new MetadataNotification().
                        setNotified(false).
                        setAction(MetadataNotificationAction.UPDATE).
                        setMetadataUuid(metadata.getUuid());
                    final MetadataNotificationId notificationId = new MetadataNotificationId().
                        setMetadataId(metadata.getId()).
                        setNotifierId(notifier.getId());
                    notification.setId(notificationId);
                    _notificationRepository.save(notification);

                    _applicationContext.getBean(MetadataNotifierTask.class).run();
                }
            });
        } finally {
            httpServer.stop(0);
        }

        assertTrue(notified.get());
    }
}
