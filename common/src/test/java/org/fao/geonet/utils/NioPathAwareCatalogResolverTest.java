package org.fao.geonet.utils;

import org.apache.xml.resolver.CatalogManager;
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
