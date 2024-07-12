/*
 * =============================================================================
 * ===	Copyright (C) 2019 Food and Agriculture Organization of the
 * ===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * ===	and United Nations Environment Programme (UNEP)
 * ===
 * ===	This program is free software; you can redistribute it and/or modify
 * ===	it under the terms of the GNU General Public License as published by
 * ===	the Free Software Foundation; either version 2 of the License, or (at
 * ===	your option) any later version.
 * ===
 * ===	This program is distributed in the hope that it will be useful, but
 * ===	WITHOUT ANY WARRANTY; without even the implied warranty of
 * ===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * ===	General Public License for more details.
 * ===
 * ===	You should have received a copy of the GNU General Public License
 * ===	along with this program; if not, write to the Free Software
 * ===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 * ===
 * ===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * ===	Rome - Italy. email: geonetwork@osgeo.org
 * ==============================================================================
 */
package org.fao.geonet.api.records.attachments;

import org.junit.Assume;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;

/**
 * This test needs a configuration placed in services/src/test/resources/s3-test-context.xml to run:
 * <pre>
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;
 *  &lt;beans xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 *         default-lazy-init="true"
 *         xmlns="http://www.springframework.org/schema/beans"
 *         xsi:schemaLocation="
 *          http://www.springframework.org/schema/beans
 *          http://www.springframework.org/schema/beans/spring-beans.xsd
 *         "&gt;
 *
 *     &lt;bean id="s3Store" class="org.fao.geonet.api.records.attachments.S3Store"&gt;
 *       &lt;property name="region" value="eu-west-1"/&gt;
 *       &lt;property name="bucket" value="geonetwork-test"/&gt;
 *       &lt;property name="keyPrefix" value="geonetwork/test"/&gt;
 *       &lt;!-- Only needed if you don't have a ~/.aws/credentials --&gt;
 *       &lt;property name="accessKey" value="MyAccessKey"/&gt;
 *       &lt;property name="secretKey" value="MySecretKey"/&gt;
 *       &lt;!-- Only needed when not using Amazon S3 --&gt;
 *       &lt;property name="endpoint" value="sos-ch-dk-2.exo.io"/&gt;
 *     &lt;/bean&gt;
 *  &lt;/beans&gt;
 * </pre>
 */
public class S3StoreTest extends AbstractStoreTest {
    @Autowired
    private GenericApplicationContext context;

    @Autowired(required = false)
    private S3Store store;

    @Override
    protected Store getStore() {
        Assume.assumeTrue("Cannot load s3-test-context.xml => skipped test", store != null);
        return store;
    }

    @Override
    public void testGetResources() throws Exception {
        super.testGetResources();
    }

    @Override
    public void testPutPatchAndDeleteResource() throws Exception {
        super.testPutPatchAndDeleteResource();
    }

    @Override
    public void testPutResourceFromURL() throws Exception {
        super.testPutResourceFromURL();
    }

    @Override
    public void testPutResourceFromURLWithURLParameters() throws Exception {
        super.testPutResourceFromURLWithURLParameters();
    }
}
