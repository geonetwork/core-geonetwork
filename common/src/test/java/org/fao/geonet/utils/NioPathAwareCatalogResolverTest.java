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

package org.fao.geonet.utils;

import org.apache.xml.resolver.CatalogManager;
import org.fao.geonet.utils.nio.NioPathAwareCatalogResolver;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;

import javax.xml.transform.Source;

import static org.junit.Assert.assertEquals;

public class NioPathAwareCatalogResolverTest {

    public static final CatalogManager CAT_MAN = new CatalogManager();

    @Test
    public void testResolveFullUri() throws Exception {

        final NioPathAwareCatalogResolver resolver = new NioPathAwareCatalogResolver(CAT_MAN);
        final Path path = getPathToFile();

        final Path base = path.getParent().resolve("abc.txt");
        final Source source = resolver.resolve(path.toUri().toASCIIString(), base.toUri().toString());
        final String systemId = source.getSystemId();
        assertEquals(path.toAbsolutePath().normalize(), IO.toPath(new URI(systemId)).toAbsolutePath().normalize());
    }

    @Test
    public void testResolveRelative() throws Exception {
        final NioPathAwareCatalogResolver resolver = new NioPathAwareCatalogResolver(CAT_MAN);
        final Path path = getPathToFile();

        final Path base = path.getParent().resolve("abc.txt");
        final String systemId = resolver.resolve(path.getFileName().toString(), base.toUri().toASCIIString()).getSystemId();
        assertEquals(path.toAbsolutePath().normalize(), IO.toPath(new URI(systemId)).toAbsolutePath().normalize());
    }

    @Test
    public void testResolveFullPath() throws Exception {
        final NioPathAwareCatalogResolver resolver = new NioPathAwareCatalogResolver(CAT_MAN);
        final Class<NioPathAwareCatalogResolverTest> thisClass = NioPathAwareCatalogResolverTest.class;
        final URI uri = thisClass.getResource(thisClass.getSimpleName() + ".class").toURI();

        Path path = IO.toPath(uri);
        final Path base = path.getParent().resolve("abc.txt");
        final String systemId = resolver.resolve(path.toAbsolutePath().toString(), base.toUri().toASCIIString()).getSystemId();
        assertEquals(path.toAbsolutePath().normalize(), IO.toPath(new URI(systemId)).toAbsolutePath().normalize());
    }


    protected Path getPathToFile() throws URISyntaxException, IOException {
        final Class<NioPathAwareCatalogResolverTest> thisClass = NioPathAwareCatalogResolverTest.class;
        final URI uri = thisClass.getResource(thisClass.getSimpleName() + ".class").toURI();
        return IO.toPath(uri);
    }

}
